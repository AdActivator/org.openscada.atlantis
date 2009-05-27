/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2009 inavare GmbH (http://inavare.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscada.da.server.opc2.connection;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.core.Variant;
import org.openscada.da.server.common.exporter.AbstractPropertyChange;
import org.openscada.da.server.opc2.Helper;
import org.openscada.da.server.opc2.job.Worker;
import org.openscada.da.server.opc2.job.impl.ErrorMessageJob;
import org.openscada.da.server.opc2.job.impl.ItemActivationJob;
import org.openscada.da.server.opc2.job.impl.RealizeItemsJob;
import org.openscada.da.server.opc2.job.impl.UnrealizeItemsJob;
import org.openscada.opc.dcom.common.KeyedResult;
import org.openscada.opc.dcom.common.KeyedResultSet;
import org.openscada.opc.dcom.common.Result;
import org.openscada.opc.dcom.common.ResultSet;
import org.openscada.opc.dcom.da.OPCDATASOURCE;
import org.openscada.opc.dcom.da.OPCITEMDEF;
import org.openscada.opc.dcom.da.OPCITEMRESULT;
import org.openscada.opc.dcom.da.ValueData;
import org.openscada.opc.dcom.da.WriteRequest;

public abstract class OPCIoManager extends AbstractPropertyChange
{

    private final static class ErrorFutureTask<T> extends FutureTask<T>
    {

        private final static class RunnableImplementation implements Runnable
        {
            private final String message;

            public RunnableImplementation ( final String message )
            {
                this.message = message;
            }

            public void run ()
            {
                throw new RuntimeException ( this.message );
            }
        }

        public ErrorFutureTask ( final String message )
        {
            super ( new RunnableImplementation ( message ), null );
        }

    }

    private static final String PROP_SERVER_HANDLE_COUNT = "serverHandleCount";

    private static final String PROP_WRITE_REQUEST_COUNT = "writeRequestCount";

    private static final String PROP_WRITE_REQUEST_MAX = "writeRequestMax";

    private static final String PROP_WRITE_REQUEST_TOTAL = "writeRequestTotal";

    private int writeRequestMax = 0;

    private long writeRequestTotal = 0;

    private static Logger logger = Logger.getLogger ( OPCIoManager.class );

    /**
     * The request queue
     */
    private final Map<String, ItemRegistrationRequest> requestMap = new HashMap<String, ItemRegistrationRequest> ();

    private final Map<String, ItemRegistrationRequest> requestedMap = new HashMap<String, ItemRegistrationRequest> ();

    protected final Map<String, Integer> clientHandleMap = new HashMap<String, Integer> ();

    protected final Map<Integer, String> clientHandleMapRev = new HashMap<Integer, String> ();

    protected final Map<String, Integer> serverHandleMap = new HashMap<String, Integer> ();

    protected final Map<Integer, String> serverHandleMapRev = new HashMap<Integer, String> ();

    private final Map<String, Boolean> activationRequestMap = new HashMap<String, Boolean> ();

    private final Set<String> activeSet = new HashSet<String> ();

    private final Queue<FutureTask<Result<WriteRequest>>> writeRequests = new LinkedList<FutureTask<Result<WriteRequest>>> ();

    private final Set<String> itemUnregistrations = new HashSet<String> ();

    protected final Worker worker;

    protected final OPCModel model;

    protected final OPCController controller;

    private final Map<Integer, String> errorCodeCache = new HashMap<Integer, String> ();

    public OPCIoManager ( final Worker worker, final OPCModel model, final OPCController controller )
    {
        super ();
        this.worker = worker;
        this.model = model;
        this.controller = controller;
    }

    public void shutdown ()
    {
        handleDisconnected ();
    }

    public void requestItemsById ( final Collection<String> requestItems )
    {
        final List<ItemRegistrationRequest> reqs = new ArrayList<ItemRegistrationRequest> ( requestItems.size () );
        for ( final String itemId : requestItems )
        {
            final ItemRegistrationRequest req = new ItemRegistrationRequest ();
            final OPCITEMDEF def = new OPCITEMDEF ();
            def.setItemID ( itemId );
            def.setActive ( false );
            req.setItemDefinition ( def );

            reqs.add ( req );
        }
        requestItems ( reqs );
    }

    public synchronized void requestItems ( final Collection<ItemRegistrationRequest> items )
    {
        for ( final ItemRegistrationRequest itemDef : items )
        {
            final String itemId = itemDef.getItemDefinition ().getItemID ();
            if ( this.requestMap.containsKey ( itemId ) )
            {
                logger.info ( "Item already in request queue" );
                continue;
            }
            if ( this.requestedMap.containsKey ( itemId ) )
            {
                logger.info ( "Item already requested" );
                continue;
            }
            this.requestMap.put ( itemDef.getItemDefinition ().getItemID (), itemDef );
        }
    }

    public synchronized void unrequestItem ( final String itemId )
    {
        this.itemUnregistrations.add ( itemId );
    }

    public void requestItem ( final ItemRegistrationRequest itemDef )
    {
        requestItems ( Arrays.asList ( itemDef ) );
    }

    public void requestItemById ( final String itemId )
    {
        requestItemsById ( Arrays.asList ( itemId ) );
    }

    /**
     * May only be called by the controller
     */
    public void handleConnected () throws InvocationTargetException
    {
        registerAllItems ();
    }

    /**
     * May only be called by the controller
     */
    public synchronized void handleDisconnected ()
    {
        this.itemUnregistrations.clear ();

        for ( final FutureTask<Result<WriteRequest>> request : this.writeRequests )
        {
            request.cancel ( true );
        }

        this.writeRequests.clear ();

        this.listeners.firePropertyChange ( PROP_WRITE_REQUEST_COUNT, null, this.writeRequests.size () );

        this.clientHandleMap.clear ();
        this.clientHandleMapRev.clear ();

        this.serverHandleMap.clear ();
        this.serverHandleMapRev.clear ();
        this.listeners.firePropertyChange ( PROP_SERVER_HANDLE_COUNT, null, this.serverHandleMap.size () );

    }

    /**
     * register all requested items with the OPC server
     * @throws InvocationTargetException
     */
    private void registerAllItems () throws InvocationTargetException
    {
        performRealizeItems ( this.requestedMap.values () );
        setActive ( true, this.activeSet );
    }

    private void performUnrealizeItems ( final Collection<String> items ) throws InvocationTargetException
    {
        if ( items.isEmpty () )
        {
            return;
        }

        // first convert item ids to server handles
        final Set<Integer> itemHandles = new HashSet<Integer> ();

        for ( final String itemId : items )
        {
            final Integer serverHandle = this.serverHandleMap.get ( itemId );
            if ( serverHandle != null )
            {
                itemHandles.add ( serverHandle );
            }
        }

        // perform the operation
        final UnrealizeItemsJob job = new UnrealizeItemsJob ( this.model.getConnectJobTimeout (), this.model.getItemMgt (), itemHandles.toArray ( new Integer[itemHandles.size ()] ) );
        final ResultSet<Integer> result = this.worker.execute ( job, job );

        for ( final Result<Integer> entry : result )
        {
            if ( !entry.isFailed () )
            {
                final Integer serverHandle = entry.getValue ();
                final String itemId = this.serverHandleMapRev.get ( serverHandle );

                removeByServerHandle ( serverHandle );

                if ( itemId != null )
                {
                    try
                    {
                        this.controller.getItemManager ().itemUnrealized ( itemId );
                    }
                    catch ( final Throwable e )
                    {
                        logger.warn ( "Failed to notify item of unrealize", e );
                    }
                }
            }
        }
    }

    /**
     * Remove an item form the internal structure by server handle
     * @param serverHandle the server handle of the item to remove 
     */
    private void removeByServerHandle ( final Integer serverHandle )
    {
        if ( serverHandle != null )
        {
            final String itemId = this.serverHandleMapRev.get ( serverHandle );
            if ( itemId != null )
            {
                final Integer clientHandle = this.clientHandleMap.get ( itemId );
                if ( clientHandle != null )
                {
                    this.clientHandleMapRev.remove ( clientHandle );
                }
                this.clientHandleMap.remove ( itemId );
                this.serverHandleMap.remove ( itemId );
                this.serverHandleMapRev.remove ( serverHandle );
            }
        }
    }

    private void performRealizeItems ( final Collection<ItemRegistrationRequest> newItems ) throws InvocationTargetException
    {
        if ( newItems.isEmpty () )
        {
            return;
        }

        final Random r = new Random ();
        synchronized ( this.clientHandleMap )
        {
            for ( final ItemRegistrationRequest def : newItems )
            {
                Integer i = r.nextInt ();
                while ( this.clientHandleMapRev.containsKey ( i ) )
                {
                    i = r.nextInt ();
                }
                this.clientHandleMap.put ( def.getItemDefinition ().getItemID (), i );
                this.clientHandleMapRev.put ( i, def.getItemDefinition ().getItemID () );
                def.getItemDefinition ().setClientHandle ( i );
            }
        }

        // for now do it one by one .. since packets that get too big cause an error
        for ( final ItemRegistrationRequest def : newItems )
        {
            //RealizeItemsJob job = new RealizeItemsJob ( this.model.getItemMgt (), newItems.toArray ( new OPCITEMDEF[0] ) );
            final RealizeItemsJob job = new RealizeItemsJob ( this.model.getConnectJobTimeout (), this.model.getItemMgt (), new OPCITEMDEF[] { def.getItemDefinition () } );
            final KeyedResultSet<OPCITEMDEF, OPCITEMRESULT> result = this.worker.execute ( job, job );

            for ( final KeyedResult<OPCITEMDEF, OPCITEMRESULT> entry : result )
            {
                final String itemId = entry.getKey ().getItemID ();

                if ( entry.isFailed () )
                {
                    logger.info ( String.format ( "Revoking client handle %d for item %s", entry.getKey ().getClientHandle (), itemId ) );
                    this.clientHandleMap.remove ( itemId );
                    this.clientHandleMapRev.remove ( entry.getKey ().getClientHandle () );
                }
                else
                {
                    final int serverHandle = entry.getValue ().getServerHandle ();
                    this.serverHandleMap.put ( itemId, serverHandle );
                    this.serverHandleMapRev.put ( serverHandle, itemId );
                }
                this.controller.getItemManager ().itemRealized ( def.getItemDefinition ().getItemID (), entry );
            }
        }

        // fire updates
        this.listeners.firePropertyChange ( PROP_SERVER_HANDLE_COUNT, null, this.serverHandleMap.size () );
    }

    public void wakeupItem ( final String item )
    {
        // request the item in any way
        requestItemById ( item );

        synchronized ( this )
        {
            this.activationRequestMap.put ( item, Boolean.TRUE );
            this.activeSet.add ( item );
        }
    }

    public synchronized void suspendItem ( final String item )
    {
        this.activationRequestMap.put ( item, Boolean.FALSE );
        this.activeSet.remove ( item );
    }

    public synchronized OPCIoContext prepareProcessing ()
    {
        final OPCIoContext ctx = new OPCIoContext ();

        // registrations
        if ( !this.requestMap.isEmpty () )
        {
            final List<ItemRegistrationRequest> newItems;
            newItems = new ArrayList<ItemRegistrationRequest> ( this.requestMap.size () );
            for ( final Map.Entry<String, ItemRegistrationRequest> def : this.requestMap.entrySet () )
            {
                newItems.add ( def.getValue () );
                this.requestedMap.put ( def.getKey (), def.getValue () );
            }
            this.requestMap.clear ();
            ctx.setRegistrations ( newItems );
        }

        // activations
        if ( !this.activationRequestMap.isEmpty () )
        {
            ctx.setActivations ( new HashMap<String, Boolean> ( this.activationRequestMap ) );
            this.activationRequestMap.clear ();
        }

        // write
        if ( !this.writeRequests.isEmpty () )
        {
            ctx.setWriteRequests ( new ArrayList<FutureTask<Result<WriteRequest>>> ( this.writeRequests ) );
            this.writeRequests.clear ();
            this.listeners.firePropertyChange ( PROP_WRITE_REQUEST_COUNT, null, this.writeRequests.size () );
        }

        // read
        if ( !this.activeSet.isEmpty () )
        {
            ctx.setReadItems ( new HashSet<String> ( this.activeSet ) );
            // don't clear the active set, we only use it to check what we need to read
        }

        // unrealize
        if ( !this.itemUnregistrations.isEmpty () )
        {
            ctx.setUnregistrations ( new HashSet<String> ( this.itemUnregistrations ) );
            this.itemUnregistrations.clear ();
            for ( final String itemId : ctx.getUnregistrations () )
            {
                this.requestedMap.remove ( itemId );
            }
        }

        return ctx;
    }

    public void performProcessing ( final OPCIoContext ctx, final OPCDATASOURCE dataSource ) throws InvocationTargetException
    {
        if ( ctx.getRegistrations () != null )
        {
            this.controller.setControllerState ( ControllerState.REGISTERING );
            performRealizeItems ( ctx.getRegistrations () );
        }
        if ( ctx.getActivations () != null )
        {
            this.controller.setControllerState ( ControllerState.ACTIVATING );
            performActivations ( ctx.getActivations () );
        }
        if ( ctx.getWriteRequests () != null )
        {
            this.controller.setControllerState ( ControllerState.WRITING );
            performWriteRequests ( ctx.getWriteRequests () );
        }
        if ( ctx.getReadItems () != null )
        {
            this.controller.setControllerState ( ControllerState.READING );
            performRead ( ctx.getReadItems (), dataSource );
        }
        if ( ctx.getUnregistrations () != null )
        {
            this.controller.setControllerState ( ControllerState.UNREGISTERING );
            performUnrealizeItems ( ctx.getUnregistrations () );
        }
    }

    /**
     * Handle the pending activations
     * @param processMap the activations to process
     * @throws InvocationTargetException
     */
    private void performActivations ( final Map<String, Boolean> processMap ) throws InvocationTargetException
    {
        final Set<String> setActive = new HashSet<String> ();
        final Set<String> setInactive = new HashSet<String> ();

        for ( final Map.Entry<String, Boolean> entry : processMap.entrySet () )
        {
            if ( entry.getValue () )
            {
                setActive.add ( entry.getKey () );
            }
            else
            {
                setInactive.add ( entry.getKey () );
            }
        }

        setActive ( true, setActive );
        setActive ( false, setInactive );
    }

    /**
     * execute setting the active state.
     * <p>
     * This method might block until either the timeout occurrs or the
     * operation is completed
     * @param state the state to set
     * @param list the list to set
     * @throws InvocationTargetException
     */
    private void setActive ( final boolean state, final Collection<String> list ) throws InvocationTargetException
    {
        if ( list.isEmpty () )
        {
            return;
        }

        // now look up client IDs
        final List<Integer> handles = new ArrayList<Integer> ( list.size () );
        for ( final String itemId : list )
        {
            final Integer handle = this.serverHandleMap.get ( itemId );
            if ( handle != null )
            {
                handles.add ( handle );
            }
        }

        if ( handles.isEmpty () )
        {
            return;
        }

        final ItemActivationJob job = new ItemActivationJob ( this.model.getConnectJobTimeout (), this.model, state, handles.toArray ( new Integer[0] ) );
        this.worker.execute ( job, job );
    }

    /**
     * Perform the read operation on the already registered items
     * @param dataSource the datasource to read from (cache or device)
     * @throws InvocationTargetException
     */
    protected abstract void performRead ( final Set<String> readSet, final OPCDATASOURCE dataSource ) throws InvocationTargetException;

    /**
     * Provide the registers items with the read result
     * @param result the read result
     * @param useServerHandles <code>true</code> if the result uses server handle, <code>false</code> if it uses client handles
     * @throws InvocationTargetException
     */
    protected void handleReadResult ( final KeyedResultSet<Integer, ValueData> result, final boolean useServerHandles ) throws InvocationTargetException
    {
        for ( final KeyedResult<Integer, ValueData> entry : result )
        {
            final String itemId;

            // get the item id
            if ( useServerHandles )
            {
                itemId = this.serverHandleMapRev.get ( entry.getKey () );
            }
            else
            {
                itemId = this.clientHandleMapRev.get ( entry.getKey () );
            }

            if ( itemId == null )
            {
                logger.info ( String.format ( "Got read reply for invalid item - server handle: '%s'", entry.getKey () ) );
                continue;
            }

            String errorMessage = null;
            if ( entry.isFailed () )
            {
                errorMessage = getErrorMessage ( entry.getErrorCode () );
            }
            this.controller.getItemManager ().dataRead ( itemId, entry, errorMessage );
        }
    }

    private String getErrorMessage ( final int errorCode ) throws InvocationTargetException
    {
        if ( this.errorCodeCache.containsKey ( errorCode ) )
        {
            return this.errorCodeCache.get ( errorCode );
        }

        // fetch from the server
        final ErrorMessageJob job = new ErrorMessageJob ( this.model.getConnectJobTimeout (), this.model, errorCode );
        final String message = this.worker.execute ( job, job );
        this.errorCodeCache.put ( errorCode, message );
        return message;
    }

    public Future<Result<WriteRequest>> addWriteRequest ( final String itemId, final Variant value )
    {
        if ( !this.model.isConnected () )
        {
            // discard write request
            logger.warn ( String.format ( "OPC is not connected", value ) );
            return new ErrorFutureTask<Result<WriteRequest>> ( "OPC is not connected" );
        }

        // request the item first ... nothing happens if we already did that
        requestItemById ( itemId );

        // convert the variant
        final JIVariant variant = Helper.ours2theirs ( value );
        if ( variant == null )
        {
            logger.warn ( String.format ( "Failed to convert %s to variant", value ) );
            return new ErrorFutureTask<Result<WriteRequest>> ( String.format ( "Failed to convert %s to variant", value ) );
        }

        return addWriteRequest ( new OPCWriteRequest ( itemId, variant ) );
    }

    protected abstract FutureTask<Result<WriteRequest>> newWriteFuture ( final OPCWriteRequest request );

    protected Future<Result<WriteRequest>> addWriteRequest ( final OPCWriteRequest request )
    {
        final FutureTask<Result<WriteRequest>> future;

        synchronized ( this )
        {
            future = newWriteFuture ( request );
            this.writeRequests.add ( future );
        }

        // update stats
        final int size = this.writeRequests.size ();
        this.writeRequestMax = Math.max ( this.writeRequestMax, size );
        this.writeRequestTotal++;
        this.listeners.firePropertyChange ( PROP_WRITE_REQUEST_COUNT, null, size );
        this.listeners.firePropertyChange ( PROP_WRITE_REQUEST_MAX, null, size );
        this.listeners.firePropertyChange ( PROP_WRITE_REQUEST_TOTAL, null, this.writeRequestTotal );

        return future;
    }

    /**
     * Perform all queued write requests
     * <p>
     * May only be called by the controller
     * @param requests 
     * @throws InvocationTargetException
     */
    protected abstract void performWriteRequests ( final Collection<FutureTask<Result<WriteRequest>>> requests ) throws InvocationTargetException;

    public int getServerHandleCount ()
    {
        return this.serverHandleMap.size ();
    }

    public int getWriteRequestCount ()
    {
        return this.writeRequests.size ();
    }

    public int getWriteRequestMax ()
    {
        return this.writeRequestMax;
    }

    public long getWriteRequestTotal ()
    {
        return this.writeRequestTotal;
    }

}
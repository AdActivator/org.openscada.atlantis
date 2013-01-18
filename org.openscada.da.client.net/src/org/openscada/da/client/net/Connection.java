/*
 * This file is part of the OpenSCADA project
 * 
 * Copyright (C) 2006-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassi.de)
 *
 * OpenSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.da.client.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.openscada.core.ConnectionInformation;
import org.openscada.core.OperationException;
import org.openscada.core.Variant;
import org.openscada.core.client.ConnectionState;
import org.openscada.core.client.NoConnectionException;
import org.openscada.core.client.net.SessionConnectionBase;
import org.openscada.core.data.SubscriptionState;
import org.openscada.core.net.MessageHelper;
import org.openscada.da.client.BrowseOperationCallback;
import org.openscada.da.client.FolderListener;
import org.openscada.da.client.ItemUpdateListener;
import org.openscada.da.client.WriteAttributeOperationCallback;
import org.openscada.da.client.WriteOperationCallback;
import org.openscada.da.client.net.operations.BrowseOperationController;
import org.openscada.da.client.net.operations.WriteAttributesOperationController;
import org.openscada.da.client.net.operations.WriteOperationController;
import org.openscada.da.core.Location;
import org.openscada.da.core.OperationParameters;
import org.openscada.da.core.WriteAttributeResults;
import org.openscada.da.core.browser.Entry;
import org.openscada.da.net.handler.ListBrowser;
import org.openscada.da.net.handler.Messages;
import org.openscada.da.net.handler.WriteAttributesOperation;
import org.openscada.net.base.MessageListener;
import org.openscada.net.base.data.ListValue;
import org.openscada.net.base.data.MapValue;
import org.openscada.net.base.data.Message;
import org.openscada.net.base.data.StringValue;
import org.openscada.net.base.data.Value;
import org.openscada.utils.exec.LongRunningListener;
import org.openscada.utils.exec.LongRunningOperation;
import org.openscada.utils.exec.LongRunningState;
import org.openscada.utils.lang.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection extends SessionConnectionBase implements org.openscada.da.client.Connection
{

    static
    {
        DriverFactory.registerDriver ();
    }

    public static final String VERSION = "0.1.8";

    private final static Logger logger = LoggerFactory.getLogger ( Connection.class );

    private final Map<String, ItemUpdateListener> itemListeners = new ConcurrentHashMap<String, ItemUpdateListener> ();

    private final Map<Location, FolderListener> folderListeners = new ConcurrentHashMap<Location, FolderListener> ();

    // operations
    private final BrowseOperationController browseController;

    private final WriteOperationController writeController;

    private final WriteAttributesOperationController writeAttributesController;

    @Override
    public String getRequiredVersion ()
    {
        return VERSION;
    }

    public Connection ( final ConnectionInformation connectionInformantion )
    {
        super ( connectionInformantion );

        // setup messaging
        this.messenger.setHandler ( Messages.CC_NOTIFY_DATA, new MessageListener () {

            @Override
            public void messageReceived ( final Message message )
            {
                notifyDataChange ( message );
            }
        } );

        this.messenger.setHandler ( Messages.CC_BROWSER_EVENT, new MessageListener () {

            @Override
            public void messageReceived ( final Message message )
            {
                logger.debug ( "Browse event message from server" );
                performBrowseEvent ( message );
            }
        } );

        this.messenger.setHandler ( Messages.CC_SUBSCRIPTION_CHANGE, new MessageListener () {

            @Override
            public void messageReceived ( final Message message ) throws Exception
            {
                logger.debug ( "received subscription change" );
                performSubscriptionChange ( message );
            }
        } );

        this.browseController = new BrowseOperationController ( this.messenger );
        this.browseController.register ();

        this.writeController = new WriteOperationController ( this.messenger );
        this.writeController.register ();

        this.writeAttributesController = new WriteAttributesOperationController ( this.messenger );
        this.writeAttributesController.register ();
    }

    private void fireBrowseEvent ( final Location location, final Collection<Entry> added, final Collection<String> removed, final boolean full )
    {
        final FolderListener listener = this.folderListeners.get ( location );

        if ( listener != null )
        {
            this.executor.execute ( new Runnable () {

                @Override
                public void run ()
                {
                    listener.folderChanged ( added, removed, full );
                }
            } );
        }
    }

    private void fireDataChange ( final String itemName, final Variant value, final Map<String, Variant> attributes, final boolean cache )
    {
        final ItemUpdateListener listener = this.itemListeners.get ( itemName );
        if ( listener != null )
        {
            this.executor.execute ( new Runnable () {

                @Override
                public void run ()
                {
                    listener.notifyDataChange ( value, attributes, cache );
                }
            } );
        }
    }

    private void notifyDataChange ( final Message message )
    {
        final boolean cache = message.getValues ().containsKey ( "cache-read" );
        final String itemId = message.getValues ().get ( "item-id" ).toString ();

        Variant value = decodeValueChange ( message );
        Map<String, Variant> attributes = decodeAttributeChange ( message );

        if ( cache && value == null )
        {
            // we need a value if we read from cache
            value = Variant.NULL;
        }
        if ( cache && attributes == null )
        {
            // we need attributes if we read from cache
            attributes = new HashMap<String, Variant> ( 0 );
        }

        fireDataChange ( itemId, value, attributes, cache );
    }

    /**
     * Decode the value change information from a "notify data" message
     * 
     * @param message
     *            the message
     * @return the decoded value or <code>null</code> if no value was encoded
     */
    private Variant decodeValueChange ( final Message message )
    {
        if ( message.getValues ().containsKey ( "value" ) )
        {
            return MessageHelper.valueToVariant ( message.getValues ().get ( "value" ), null );
        }
        return null;
    }

    /**
     * Decode the attributes from a "notify data" message
     * 
     * @param message
     *            the message
     * @return the decoded attributes or <code>null</code> if no attribute
     *         changed
     */
    private Map<String, Variant> decodeAttributeChange ( final Message message )
    {
        final Map<String, Variant> attributes = new HashMap<String, Variant> ();

        final Value setEntries = message.getValues ().get ( "attributes-set" );
        if ( setEntries instanceof MapValue )
        {
            for ( final Map.Entry<String, Value> entry : ( (MapValue)setEntries ).getValues ().entrySet () )
            {
                final Variant variant = MessageHelper.valueToVariant ( entry.getValue (), null );
                if ( variant != null )
                {
                    attributes.put ( entry.getKey (), variant );
                }
            }
        }

        final Value unsetEntries = message.getValues ().get ( "attributes-unset" );
        if ( unsetEntries instanceof ListValue )
        {
            for ( final Value entry : ( (ListValue)unsetEntries ).getValues () )
            {
                if ( entry instanceof StringValue )
                {
                    attributes.put ( ( (StringValue)entry ).getValue (), null );
                }
            }
        }

        if ( attributes.isEmpty () )
        {
            return null;
        }
        return attributes;
    }

    private void performBrowseEvent ( final Message message )
    {
        logger.debug ( "Performing browse event" );

        final List<Entry> added = new ArrayList<Entry> ();
        final List<String> removed = new ArrayList<String> ();
        final List<String> path = new ArrayList<String> ();
        final Holder<Boolean> initial = new Holder<Boolean> ();

        initial.value = false;

        ListBrowser.parseEvent ( message, path, added, removed, initial );

        final Location location = new Location ( path );

        logger.debug ( "Folder: {} - Added: {} - Removed: {}", new Object[] { location, added.size (), removed.size () } );

        fireBrowseEvent ( location, added, removed, initial.value );
    }

    // write operation

    @Override
    public void write ( final String item, final Variant value, final OperationParameters operationParameters, final WriteOperationCallback callback )
    {
        try
        {
            this.writeController.start ( item, value, operationParameters, new LongRunningListener () {

                @Override
                public void stateChanged ( final LongRunningOperation operation, final LongRunningState state, final Throwable error )
                {
                    switch ( state )
                    {
                        case FAILURE:
                            if ( callback != null )
                            {
                                callback.failed ( error != null ? error.getMessage () : "<unknown error>" );
                            }
                            break;
                        case SUCCESS:
                            try
                            {
                                completeWrite ( operation );
                                if ( callback != null )
                                {
                                    callback.complete ();
                                }
                            }
                            catch ( final OperationException e )
                            {
                                logger.debug ( "Failed to write", e );
                                if ( callback != null )
                                {
                                    callback.failed ( e.getMessage () );
                                }
                            }
                            break;
                        case REQUESTED:
                            //$FALL-THROUGH$
                        case RUNNING:
                            break;
                    }
                }
            } );
        }
        catch ( final Exception e )
        {
            logger.info ( "Failed to write", e );
            if ( callback != null )
            {
                callback.error ( e );
            }
        }
    }

    protected void completeWrite ( final LongRunningOperation operation ) throws OperationException
    {
        if ( ! ( operation instanceof org.openscada.net.base.LongRunningOperation ) )
        {
            throw new RuntimeException ( "Operation is not of type org.openscada.net.base.LongRunningOperation" );
        }

        final org.openscada.net.base.LongRunningOperation op = (org.openscada.net.base.LongRunningOperation)operation;

        if ( op.getError () != null )
        {
            throw new OperationException ( op.getError () );
        }
        if ( op.getReply () != null )
        {
            final Message reply = op.getReply ();
            if ( reply.getValues ().containsKey ( Message.FIELD_ERROR_INFO ) )
            {
                throw new OperationException ( reply.getValues ().get ( Message.FIELD_ERROR_INFO ).toString () );
            }
        }
    }

    // write attributes operation

    @Override
    public void writeAttributes ( final String item, final Map<String, Variant> attributes, final OperationParameters operationParameters, final WriteAttributeOperationCallback callback )
    {
        try
        {
            this.writeAttributesController.start ( item, attributes, operationParameters, new LongRunningListener () {

                @Override
                public void stateChanged ( final LongRunningOperation operation, final LongRunningState state, final Throwable error )
                {
                    switch ( state )
                    {
                        case FAILURE:
                            if ( callback != null )
                            {
                                callback.failed ( error.getMessage () );
                            }
                            break;
                        case SUCCESS:
                            try
                            {
                                final WriteAttributeResults results = completeWriteAttributes ( operation );
                                if ( callback != null )
                                {
                                    callback.complete ( results );
                                }
                            }
                            catch ( final OperationException e )
                            {
                                logger.debug ( "Failed to write attributes", e );
                                if ( callback != null )
                                {
                                    callback.failed ( e.getMessage () );
                                }
                            }
                            break;
                        case REQUESTED:
                            //$FALL-THROUGH$
                        case RUNNING:
                            break;
                    }
                }
            } );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to perform write", e );
            callback.error ( e );
        }
    }

    public WriteAttributeResults completeWriteAttributes ( final LongRunningOperation operation ) throws OperationException
    {
        if ( ! ( operation instanceof org.openscada.net.base.LongRunningOperation ) )
        {
            throw new RuntimeException ( "Operation is not of type org.openscada.net.base.LongRunningOperation" );
        }

        final org.openscada.net.base.LongRunningOperation op = (org.openscada.net.base.LongRunningOperation)operation;

        if ( op.getError () != null )
        {
            throw new OperationException ( op.getError () );
        }
        if ( op.getReply () != null )
        {
            final Message reply = op.getReply ();
            try
            {
                return WriteAttributesOperation.parseResponse ( reply );
            }
            catch ( final Exception e )
            {
                throw new OperationException ( e );
            }
        }
        return null;
    }

    protected Entry[] completeBrowse ( final LongRunningOperation operation ) throws OperationException
    {
        if ( ! ( operation instanceof org.openscada.net.base.LongRunningOperation ) )
        {
            throw new RuntimeException ( "Operation is not of type org.openscada.net.base.LongRunningOperation" );
        }

        final org.openscada.net.base.LongRunningOperation op = (org.openscada.net.base.LongRunningOperation)operation;

        if ( op.getError () != null )
        {
            throw new OperationException ( op.getError () );
        }
        if ( op.getReply () != null )
        {
            final Message reply = op.getReply ();

            if ( reply.getValues ().containsKey ( Message.FIELD_ERROR_INFO ) )
            {
                // in case of an error
                throw new OperationException ( reply.getValues ().get ( Message.FIELD_ERROR_INFO ).toString () );
            }
            else
            {
                // in case of success
                try
                {
                    return ListBrowser.parseResponse ( reply );
                }
                catch ( final Exception e )
                {
                    logger.info ( "Failed to complete browse", e );
                    throw new OperationException ( e );
                }
            }

        }
        return null;
    }

    @Override
    public void subscribeItem ( final String itemId ) throws NoConnectionException
    {
        logger.debug ( "Subscribe to item: {}", itemId );
        if ( getState () == ConnectionState.BOUND )
        {
            this.messenger.sendMessage ( Messages.subscribeItem ( itemId ) );
        }
    }

    @Override
    public void unsubscribeItem ( final String itemId ) throws NoConnectionException
    {
        logger.debug ( "Unsubscribe from item: {}", itemId );
        if ( getState () == ConnectionState.BOUND )
        {
            this.messenger.sendMessage ( Messages.unsubscribeItem ( itemId ) );
        }
    }

    @Override
    public ItemUpdateListener setItemUpdateListener ( final String itemId, final ItemUpdateListener listener )
    {
        return this.itemListeners.put ( itemId, listener );
    }

    @Override
    public FolderListener setFolderListener ( final Location location, final FolderListener listener )
    {
        return this.folderListeners.put ( location, listener );
    }

    @Override
    public void subscribeFolder ( final Location location ) throws NoConnectionException, OperationException
    {
        logger.debug ( "Subscribe to folder: {}", location );
        this.messenger.sendMessage ( ListBrowser.createSubscribe ( location.asArray () ) );
    }

    @Override
    public void unsubscribeFolder ( final Location location ) throws NoConnectionException, OperationException
    {
        this.messenger.sendMessage ( ListBrowser.createUnsubscribe ( location.asArray () ) );
    }

    @Override
    protected synchronized void onConnectionClosed ()
    {
        // clear all subscribed folders
        final HashMap<Location, FolderListener> listeners = new HashMap<Location, FolderListener> ( this.folderListeners );

        getExecutor ().execute ( new Runnable () {

            @Override
            public void run ()
            {
                for ( final Map.Entry<Location, FolderListener> entry : listeners.entrySet () )
                {
                    entry.getValue ().folderChanged ( Collections.<Entry> emptyList (), Collections.<String> emptyList (), true );
                }
            }
        } );

        super.onConnectionClosed ();
    }

    protected void performSubscriptionChange ( final Message message )
    {
        final Holder<String> item = new Holder<String> ();
        final Holder<SubscriptionState> subscriptionState = new Holder<SubscriptionState> ();

        Messages.parseSubscriptionChange ( message, item, subscriptionState );

        fireSubscriptionChange ( item.value, subscriptionState.value );
    }

    private void fireSubscriptionChange ( final String item, final SubscriptionState subscriptionState )
    {
        // FIXME: should be synchronized
        final ItemUpdateListener listener = this.itemListeners.get ( item );

        if ( listener != null )
        {
            this.executor.execute ( new Runnable () {

                @Override
                public void run ()
                {
                    listener.notifySubscriptionChange ( subscriptionState, null );
                }
            } );
        }
    }

    public Entry[] browse ( final Location location, final int timeout ) throws NoConnectionException, OperationException
    {
        final LongRunningOperation op = this.browseController.start ( location.asArray (), null );
        try
        {
            op.waitForCompletion ( timeout );
            return completeBrowse ( op );
        }
        catch ( final InterruptedException e )
        {
            throw new OperationException ( e );
        }
    }

    @Override
    public void browse ( final Location location, final BrowseOperationCallback callback )
    {
        try
        {
            this.browseController.start ( location.asArray (), new LongRunningListener () {

                @Override
                public void stateChanged ( final LongRunningOperation operation, final LongRunningState state, final Throwable error )
                {
                    switch ( state )
                    {
                        case FAILURE:
                            if ( callback != null )
                            {
                                callback.failed ( error.getMessage () );
                            }
                            break;
                        case SUCCESS:
                            try
                            {
                                final Entry[] result = completeBrowse ( operation );
                                if ( callback != null )
                                {
                                    callback.complete ( result );
                                }
                            }
                            catch ( final OperationException e )
                            {
                                logger.debug ( "Failed to browse", e );
                                if ( callback != null )
                                {
                                    callback.failed ( e.getMessage () );
                                }
                            }
                            break;
                        case REQUESTED:
                            //$FALL-THROUGH$
                        case RUNNING:
                            break;
                    }
                }
            } );
        }
        catch ( final Exception e )
        {
            logger.info ( "Failed to start browsing", e );
            callback.error ( e );
        }
    }

    @Override
    public ScheduledExecutorService getExecutor ()
    {
        return this.executor;
    }
}

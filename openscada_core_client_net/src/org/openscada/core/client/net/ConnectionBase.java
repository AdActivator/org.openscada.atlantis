/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2009 inavare GmbH (http://inavare.com)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openscada.core.client.net;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.openscada.core.ConnectionInformation;
import org.openscada.core.client.Connection;
import org.openscada.core.client.ConnectionState;
import org.openscada.core.client.ConnectionStateListener;
import org.openscada.core.net.ConnectionHelper;
import org.openscada.net.base.PingService;
import org.openscada.net.base.data.Message;
import org.openscada.net.mina.IoSessionSender;
import org.openscada.net.mina.Messenger;
import org.openscada.net.mina.SocketImpl;

public abstract class ConnectionBase implements Connection, IoHandler
{
    private final static class ThreadFactoryImplementation implements ThreadFactory
    {
        private final ConnectionInformation connectionInformation;

        private ThreadFactoryImplementation ( final ConnectionInformation connectionInformation )
        {
            this.connectionInformation = connectionInformation;
        }

        private final AtomicInteger counter = new AtomicInteger ();

        public Thread newThread ( final Runnable r )
        {
            final Thread t = new Thread ( r, "ConnectionBaseExecutor/" + this.counter.getAndIncrement () + "/" + this.connectionInformation );
            t.setDaemon ( true );
            return t;
        }
    }

    private static Logger logger = Logger.getLogger ( ConnectionBase.class );

    private final List<ConnectionStateListener> connectionStateListeners = new CopyOnWriteArrayList<ConnectionStateListener> ();

    private ConnectionState connectionState = ConnectionState.CLOSED;

    private static final int DEFAULT_TIMEOUT = 10000;

    protected IoSession session;

    protected final Messenger messenger;

    private final ConnectionInformation connectionInformation;

    private IoConnector connector;

    private final PingService pingService;

    private ConnectFuture connectingFuture;

    private final ExecutorService executor;

    private SocketAddress remoteAddress;

    public ConnectionBase ( final ConnectionInformation connectionInformation )
    {
        super ();
        this.connectionInformation = connectionInformation;

        this.executor = Executors.newCachedThreadPool ( new ThreadFactoryImplementation ( connectionInformation ) );

        this.messenger = new Messenger ( getMessageTimeout () );

        this.pingService = new PingService ( this.messenger );
    }

    protected synchronized void switchState ( final ConnectionState state, final Throwable error )
    {
        if ( this.connectionState == state )
        {
            logger.info ( "We already are in state: " + state );
            return;
        }

        switch ( this.connectionState )
        {
        case CLOSED:
            handleSwitchClosed ( state );
            break;
        case CONNECTING:
            handleSwitchConnecting ( state, error );
            break;
        case CONNECTED:
            handleSwitchConnected ( state, error );
            break;
        case BOUND:
            handleSwitchBound ( state, error );
            break;
        case CLOSING:
            handleSwitchClosing ( state, error );
            break;
        case LOOKUP:
            handleSwitchLookup ( state, error );
            break;
        }
    }

    private void handleSwitchLookup ( final ConnectionState state, final Throwable error )
    {
        switch ( state )
        {
        case CLOSED:
            performClosed ( error );
            break;
        case CLOSING:
            performClosed ( error );
            break;
        case CONNECTING:
            performConnect ();
            break;
        }
    }

    private void handleSwitchClosing ( final ConnectionState state, final Throwable error )
    {
        switch ( state )
        {
        case CLOSED:
            performClosed ( error );
            break;
        }
    }

    private void handleSwitchBound ( final ConnectionState state, final Throwable error )
    {
        switch ( state )
        {
        case CLOSING:
            requestClose ();
            break;
        case CLOSED:
            performClosed ( error );
            break;
        }
    }

    private void handleSwitchConnected ( final ConnectionState state, final Throwable error )
    {
        switch ( state )
        {
        case CLOSING:
            requestClose ();
            break;
        case CLOSED:
            performClosed ( error );
            break;
        case BOUND:
            setState ( ConnectionState.BOUND, null );
            onConnectionBound ();
            break;
        }
    }

    private void performClosed ( final Throwable error )
    {
        logger.info ( "Performin close stuff" );
        setState ( ConnectionState.CLOSED, error );
        this.messenger.disconnected ();
        disposeConnector ();

        this.session = null;
        this.connectingFuture = null;
    }

    private void requestClose ()
    {
        setState ( ConnectionState.CLOSING, null );

        // we can already disconnect the messenger
        this.messenger.disconnected ();

        this.session.close ( true );
    }

    private void handleSwitchConnecting ( final ConnectionState state, final Throwable error )
    {
        switch ( state )
        {
        case CONNECTED:
            onConnectionEstablished ();
            setState ( ConnectionState.CONNECTED, null );
            break;
        case CLOSED:
            setState ( ConnectionState.CLOSED, error );
            break;
        }
    }

    private void handleSwitchClosed ( final ConnectionState state )
    {
        switch ( state )
        {
        case CONNECTING:
            if ( this.remoteAddress != null )
            {
                performConnect ();
            }
            else
            {
                performLookup ();
            }
            break;
        }
    }

    /**
     * request a disconnect
     */
    public void disconnect ()
    {
        logger.info ( "Requested disconnect" );

        switchState ( ConnectionState.CLOSING, null );
    }

    public ConnectionInformation getConnectionInformation ()
    {
        return this.connectionInformation;
    }

    protected synchronized void disconnected ( final Throwable reason )
    {
        // disconnect the messenger here
        this.messenger.disconnected ();

        if ( this.session != null )
        {
            logger.info ( "Session disconnected", reason );

            setState ( ConnectionState.CLOSING, reason );

            this.session.close ( true );
            this.session = null;
        }

        disposeConnector ();
    }

    public void addConnectionStateListener ( final ConnectionStateListener connectionStateListener )
    {
        this.connectionStateListeners.add ( connectionStateListener );
    }

    public void removeConnectionStateListener ( final ConnectionStateListener connectionStateListener )
    {
        this.connectionStateListeners.remove ( connectionStateListener );
    }

    public ConnectionState getState ()
    {
        return this.connectionState;
    }

    /**
     * set new state internally
     * @param connectionState
     * @param error additional error information or <code>null</code> if we don't have an error.
     */
    private void setState ( final ConnectionState connectionState, final Throwable error )
    {
        boolean trigger = false;
        synchronized ( this )
        {
            if ( this.connectionState != connectionState )
            {
                this.connectionState = connectionState;
                trigger = true;
            }
        }
        if ( trigger )
        {
            notifyStateChange ( connectionState, error );
        }
    }

    /**
     * Notify state change listeners
     * @param connectionState new state
     * @param error additional error information or <code>null</code> if we don't have an error. 
     */
    private void notifyStateChange ( final ConnectionState connectionState, final Throwable error )
    {
        for ( final ConnectionStateListener listener : this.connectionStateListeners )
        {
            try
            {
                listener.stateChange ( this, connectionState, error );
            }
            catch ( final Exception e )
            {
            }
        }
    }

    protected void setupConnector ( final ConnectionInformation connectionInformation, final IoConnector connector )
    {
        // set connector timeout
        connector.setConnectTimeoutMillis ( getConnectTimeout () );

        // build filter chain
        ConnectionHelper.setupFilterChain ( connectionInformation, connector.getFilterChain () );
    }

    public boolean isConnected ()
    {
        return this.session != null;
    }

    public synchronized void connect ()
    {
        switch ( this.connectionState )
        {
        case LOOKUP:
        case CONNECTING:
        case CONNECTED:
            // no-op
            break;
        default:
            switchState ( ConnectionState.CONNECTING, null );
            break;
        }
    }

    protected synchronized void performLookup ()
    {
        setState ( ConnectionState.LOOKUP, null );

        this.executor.execute ( new Runnable () {

            public void run ()
            {
                doLookup ();
            }
        } );
    }

    protected void resolvedRemoteAddress ( final SocketAddress address, final Throwable e )
    {
        logger.info ( String.format ( "Completed resolving remote address: %s", address ), e );
        if ( this.connectionState != ConnectionState.LOOKUP )
        {
            return;
        }
        synchronized ( this )
        {
            if ( e == null )
            {
                // lookup successful ... re-trigger connecting
                this.remoteAddress = address;
                switchState ( ConnectionState.CONNECTING, null );
            }
            else
            {
                Throwable e1 = e;
                if ( e1 == null )
                {
                    e1 = new RuntimeException ( String.format ( "Unable to resolve: %s", address ) );
                }
                // lookup failed
                switchState ( ConnectionState.CLOSED, e1 );
            }
        }

    }

    protected synchronized void performConnect ()
    {
        setState ( ConnectionState.CONNECTING, null );

        this.connector = createConnector ();
        this.connectingFuture = this.connector.connect ( this.remoteAddress );

        this.connectingFuture.addListener ( new IoFutureListener<ConnectFuture> () {

            public void operationComplete ( final ConnectFuture future )
            {
                try
                {
                    future.getSession ();
                }
                catch ( final Throwable e )
                {
                    ConnectionBase.this.connectFailed ( future, e );
                }
            }
        } );
    }

    /**
     * called when a connection attempt failed
     * @param future 
     * @param e the error
     */
    protected synchronized void connectFailed ( final ConnectFuture future, final Throwable e )
    {
        logger.warn ( "Connection attempt failed", e );

        if ( future == this.connectingFuture )
        {
            disposeConnector ();
            this.connectingFuture = null;

            switchState ( ConnectionState.CLOSED, null );
        }
    }

    private String getSocketImpl ()
    {
        return this.connectionInformation.getProperties ().get ( "socketImpl" );
    }

    private IoConnector createConnector ()
    {
        final SocketImpl socketImpl = SocketImpl.fromName ( getSocketImpl () );

        final IoConnector connector = socketImpl.createConnector ();

        connector.setHandler ( this );

        setupConnector ( this.connectionInformation, connector );

        return connector;
    }

    /**
     * Dispose the socket connector
     */
    private void disposeConnector ()
    {
        final IoConnector connector;

        synchronized ( this )
        {
            connector = this.connector;
            this.connector = null;
        }

        // the connector must be disposed in a separate thread, since
        // the call might take some seconds to complete

        if ( connector != null )
        {
            final Runnable r = new Runnable () {

                public void run ()
                {
                    logger.debug ( "Dispose connector..." );
                    connector.dispose ();
                    logger.debug ( "Dispose connector...done" );
                }
            };
            this.executor.execute ( r );
        }
    }

    protected void connectionFailed ( final Throwable e )
    {
        disconnected ( e );
    }

    /**
     * Cancel an open connection ... for debug purposes only
     */
    public void cancelConnection ()
    {
        disconnected ( new Exception ( "cancelled" ) );
    }

    protected void onConnectionClosed ()
    {
    }

    protected void onConnectionEstablished ()
    {
        setBound ();
    }

    public void setBound ()
    {
        switchState ( ConnectionState.BOUND, null );
    }

    protected void onConnectionBound ()
    {
    }

    public void exceptionCaught ( final IoSession session, final Throwable cause ) throws Exception
    {
        logger.error ( "Connection exception", cause );
    }

    public void messageReceived ( final IoSession session, final Object message ) throws Exception
    {
        if ( session == this.session )
        {
            // only accept current session stuff
            if ( message instanceof Message )
            {
                this.messenger.messageReceived ( (Message)message );
            }
        }
    }

    public void messageSent ( final IoSession session, final Object message ) throws Exception
    {
    }

    public void sessionClosed ( final IoSession session ) throws Exception
    {
        logger.info ( "Session closed: " + session );
        switchState ( ConnectionState.CLOSED, null );
    }

    public void sessionCreated ( final IoSession session ) throws Exception
    {
        logger.info ( "Session created: " + session );

        session.getConfig ().setReaderIdleTime ( getPingPeriod () / 1000 );

        if ( this.session == null )
        {
            this.session = session;
        }
        else
        {
            logger.error ( "Created a new session with an existing one!" );
        }
    }

    public void sessionIdle ( final IoSession session, final IdleStatus status ) throws Exception
    {
        logger.info ( "Session idle: " + status + " - " + session );

        if ( session != this.session )
        {
            return;
        }

        this.pingService.sendPing ();
    }

    public synchronized void sessionOpened ( final IoSession session ) throws Exception
    {
        logger.info ( "Session opened: " + session );

        if ( session == this.session )
        {
            this.messenger.connected ( new IoSessionSender ( session ) );

            switchState ( ConnectionState.CONNECTED, null );
        }
    }

    /**
     * get the timeout used for connecting to the remote host
     * @return the timeout in milliseconds
     */
    public int getConnectTimeout ()
    {
        return getIntProperty ( "connectTimeout", getIntProperty ( "timeout", DEFAULT_TIMEOUT ) );
    }

    public int getPingPeriod ()
    {
        return getIntProperty ( "pingPeriod", getIntProperty ( "timeout", DEFAULT_TIMEOUT ) / getIntProperty ( "pingFrequency", 3 ) );
    }

    public int getMessageTimeout ()
    {
        return getIntProperty ( "messageTimeout", getIntProperty ( "timeout", DEFAULT_TIMEOUT ) );
    }

    protected int getIntProperty ( final String propertyName, final int defaultValue )
    {
        try
        {
            final String timeout = this.connectionInformation.getProperties ().get ( propertyName );
            final int i = Integer.parseInt ( timeout );
            if ( i <= 0 )
            {
                return defaultValue;
            }
            return i;
        }
        catch ( final Throwable e )
        {
            return defaultValue;
        }
    }

    @Override
    protected void finalize () throws Throwable
    {
        logger.info ( "Finalized" );
        this.executor.shutdown ();
        super.finalize ();
    }

    /**
     * Does the actual lookup
     */
    private void doLookup ()
    {
        final SocketImpl socketImpl = SocketImpl.fromName ( getSocketImpl () );

        SocketAddress address = null;
        try
        {
            address = socketImpl.doLookup ( ConnectionBase.this.connectionInformation.getTarget (), ConnectionBase.this.connectionInformation.getSecondaryTarget () );
            ConnectionBase.this.resolvedRemoteAddress ( address, null );
        }
        catch ( final Throwable e )
        {
            ConnectionBase.this.resolvedRemoteAddress ( null, e );
        }
    }

}

/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
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

package org.openscada.core.connection.provider;

import java.util.HashMap;
import java.util.Map;

import org.openscada.core.ConnectionInformation;
import org.openscada.core.client.DriverFactory;
import org.openscada.utils.osgi.FilterUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConnectionProvider
{
    private static final Logger logger = LoggerFactory.getLogger ( AbstractConnectionProvider.class );

    protected abstract AbstractConnectionManager createConnectionManager ( final ConnectionRequest request );

    protected final ServiceTracker<ConnectionRequest, ConnectionRequest> tracker;

    protected final BundleContext context;

    private final Map<ConnectionInformation, AbstractConnectionManager> connections = new HashMap<ConnectionInformation, AbstractConnectionManager> ();

    private final Map<String, AbstractConnectionManager> privateConnections = new HashMap<String, AbstractConnectionManager> ();

    public AbstractConnectionProvider ( final BundleContext context, final String interfaceName )
    {
        super ();
        this.context = context;

        Filter filter = null;
        try
        {
            final Map<String, String> parameters = new HashMap<String, String> ();
            parameters.put ( DriverFactory.INTERFACE_NAME, interfaceName );
            filter = FilterUtil.createAndFilter ( ConnectionRequest.class.getName (), parameters );
        }
        catch ( final InvalidSyntaxException e )
        {
            logger.warn ( "Failed to create filter", e );
        }

        if ( filter != null )
        {
            this.tracker = new ServiceTracker<ConnectionRequest, ConnectionRequest> ( context, filter, new ServiceTrackerCustomizer<ConnectionRequest, ConnectionRequest> () {

                @Override
                public void removedService ( final ServiceReference<ConnectionRequest> reference, final ConnectionRequest service )
                {
                    AbstractConnectionProvider.this.removedService ( reference, service );
                }

                @Override
                public void modifiedService ( final ServiceReference<ConnectionRequest> reference, final ConnectionRequest service )
                {
                    AbstractConnectionProvider.this.modifiedService ( reference, service );
                }

                @Override
                public ConnectionRequest addingService ( final ServiceReference<ConnectionRequest> reference )
                {
                    return AbstractConnectionProvider.this.addingService ( reference );
                }
            } );
        }
        else
        {
            this.tracker = null;
        }

    }

    public synchronized void start ()
    {
        this.tracker.open ();
    }

    public synchronized void stop ()
    {
        this.tracker.close ();
    }

    protected ConnectionRequest addingService ( final ServiceReference<ConnectionRequest> reference )
    {
        Object o = this.context.getService ( reference );
        try
        {
            final ConnectionRequest request = (ConnectionRequest)o;
            addRequest ( request );
            o = null;
            return request;
        }
        catch ( final Throwable e )
        {
            if ( o != null )
            {
                this.context.ungetService ( reference );
            }
        }
        return null;
    }

    protected void modifiedService ( final ServiceReference<ConnectionRequest> reference, final Object service )
    {
        if ( service instanceof ConnectionRequest )
        {
            removeRequest ( (ConnectionRequest)service );
            addRequest ( (ConnectionRequest)service );
        }
    }

    protected void removedService ( final ServiceReference<ConnectionRequest> reference, final Object service )
    {
        logger.debug ( "Removed service: {}", reference );

        if ( service instanceof ConnectionRequest )
        {
            removeRequest ( (ConnectionRequest)service );
        }
    }

    private synchronized void removeRequest ( final ConnectionRequest request )
    {
        logger.info ( "Request removed: {}", request );

        final String requestId = request.getRequestId ();

        if ( requestId == null )
        {
            // public request
            final AbstractConnectionManager manager = this.connections.get ( request.getConnectionInformation () );
            if ( manager == null )
            {
                logger.warn ( "Unknown request: {}", request );
                return;
            }

            manager.removeRequest ( request );
            if ( manager.isIdle () )
            {
                logger.info ( "Dropping connection" );

                // if this was the last request .. remove it
                this.connections.remove ( request.getConnectionInformation () );
                manager.dispose ();
            }
        }
        else
        {
            // private request
            final AbstractConnectionManager manager = this.privateConnections.get ( requestId );
            if ( manager == null )
            {
                logger.warn ( "Unknown request: {}", requestId );
                return;
            }
            manager.removeRequest ( request );

            if ( manager.isIdle () )
            {
                logger.info ( "Dropping private connection" );

                // if this was the last request .. remove it
                this.privateConnections.remove ( requestId );
                manager.dispose ();
            }
        }
    }

    private synchronized void addRequest ( final ConnectionRequest request )
    {
        logger.info ( "Found new request: {}", request );

        final String requestId = request.getRequestId ();

        if ( requestId == null )
        {
            // public request
            AbstractConnectionManager manager = this.connections.get ( request.getConnectionInformation () );
            if ( manager == null )
            {
                logger.info ( "Create new connection: {}", request );
                manager = createConnectionManager ( request );
                this.connections.put ( request.getConnectionInformation (), manager );
            }
            manager.addRequest ( request );
        }
        else
        {
            // we have a private request
            AbstractConnectionManager manager = this.privateConnections.get ( requestId );
            if ( manager == null )
            {
                logger.info ( "Create new private connection: {} -> {}", new Object[] { request, requestId } );
                manager = createConnectionManager ( request );
                this.privateConnections.put ( requestId, manager );
            }
            manager.addRequest ( request );
        }
    }

}
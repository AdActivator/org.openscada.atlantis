/*
 * This file is part of the openSCADA project
 * Copyright (C) 2011-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
 *
 * openSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * openSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with openSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.ae.client.ngp;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.eclipse.scada.ae.data.MonitorStatusInformation;
import org.eclipse.scada.core.data.SubscriptionState;
import org.openscada.ae.client.MonitorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorManager
{

    private final static Logger logger = LoggerFactory.getLogger ( MonitorManager.class );

    private final Map<String, MonitorListener> monitorListeners = new HashMap<String, MonitorListener> ();

    private final Executor executor;

    private final ConnectionImpl connection;

    public MonitorManager ( final Executor executor, final ConnectionImpl connection )
    {
        this.executor = executor;
        this.connection = connection;
    }

    public void setMonitorListener ( final String monitorQueryId, final MonitorListener listener )
    {
        if ( listener == null )
        {
            clearMonitorListener ( monitorQueryId );
        }
        else
        {
            updateMonitorListener ( monitorQueryId, listener );
        }
    }

    private void updateMonitorListener ( final String monitorQueryId, final MonitorListener listener )
    {
        MonitorListener oldListener;

        oldListener = this.monitorListeners.put ( monitorQueryId, listener );
        if ( oldListener == listener )
        {
            return;
        }

        if ( oldListener != null )
        {
            // notify old listener first
            fireMonitorStatusChange ( oldListener, SubscriptionState.DISCONNECTED );
        }
        else
        {
            // request data
            sendSubscribeMonitors ( monitorQueryId, true );
        }

        // initially send DISCONNECTED
        fireMonitorStatusChange ( listener, SubscriptionState.DISCONNECTED );
    }

    private void clearMonitorListener ( final String monitorQueryId )
    {
        MonitorListener oldListener;

        oldListener = this.monitorListeners.remove ( monitorQueryId );
        if ( oldListener != null )
        {
            sendSubscribeMonitors ( monitorQueryId, false );
        }
        if ( oldListener != null )
        {
            fireMonitorStatusChange ( oldListener, SubscriptionState.DISCONNECTED );
        }
    }

    private void fireMonitorStatusChange ( final MonitorListener listener, final SubscriptionState status )
    {
        if ( listener == null )
        {
            return;
        }

        this.executor.execute ( new Runnable () {
            @Override
            public void run ()
            {
                listener.statusChanged ( status );
            }
        } );
    }

    private void sendSubscribeMonitors ( final String monitorPoolId, final boolean flag )
    {
        logger.info ( "Requesting monitors: {}/{}", monitorPoolId, flag );

        if ( flag )
        {
            this.connection.sendSubscribeMonitorPool ( monitorPoolId );
        }
        else
        {
            this.connection.sendUnsubscribeMonitorPool ( monitorPoolId );
        }
    }

    private void fireMonitorDataChange ( final MonitorListener listener, final List<MonitorStatusInformation> addedOrUpdated, final Set<String> removed, final boolean full )
    {
        if ( listener == null )
        {
            logger.warn ( "Monitor change data without a listener" );
            return;
        }

        try
        {
            logger.debug ( "notify condition data change" );
            this.executor.execute ( new Runnable () {

                @Override
                public void run ()
                {
                    listener.dataChanged ( addedOrUpdated, removed, full );
                }
            } );

        }
        catch ( final Throwable e )
        {
            logger.warn ( "Failed to notify", e );
        }
    }

    public void dispose ()
    {
        this.monitorListeners.clear ();
    }

    public void onClosed ()
    {
        for ( final MonitorListener listener : this.monitorListeners.values () )
        {
            fireMonitorStatusChange ( listener, SubscriptionState.DISCONNECTED );
            fireMonitorDataChange ( listener, Collections.<MonitorStatusInformation> emptyList (), Collections.<String> emptySet (), true );
        }
    }

    public void onBound ()
    {
        for ( final String id : this.monitorListeners.keySet () )
        {
            this.connection.sendSubscribeMonitorPool ( id );
        }
    }

    public void handleDataUpdate ( final String monitorPoolId, final List<MonitorStatusInformation> addedOrUpdated, final Set<String> removed, final boolean full )
    {
        final MonitorListener listener = this.monitorListeners.get ( monitorPoolId );
        fireMonitorDataChange ( listener, addedOrUpdated, removed, full );
    }

    public void handleStatusUpdate ( final String monitorPoolId, final SubscriptionState state )
    {
        final MonitorListener listener = this.monitorListeners.get ( monitorPoolId );
        fireMonitorStatusChange ( listener, state );
    }

}

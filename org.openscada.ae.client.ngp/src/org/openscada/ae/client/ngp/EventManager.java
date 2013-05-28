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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.openscada.ae.client.EventListener;
import org.openscada.ae.data.EventInformation;
import org.openscada.core.data.SubscriptionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventManager
{

    private final static Logger logger = LoggerFactory.getLogger ( EventManager.class );

    private final Map<String, EventListener> eventListeners = new HashMap<String, EventListener> ();

    private final Executor executor;

    private final ConnectionImpl connection;

    public EventManager ( final Executor executor, final ConnectionImpl connection )
    {
        this.executor = executor;
        this.connection = connection;
    }

    public void setEventListener ( final String eventQueryId, final EventListener listener )
    {
        if ( listener == null )
        {
            clearEventListener ( eventQueryId );
        }
        else
        {
            updateEventListener ( eventQueryId, listener );
        }
    }

    private void updateEventListener ( final String eventQueryId, final EventListener listener )
    {
        logger.debug ( "Update event listener for {}", eventQueryId );

        EventListener oldListener;

        oldListener = this.eventListeners.put ( eventQueryId, listener );
        if ( oldListener == listener )
        {
            return;
        }

        if ( oldListener != null )
        {
            // notify old listener first
            fireEventStatusChange ( oldListener, SubscriptionState.DISCONNECTED );
        }
        else
        {
            // request data
            sendSubscribeEvents ( eventQueryId, true );
        }

        // initially send DISCONNECTED
        fireEventStatusChange ( listener, SubscriptionState.DISCONNECTED );
    }

    private void clearEventListener ( final String eventQueryId )
    {
        logger.debug ( "Clear event listener for {}", eventQueryId );

        EventListener oldListener;

        oldListener = this.eventListeners.remove ( eventQueryId );
        if ( oldListener != null )
        {
            sendSubscribeEvents ( eventQueryId, false );
        }
        if ( oldListener != null )
        {
            fireEventStatusChange ( oldListener, SubscriptionState.DISCONNECTED );
        }
    }

    private void fireEventStatusChange ( final EventListener listener, final SubscriptionState status )
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

    private void sendSubscribeEvents ( final String eventQueryId, final boolean flag )
    {
        logger.info ( "Requesting events: {}/{}", eventQueryId, flag );

        if ( flag )
        {
            this.connection.sendSubscribeEventPool ( eventQueryId );
        }
        else
        {
            this.connection.sendUnsubscribeEventPool ( eventQueryId );
        }
    }

    private void fireEventDataChange ( final EventListener listener, final List<EventInformation> added )
    {
        if ( listener == null )
        {
            logger.warn ( "Event change data without a listener" );
            return;
        }

        try
        {
            logger.debug ( "notify condition data change" );
            this.executor.execute ( new Runnable () {

                @Override
                public void run ()
                {
                    listener.dataChanged ( Events.convertToEvent ( added ) );
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
        logger.debug ( "Dispose" );

        onClosed ();
        this.eventListeners.clear ();
    }

    public void onClosed ()
    {
        for ( final EventListener listener : this.eventListeners.values () )
        {
            fireEventStatusChange ( listener, SubscriptionState.DISCONNECTED );
        }
    }

    public void onBound ()
    {
        for ( final String id : this.eventListeners.keySet () )
        {
            this.connection.sendSubscribeMonitorPool ( id );
        }
    }

    public void handleDataUpdate ( final String eventQueryId, final List<EventInformation> added )
    {
        final EventListener listener = this.eventListeners.get ( eventQueryId );
        fireEventDataChange ( listener, added );
    }

    public void handleStatusUpdate ( final String eventQueryId, final SubscriptionState state )
    {
        final EventListener listener = this.eventListeners.get ( eventQueryId );
        fireEventStatusChange ( listener, state );
    }

}

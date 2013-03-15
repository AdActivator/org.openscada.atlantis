/*
 * This file is part of the openSCADA project
 * 
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassi.de)
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

package org.openscada.core.client.ngp;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openscada.core.info.StatisticsImpl;
import org.openscada.sec.callback.Callback;
import org.openscada.sec.callback.CallbackHandler;
import org.openscada.sec.callback.Callbacks;
import org.openscada.utils.concurrent.NotifyFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenCallbacksManager
{

    private final static Logger logger = LoggerFactory.getLogger ( OpenCallbacksManager.class );

    private static final Object STAT_OPEN_CALLBACK_FUTURES = new Object ();

    private final StatisticsImpl statistics;

    private final ScheduledExecutorService executor;

    private final Set<NotifyFuture<Callback[]>> openFutures = new HashSet<NotifyFuture<Callback[]>> ();

    private final Object lock;

    public OpenCallbacksManager ( final Object lock, final StatisticsImpl statistics, final ScheduledExecutorService executor )
    {
        this.lock = lock;
        this.statistics = statistics;
        this.executor = executor;

        statistics.setLabel ( STAT_OPEN_CALLBACK_FUTURES, "Active callbacks" );
    }

    public void disconnected ()
    {
        logger.debug ( "Disconnected" );

        for ( final NotifyFuture<Callback[]> future : this.openFutures )
        {
            future.cancel ( true );
        }
        this.openFutures.clear ();
        this.statistics.setCurrentValue ( STAT_OPEN_CALLBACK_FUTURES, 0 );
    }

    public NotifyFuture<Callback[]> processCallbacks ( final CallbackHandler callbackHandler, final Callback[] callbacks, final Long timeout )
    {
        logger.debug ( "Process callbacks: {}", callbacks );

        final NotifyFuture<Callback[]> future = Callbacks.callback ( callbackHandler, callbacks );

        logger.debug ( "Future opened: {}", callbacks );

        if ( timeout != null && timeout > 0 && !future.isDone () ) // check also for isDone since it might be an instant future
        {
            this.executor.schedule ( new Runnable () {

                @Override
                public void run ()
                {
                    if ( !future.isCancelled () )
                    {
                        future.cancel ( true );
                    }
                }
            }, timeout, TimeUnit.MILLISECONDS );
        }

        // add before adding the listener since it might be removed in the next call if the future is instant
        this.openFutures.add ( future );

        future.addListener ( new Runnable () {

            @Override
            public void run ()
            {
                closeFuture ( future );
            }
        } );

        this.statistics.setCurrentValue ( STAT_OPEN_CALLBACK_FUTURES, this.openFutures.size () );

        return future;
    }

    protected void closeFuture ( final NotifyFuture<Callback[]> future )
    {
        logger.debug ( "Future closed: {}", future );

        synchronized ( this.lock )
        {
            this.openFutures.remove ( future );
            this.statistics.setCurrentValue ( STAT_OPEN_CALLBACK_FUTURES, this.openFutures.size () );
        }
    }
}

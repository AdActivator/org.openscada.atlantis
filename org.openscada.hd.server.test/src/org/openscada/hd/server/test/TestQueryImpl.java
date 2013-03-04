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

package org.openscada.hd.server.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openscada.hd.Query;
import org.openscada.hd.QueryListener;
import org.openscada.hd.QueryState;
import org.openscada.hd.data.QueryParameters;
import org.openscada.hd.data.ValueInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestQueryImpl implements Query
{

    private final static Logger logger = LoggerFactory.getLogger ( TestQueryImpl.class );

    private final TestItemImpl item;

    private final QueryParameters parameters;

    private final QueryListener listener;

    private final ExecutorService executor;

    public TestQueryImpl ( final TestItemImpl item, final QueryParameters parameters, final QueryListener listener )
    {
        this.item = item;
        this.parameters = parameters;
        this.listener = listener;

        this.executor = Executors.newSingleThreadExecutor ();

        startLoadData ( this.parameters );
    }

    private void startLoadData ( final QueryParameters parameters )
    {
        this.listener.updateState ( QueryState.LOADING );

        if ( parameters.getNumberOfEntries () == 0 )
        {
            this.listener.updateState ( QueryState.COMPLETE );
            return;
        }

        this.executor.execute ( new Runnable () {

            @Override
            public void run ()
            {
                loadData ( parameters, 10 );
            }
        } );
    }

    protected void loadData ( final QueryParameters parameters, final int blockSize )
    {
        this.listener.updateParameters ( parameters, new HashSet<String> ( Arrays.asList ( "AVG", "MIN", "MAX" ) ) );

        final long startTix = parameters.getStartTimestamp ();
        final long endTix = parameters.getEndTimestamp ();
        final long countMillis = endTix - startTix;

        final double step = (double)countMillis / parameters.getNumberOfEntries ();

        long currentTix = startTix;
        final List<ValueInformation> next = new ArrayList<ValueInformation> ();
        int count = 0;
        int startCount = 0;
        while ( true )
        {
            final Calendar start = Calendar.getInstance ();
            start.setTimeInMillis ( currentTix );

            final Calendar end = Calendar.getInstance ();
            long nextTix = currentTix + (long)step;
            if ( nextTix > endTix )
            {
                nextTix = endTix;
            }
            end.setTimeInMillis ( nextTix );

            next.add ( new ValueInformation ( /*100% good*/1.0, 0.0, start.getTimeInMillis (), end.getTimeInMillis (), nextTix - currentTix ) );

            count++;
            if ( nextTix == endTix )
            {
                sendNext ( next, startCount );
                break;
            }
            else if ( count >= blockSize )
            {
                sendNext ( next, startCount );
                startCount += count;
                count = 0;
                next.clear ();
            }
            currentTix = nextTix;
        }

        this.listener.updateState ( QueryState.COMPLETE );
    }

    private void sendNext ( final List<ValueInformation> next, final int sendIndex )
    {
        final int count = next.size ();
        if ( count > 0 )
        {
            logger.info ( "Sending {} entries: {} - {}", new Object[] { count, next.get ( 0 ), next.get ( count - 1 ) } );
        }

        final List<ValueInformation> valueInformation = new ArrayList<ValueInformation> ( count );

        final Map<String, List<Double>> values = new HashMap<String, List<Double>> ( 3 );
        values.put ( "AVG", new ArrayList<Double> ( count ) );
        values.put ( "MIN", new ArrayList<Double> ( count ) );
        values.put ( "MAX", new ArrayList<Double> ( count ) );

        for ( final ValueInformation info : next )
        {
            final double quality = generateValues ( values, info );
            valueInformation.add ( new ValueInformation ( quality, 0.0, info.getStartTimestamp (), info.getEndTimestamp (), info.getSourceValues () ) );
        }

        this.listener.updateData ( sendIndex, values, valueInformation );

    }

    private double generateValues ( final Map<String, List<Double>> values, final ValueInformation vi )
    {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        BigDecimal avg = new BigDecimal ( 0.0 );

        final long start = vi.getStartTimestamp ();
        final long count = vi.getEndTimestamp () - start;

        int good = 0;
        for ( long i = 0; i < count; i++ )
        {
            if ( start + count < System.currentTimeMillis () )
            {
                good++;
                final double d = i + start;
                final double value = Math.sin ( d / 100000.0 ) * 100.0;

                min = Math.min ( value, min );
                max = Math.max ( value, max );
                avg = avg.add ( new BigDecimal ( value ) );
            }
        }

        if ( good != 0 )
        {
            avg = avg.divide ( new BigDecimal ( good ), BigDecimal.ROUND_HALF_UP );
        }
        else
        {
            avg = new BigDecimal ( 0.0 );
        }

        values.get ( "AVG" ).add ( avg.doubleValue () );
        values.get ( "MIN" ).add ( min );
        values.get ( "MAX" ).add ( max );

        return (double)good / (double)count;
    }

    @Override
    public void close ()
    {
        logger.info ( "Close query" );

        this.executor.shutdownNow ();

        this.listener.updateState ( QueryState.DISCONNECTED );

        this.item.remove ( this );
    }

    @Override
    public void changeParameters ( final QueryParameters parameters )
    {
        startLoadData ( parameters );
    }
}

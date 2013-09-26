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

package org.eclipse.scada.da.datasource.movingaverage;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DataItemValueRange
{
    private DataItemValueLight firstValue = DataItemValueLight.DISCONNECTED;

    private final SortedSet<DataItemValueLight> values = new TreeSet<DataItemValueLight> (); // 

    private final ExecutorService executor; // used to serialize access to internal state

    private final long range; // timespan to hold values since oldestPossibleTimestamp 

    private long oldestPossibleTimestamp; // timestamp in milliseconds

    public DataItemValueRange ( final ExecutorService executor, final long range )
    {
        if ( executor == null )
        {
            throw new IllegalArgumentException ( "'executor' must not be null" );
        }
        this.executor = executor;
        this.range = range;
        this.oldestPossibleTimestamp = System.currentTimeMillis () - range;
    }

    public int add ( final DataItemValueLight dataItemValue )
    {
        final Future<Integer> future = this.executor.submit ( new Callable<Integer> () {
            @Override
            public Integer call () throws Exception
            {
                if ( DataItemValueRange.this.firstValue == DataItemValueLight.DISCONNECTED )
                {
                    DataItemValueRange.this.firstValue = dataItemValue;
                }
                DataItemValueRange.this.values.add ( dataItemValue );
                checkRangeInternal ( System.currentTimeMillis () - DataItemValueRange.this.range );
                return DataItemValueRange.this.values.size ();
            }
        } );
        try
        {
            return future.get ( 30, TimeUnit.SECONDS );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public int checkRange ()
    {
        final Future<Integer> future = this.executor.submit ( new Callable<Integer> () {
            @Override
            public Integer call () throws Exception
            {
                checkRangeInternal ( System.currentTimeMillis () - DataItemValueRange.this.range );
                return DataItemValueRange.this.values.size ();
            }
        } );
        try
        {
            return future.get ( 30, TimeUnit.SECONDS );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public DataItemValueRangeInfo getInfo ()
    {
        final Future<DataItemValueRangeInfo> future = this.executor.submit ( new Callable<DataItemValueRangeInfo> () {
            @Override
            public DataItemValueRangeInfo call () throws Exception
            {
                return new DataItemValueRangeInfo ( DataItemValueRange.this.oldestPossibleTimestamp, DataItemValueRange.this.values.size (), DataItemValueRange.this.firstValue );
            }
        } );
        try
        {
            return future.get ( 30, TimeUnit.SECONDS );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public DataItemValueRangeState getState ()
    {
        final Future<DataItemValueRangeState> future = this.executor.submit ( new Callable<DataItemValueRangeState> () {
            @Override
            public DataItemValueRangeState call () throws Exception
            {
                return new DataItemValueRangeState ( DataItemValueRange.this.oldestPossibleTimestamp, DataItemValueRange.this.values.size (), DataItemValueRange.this.firstValue, new TreeSet<DataItemValueLight> ( DataItemValueRange.this.values ) );
            }
        } );
        try
        {
            return future.get ( 30, TimeUnit.SECONDS );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public long getRange ()
    {
        return this.range;
    }

    private void checkRangeInternal ( final long oldestPossibleTimestamp )
    {
        final TreeSet<DataItemValueLight> toRemove = new TreeSet<DataItemValueLight> ();
        for ( final DataItemValueLight dataItemValueLight : this.values )
        {
            if ( dataItemValueLight.getTimestamp () < oldestPossibleTimestamp )
            {
                toRemove.add ( dataItemValueLight );
            }
        }
        if ( !toRemove.isEmpty () )
        {
            this.firstValue = toRemove.last ();
            this.values.removeAll ( toRemove );
        }
        this.oldestPossibleTimestamp = oldestPossibleTimestamp;
    }

    public class DataItemValueRangeInfo implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final long oldestPossibleTimestamp;

        private final int size;

        private final DataItemValueLight firstValue;

        public DataItemValueRangeInfo ( final long oldestPossibleTimestamp, final int size, final DataItemValueLight firstValue )
        {
            this.oldestPossibleTimestamp = oldestPossibleTimestamp;
            this.size = size;
            this.firstValue = firstValue;
        }

        public long getOldestPossibleTimestamp ()
        {
            return this.oldestPossibleTimestamp;
        }

        public int getSize ()
        {
            return this.size;
        }

        public DataItemValueLight getFirstValue ()
        {
            return this.firstValue;
        }

        @Override
        public String toString ()
        {
            return "firstValue=" + this.firstValue + ", size=" + this.size + ", oldestPossibleTimestamp=" + String.format ( "[%1$tF %1$tT,%1$tL]", this.oldestPossibleTimestamp );
        }
    }

    public class DataItemValueRangeState extends DataItemValueRangeInfo
    {
        private static final long serialVersionUID = 1L;

        private final SortedSet<DataItemValueLight> values;

        public DataItemValueRangeState ( final long oldestPossibleTimestamp, final int size, final DataItemValueLight firstValue, final SortedSet<DataItemValueLight> values )
        {
            super ( oldestPossibleTimestamp, size, firstValue );
            this.values = values;
        }

        public SortedSet<DataItemValueLight> getValues ()
        {
            return this.values;
        }

        @Override
        public String toString ()
        {
            return super.toString () + ", values=" + this.values;
        }
    }
}

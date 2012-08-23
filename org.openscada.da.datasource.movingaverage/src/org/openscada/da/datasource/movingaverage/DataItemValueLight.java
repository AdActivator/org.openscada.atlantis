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

package org.openscada.da.datasource.movingaverage;

import java.util.Date;

import org.openscada.core.Variant;
import org.openscada.core.subscription.SubscriptionState;
import org.openscada.da.client.DataItemValue;

public class DataItemValueLight implements Comparable<DataItemValueLight>
{
    public static final DataItemValueLight DISCONNECTED = new DataItemValueLight ( Variant.NULL, SubscriptionState.DISCONNECTED, Long.MIN_VALUE );

    private final Variant value;

    private final SubscriptionState subscriptionState;

    private final long timestamp;

    public DataItemValueLight ( final Variant value, final SubscriptionState subscriptionState, final long timestamp )
    {
        if ( subscriptionState == null )
        {
            throw new IllegalArgumentException ( "'subscriptionState' must not be null" );
        }
        this.value = Variant.valueOf ( value );
        this.subscriptionState = subscriptionState;
        this.timestamp = timestamp;
    }

    public static DataItemValueLight valueOf ( final DataItemValue dataItemValue )
    {
        if ( ( dataItemValue == null ) || dataItemValue.equals ( DataItemValue.DISCONNECTED ) )
        {
            return DISCONNECTED;
        }
        return new DataItemValueLight ( dataItemValue.getValue (), dataItemValue.getSubscriptionState (), dataItemValue.getTimestamp () == null ? Long.MIN_VALUE : dataItemValue.getTimestamp ().getTimeInMillis () );
    }

    public static DataItemValueLight valueOf ( final DataItemValueLight dataItemValueLight )
    {
        if ( ( dataItemValueLight == null ) || dataItemValueLight.equals ( DISCONNECTED ) )
        {
            return DISCONNECTED;
        }
        return new DataItemValueLight ( dataItemValueLight.value, dataItemValueLight.subscriptionState, dataItemValueLight.timestamp );
    }

    public Variant getValue ()
    {
        return this.value;
    }

    public SubscriptionState getSubscriptionState ()
    {
        return this.subscriptionState;
    }

    public long getTimestamp ()
    {
        return this.timestamp;
    }

    public boolean hasValue ()
    {
        return this.value.isNumber ();
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = ( prime * result ) + ( ( this.value == null ) ? 0 : this.value.hashCode () );
        result = ( prime * result ) + ( ( this.subscriptionState == null ) ? 0 : this.subscriptionState.hashCode () );
        result = ( prime * result ) + (int) ( this.timestamp ^ ( this.timestamp >>> 32 ) );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass () != obj.getClass () )
        {
            return false;
        }
        final DataItemValueLight other = (DataItemValueLight)obj;
        if ( this.subscriptionState != other.subscriptionState )
        {
            return false;
        }
        if ( this.timestamp != other.timestamp )
        {
            return false;
        }
        if ( this.value == null )
        {
            if ( other.value != null )
            {
                return false;
            }
        }
        else if ( !this.value.equals ( other.value ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo ( final DataItemValueLight o )
    {
        int c = 0;
        c = this.value.compareTo ( o.value );
        if ( c != 0 )
        {
            return c;
        }
        c = this.subscriptionState.compareTo ( o.subscriptionState );
        if ( c != 0 )
        {
            return c;
        }
        return Long.valueOf ( this.timestamp ).compareTo ( o.timestamp );
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();
        sb.append ( this.value );
        sb.append ( "[" );
        sb.append ( this.subscriptionState );
        sb.append ( "]" );
        sb.append ( String.format ( "[%1$tF %1$tT,%1$tL]", new Date ( this.timestamp ) ) );
        return sb.toString ();
    }
}

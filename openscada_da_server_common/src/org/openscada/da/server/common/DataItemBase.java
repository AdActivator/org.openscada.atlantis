/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006 inavare GmbH (http://inavare.com)
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

package org.openscada.da.server.common;

import java.util.Map;

import org.openscada.core.Variant;
import org.openscada.da.core.DataItemInformation;
import org.openscada.da.server.common.chain.DataItemBaseChained;

/**
 * This is an abstract base class for the {@link DataItem} interface. It also supports
 * the {@link SuspendableDataItem} interface.
 * @author Jens Reimann &lt;jens.reimann@inavare.net&gt;
 * @see SuspendableDataItem
 */
public abstract class DataItemBase implements DataItem
{
    protected ItemListener listener;

    private final DataItemInformation _information;

    public DataItemBase ( final DataItemInformation information )
    {
        this._information = information;
    }

    public DataItemInformation getInformation ()
    {
        return this._information;
    }

    public synchronized void setListener ( final ItemListener listener )
    {
        if ( this.listener != listener )
        {
            handleListenerChange ( listener );
        }
    }

    protected synchronized void handleListenerChange ( final ItemListener listener )
    {
        if ( listener == null )
        {
            if ( this instanceof SuspendableDataItem )
            {
                ( (SuspendableDataItem)this ).suspend ();
            }
        }
        else if ( this.listener == null )
        {
            // we might need the listener in the wakeup call 
            this.listener = listener;
            if ( this instanceof SuspendableDataItem )
            {
                ( (SuspendableDataItem)this ).wakeup ();
            }
        }
        this.listener = listener;

        if ( this.listener != null )
        {
            Variant cacheValue = getCacheValue ();
            if ( cacheValue != null && cacheValue.isNull () )
            {
                cacheValue = null;
            }
            Map<String, Variant> cacheAttributes = getCacheAttributes ();
            if ( cacheAttributes != null && cacheAttributes.isEmpty () )
            {
                cacheAttributes = null;
            }
            if ( cacheValue != null || cacheAttributes != null )
            {
                notifyData ( cacheValue, cacheAttributes, true );
            }
        }
    }

    protected Variant getCacheValue ()
    {
        return null;
    }

    protected Map<String, Variant> getCacheAttributes ()
    {
        return null;
    }

    /**
     * Notify a data change without checking for a real change.
     * <p>
     * See {@link #notifyData(Variant, Map, boolean)} when to use the method!
     * @param value the value to send
     * @param attributes the attributes to send
     */
    protected void notifyData ( final Variant value, final Map<String, Variant> attributes )
    {
        notifyData ( value, attributes, false );
    }

    /**
     * Notify a data change without checking for a real change.
     * <p>
     * This method simply forwards the change notification to the currently connected listener. It does
     * not provide any real difference check and should therefore only be called by implementations that
     * check for difference first.
     * <p>
     * If you simple want to send data away without the need to check for differences first see
     * {@link DataItemBaseChained}, {@link DataItemInput} or one of their derivations.
     * 
     * @param value the value to send
     * @param attributes the attributes to send
     * @param cache cache bit
     */
    public synchronized void notifyData ( final Variant value, final Map<String, Variant> attributes, final boolean cache )
    {
        final ItemListener listener = this.listener;
        if ( listener != null )
        {
            listener.dataChanged ( this, value, attributes, cache );
        }
    }
}

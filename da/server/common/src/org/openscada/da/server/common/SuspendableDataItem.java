/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2007 inavare GmbH (http://inavare.com)
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

/**
 * An interface for items that are interested if currently a listener is set or not.
 * <br/>
 * Note that this interface must be fed by someone. It is not automatically fed by the Hive
 * anymore since the item should take care itself of this state. See {@link DataItemBase} for
 * a class supporting this interface.
 * @author Jens Reimann &lt;jens.reimann@inavare.net&gt;
 * @see DataItemBase
 */
public interface SuspendableDataItem
{
    /**
     * Called when the listener is set from a valid listener to <code>null</code>
     *
     */
    public abstract void suspend ();
    
    /**
     * Called when the listener is set from <code>null</code> to a valid listener
     *
     */
    public abstract void wakeup ();
}

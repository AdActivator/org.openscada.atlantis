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

package org.openscada.da.server.common;

import java.util.Map;
import java.util.concurrent.Future;

import org.openscada.core.InvalidOperationException;
import org.openscada.core.NotConvertableException;
import org.openscada.core.NullValueException;
import org.openscada.core.OperationException;
import org.openscada.core.Variant;
import org.openscada.da.core.DataItemInformation;
import org.openscada.da.core.WriteAttributeResults;

public interface DataItem
{
    public DataItemInformation getInformation ();

    /**
     * The read operation of a data item.
     * @return The value read from the subsystem.
     * @throws InvalidOperationException Raised if "read" is not a valid operation for this item
     */
    public Future<Variant> readValue () throws InvalidOperationException;

    /**
     * The write operation of a data item.
     * @param value The value to write to the subsystem
     * @throws InvalidOperationException Raised if "write" is not a valid operation for this item
     * @throws NullValueException Raised if a null value was passed but the subsystem does not allow null values to be written
     * @throws NotConvertableException Raised if a value was passed that cannot be converted in a variant type suitable for the subsystem
     * @throws OperationException Raised if the value could not be written due to some subsystem error
     */
    public void writeValue ( Variant value ) throws InvalidOperationException, NullValueException, NotConvertableException, OperationException;

    public Map<String, Variant> getAttributes ();

    public WriteAttributeResults setAttributes ( Map<String, Variant> attributes );

    /**
     * Sets the listener for this item.
     * 
     * Set by the controller to which this item is registered. The item has to use the listener
     * provided.
     * 
     * @param listener The listener to use or null to disable notification
     * 
     */
    public void setListener ( ItemListener listener );
}

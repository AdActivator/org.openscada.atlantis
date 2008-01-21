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

package org.openscada.da.client;

import java.util.Map;

import org.openscada.core.OperationException;
import org.openscada.core.Variant;
import org.openscada.core.client.NoConnectionException;
import org.openscada.da.core.Location;
import org.openscada.da.core.WriteAttributeResults;
import org.openscada.da.core.browser.Entry;

/**
 * A DataAccess (DA) connection.
 * @author Jens Reimann &lt;jens.reimann@inavare.net&gt;
 *
 */
public interface Connection extends org.openscada.core.client.Connection
{
    /**
     * Browse a server folder for items. The operation will block until the result is available,
     * an error occurred or the connection failed.
     * @param path The path to browse
     * @return The entries of the server folder
     * @throws NoConnectionException Raised if there is currently no connect to the server
     * @throws OperationException Raised if the operation failed
     */
    public abstract Entry[] browse ( String[] path ) throws NoConnectionException, OperationException;
    
    /**
     * Browse a server folder for items. The operation will block until the result is available,
     * an error occurred, the timeout expired or the connection failed.
     * @param path The path to browse
     * @param timeout Timeout of the operation in milliseconds
     * @return The entries of the server folder
     * @throws NoConnectionException Raised if there is currently no connect to the server
     * @throws OperationException Raised if the operation failed
     */
    public abstract Entry[] browse ( String[] path, int timeout ) throws NoConnectionException, OperationException;
    
    /**
     * Browse a server folder for items. The operation will return immediately.
     * @param path The path to browse
     * @param callback The callback that shall receive notifications of the operation
     */
    public abstract void browse ( String[] path, BrowseOperationCallback callback );

    public abstract void write ( String itemName, Variant value ) throws NoConnectionException, OperationException;
    public abstract void write ( String itemName, Variant value, int timeout ) throws NoConnectionException, OperationException;
    public abstract void write ( String itemName, Variant value, WriteOperationCallback callback );

    public abstract WriteAttributeResults writeAttributes ( String itemId, Map<String, Variant> attributes ) throws NoConnectionException, OperationException;
    public abstract WriteAttributeResults writeAttributes ( String itemId, Map<String, Variant> attributes, int timeout ) throws NoConnectionException, OperationException;
    public abstract void writeAttributes ( String itemId, Map<String, Variant> attributes, WriteAttributeOperationCallback callback );

    public abstract void subscribeFolder ( Location location ) throws NoConnectionException, OperationException;
    public abstract void unsubscribeFolder ( Location location ) throws NoConnectionException, OperationException;
    /**
     * Set the listener for this location.
     * @param location The location for which to set the listener
     * @param listener The listener to set
     * @return The previous set listener or <code>null</code> if there was no previous listener
     */
    public abstract FolderListener setFolderListener ( Location location, FolderListener listener );

    public abstract void subscribeItem ( String itemId ) throws NoConnectionException, OperationException;
    public abstract void unsubscribeItem ( String itemId ) throws NoConnectionException, OperationException;
    /**
     * Set the listener for this item.
     * @param itemId The item for which to set the listener
     * @param listener The listener to set
     * @return The previous set listener or <code>null</code> if there was no previous listener
     */
    public abstract ItemUpdateListener setItemUpdateListener ( String itemId, ItemUpdateListener listener );
}

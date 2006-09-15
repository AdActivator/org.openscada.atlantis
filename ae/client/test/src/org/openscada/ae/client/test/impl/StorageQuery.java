/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006 inavare GmbH (http://inavare.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openscada.ae.client.test.impl;

import org.openscada.ae.core.QueryDescription;

public class StorageQuery
{
    private StorageConnection _connection = null;
    private QueryDescription _queryDescription  = null;
    
    public StorageQuery ()
    {
        super ();
    }
    
    public StorageQuery ( StorageConnection connection, QueryDescription queryDescription )
    {
        super ();
        _connection = connection;
        _queryDescription = queryDescription;
    }
    
    public StorageConnection getConnection ()
    {
        return _connection;
    }
    public void setConnection ( StorageConnection connection )
    {
        _connection = connection;
    }
    public QueryDescription getQueryDescription ()
    {
        return _queryDescription;
    }
    public void setQueryDescription ( QueryDescription queryDescription )
    {
        _queryDescription = queryDescription;
    }
}

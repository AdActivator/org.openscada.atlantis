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

package org.openscada.da.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.openscada.utils.lang.Immutable;
import org.openscada.utils.str.StringHelper;

@Immutable
public class Location
{
    private final String[] location;

    public Location ( final String... location )
    {
        this.location = location.clone ();
    }

    public Location ( final Location location )
    {
        this.location = location.location.clone ();
    }

    public Location ()
    {
        this.location = new String[0];
    }

    public Location ( final List<String> location )
    {
        this.location = location.toArray ( new String[location.size ()] );
    }

    public String[] asArray ()
    {
        return this.location.clone ();
    }

    /**
     * Returns the path elements in an unmodifiable list
     * @return the path elements as list
     */
    public List<String> asList ()
    {
        return Collections.unmodifiableList ( Arrays.asList ( this.location ) );
    }

    @Override
    public String toString ()
    {
        return toString ( "/" );
    }

    public String toString ( final String separator )
    {
        return separator + StringHelper.join ( this.location, separator );
    }

    @Override
    public int hashCode ()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + Arrays.hashCode ( this.location );
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
        final Location other = (Location)obj;
        if ( !Arrays.equals ( this.location, other.location ) )
        {
            return false;
        }
        return true;
    }

    public Stack<String> getPathStack ()
    {
        final Stack<String> stack = new Stack<String> ();

        for ( int i = this.location.length; i > 0; i-- )
        {
            stack.push ( this.location[i - 1] );
        }

        return stack;
    }
}

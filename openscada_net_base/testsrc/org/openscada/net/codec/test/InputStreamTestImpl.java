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

package org.openscada.net.codec.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openscada.net.codec.Protocol;

public class InputStreamTestImpl
{
    private List<BytePacketTestImpl> _pseudoStream = new ArrayList<BytePacketTestImpl> ();

    /**
     * 
     */
    public InputStreamTestImpl ()
    {
        super ();
    }

    public InputStreamTestImpl ( final Collection<BytePacketTestImpl> packetList )
    {
        this._pseudoStream = new ArrayList<BytePacketTestImpl> ( packetList );
    }

    public void clear ()
    {
        this._pseudoStream.clear ();
    }

    public void add ( final BytePacketTestImpl packet )
    {
        this._pseudoStream.add ( packet );
    }

    public void run ( final Protocol protocol )
    {
        BytePacketTestImpl packet;
        final Iterator<BytePacketTestImpl> i = this._pseudoStream.iterator ();
        while ( i.hasNext () )
        {
            packet = i.next ();
            packet.process ( protocol );
        }
    }
}

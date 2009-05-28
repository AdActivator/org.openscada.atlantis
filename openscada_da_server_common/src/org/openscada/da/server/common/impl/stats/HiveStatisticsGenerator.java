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

package org.openscada.da.server.common.impl.stats;

import org.openscada.core.Variant;
import org.openscada.da.core.server.Session;
import org.openscada.da.server.common.DataItem;
import org.openscada.da.server.common.impl.FutureWrapperItem;
import org.openscada.da.server.common.impl.SessionCommon;

public class HiveStatisticsGenerator implements HiveEventListener, Tickable
{
    protected CounterValue itemsValue = new CounterValue ();

    protected CounterValue sessionsValue = new CounterValue ();

    protected CounterValue valueWritesCounter = new CounterValue ();

    protected CounterValue attributeWritesCounter = new CounterValue ();

    protected CounterValue valueEventsCounter = new CounterValue ();

    protected CounterValue attributeEventsCounter = new CounterValue ();

    protected CounterValue futureWrapperCounter = new CounterValue ();

    public void itemRegistered ( final DataItem item )
    {
        this.itemsValue.add ( 1 );
        if ( item instanceof FutureWrapperItem )
        {
            this.futureWrapperCounter.add ( 1 );
        }
    }

    public void sessionCreated ( final SessionCommon session )
    {
        this.sessionsValue.add ( 1 );
    }

    public void sessionDestroyed ( final SessionCommon session )
    {
        this.sessionsValue.add ( -1 );
    }

    public void startWrite ( final Session session, final String itemName, final Variant value )
    {
        this.valueWritesCounter.add ( 1 );
    }

    public void startWriteAttributes ( final Session session, final String itemId, final int size )
    {
        this.attributeWritesCounter.add ( size );
    }

    public void attributesChanged ( final DataItem item, final int size )
    {
        this.attributeEventsCounter.add ( size );
    }

    public void valueChanged ( final DataItem item, final Variant variant, final boolean cache )
    {
        this.valueEventsCounter.add ( 1 );
    }

    public void tick ()
    {
        this.attributeWritesCounter.tick ();
        this.itemsValue.tick ();
        this.sessionsValue.tick ();
        this.valueWritesCounter.tick ();
        this.valueEventsCounter.tick ();
        this.attributeEventsCounter.tick ();
    }

    public void itemUnregistered ( final DataItem item )
    {
        this.itemsValue.add ( -1 );
        if ( item instanceof FutureWrapperItem )
        {
            this.futureWrapperCounter.add ( -1 );
        }
    }

}

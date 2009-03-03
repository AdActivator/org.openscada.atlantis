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

package org.openscada.da.server.common.chain.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscada.core.Variant;
import org.openscada.da.server.common.chain.DataItemInputChained;

public class TestInputChain
{
    protected ItemListenerTestImpl _listener = null;

    protected DataItemInputChained _dataItem = null;

    protected List<EventEntry> _expectedEvents = new LinkedList<EventEntry> ();

    @BeforeClass
    public static void setupLog4j ()
    {
        BasicConfigurator.configure ();
    }

    @Before
    public void init ()
    {
        this._listener = new ItemListenerTestImpl ();
        this._dataItem = new DataItemInputChained ( "test-id" );
        this._dataItem.setListener ( this._listener );
    }

    @Test
    public void testValueEvent () throws Exception
    {
        this._dataItem.updateData ( new Variant ( 1 ), null, null );
        addEvent ( new Variant ( 1 ), null );
        assertEvents ();
    }

    @Test
    public void testAttributeEvent () throws Exception
    {
        final Map<String, Variant> attributes = new HashMap<String, Variant> ();
        attributes.put ( "name", new Variant ( "value" ) );

        this._dataItem.updateData ( null, attributes, null );
        addEvent ( null, attributes );

        assertEvents ();
    }

    @Test
    public void testPreSetValue () throws Exception
    {
        this._dataItem.setListener ( null );
        this._dataItem.updateData ( new Variant ( true ), null, null );
        assertEvents ();

        addEvent ( new Variant ( true ), null );
        this._dataItem.setListener ( this._listener );
        assertEvents ();
    }

    @Test
    public void testPreSetAttributes () throws Exception
    {
        final Map<String, Variant> attributes = new HashMap<String, Variant> ();
        attributes.put ( "test", new Variant ( "test" ) );

        this._dataItem.setListener ( null );
        this._dataItem.updateData ( null, attributes, null );
        assertEvents ();

        addEvent ( null, attributes );
        this._dataItem.setListener ( this._listener );
        assertEvents ();
    }

    protected void assertEvents ()
    {
        this._listener.assertEquals ( this._expectedEvents );
    }

    protected void addEvent ( final Variant value, final Map<String, Variant> attributes )
    {
        Variant copyValue = null;
        if ( value != null )
        {
            copyValue = new Variant ( value );
        }
        HashMap<String, Variant> copyAttributes = null;
        if ( attributes != null )
        {
            copyAttributes = new HashMap<String, Variant> ( attributes );
        }
        this._expectedEvents.add ( new EventEntry ( this._dataItem, copyValue, copyAttributes ) );
    }

}

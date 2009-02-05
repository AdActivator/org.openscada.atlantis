/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2008 inavare GmbH (http://inavare.com)
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

package org.openscada.da.server.common.chain.item;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;

import org.openscada.core.Variant;
import org.openscada.da.core.WriteAttributeResults;
import org.openscada.da.server.common.HiveServiceRegistry;
import org.openscada.da.server.common.chain.BaseChainItemCommon;
import org.openscada.da.server.common.chain.VariantBinder;

/**
 * This class 
 * @author jens
 *
 */
public class ManualOverrideChainItem extends BaseChainItemCommon
{
    public static final String MANUAL_BASE = "org.openscada.da.manual";
    public static final String ORIGINAL_VALUE = MANUAL_BASE + ".value.original";
    public static final String ORIGINAL_TIMESTAMP = MANUAL_BASE + ".timestamp.original";
    public static final String MANUAL_ACTIVE = MANUAL_BASE + ".active";
    public static final String MANUAL_VALUE = MANUAL_BASE + ".value";
    public static final String MANUAL_TIMESTAMP = MANUAL_BASE + ".timestamp";
    public static final String MANUAL_USER = MANUAL_BASE + ".user";
    public static final String MANUAL_REASON = MANUAL_BASE + ".reason";

    private VariantBinder manualValue = new VariantBinder ( new Variant () );
    private VariantBinder manualReason = new VariantBinder ( new Variant () );
    private VariantBinder manualUser = new VariantBinder ( new Variant () );

    private Calendar manualTimestamp;

    public ManualOverrideChainItem ( HiveServiceRegistry serviceRegistry )
    {
        super ( serviceRegistry );

        addBinder ( MANUAL_VALUE, manualValue );
        addBinder ( MANUAL_REASON, manualReason );
        addBinder ( MANUAL_USER, manualUser );
        setReservedAttributes ( ORIGINAL_VALUE, MANUAL_ACTIVE );
    }

    /**
     * loading initial properties from the storage service
     */
    @Override
    protected void loadInitialProperties ()
    {
        // load all other properties
        super.loadInitialProperties ();

        // load the manual timestamp
        this.manualTimestamp = null;
        Map<String, Variant> properties = loadStoredValues ( new HashSet<String> ( Arrays.asList ( MANUAL_TIMESTAMP ) ) );
        if ( properties.containsKey ( MANUAL_TIMESTAMP ) )
        {
            Variant value = properties.get ( MANUAL_TIMESTAMP );
            if ( !value.isNull () )
            {
                this.manualTimestamp = Calendar.getInstance ();
                try
                {
                    this.manualTimestamp.setTimeInMillis ( value.asLong () );
                }
                catch ( Throwable e )
                {
                }
            }
        }
    }

    @Override
    public WriteAttributeResults setAttributes ( Map<String, Variant> attributes )
    {
        Variant value = attributes.get ( MANUAL_VALUE );
        if ( value != null )
        {
            if ( value.isNull () )
            {
                // if the value is set as Variant#NULL clear the timestamp
                manualTimestamp = null;
            }
            else
            {
                // we got a valid value
                manualTimestamp = Calendar.getInstance ();
            }
        }
        else if ( attributes.containsKey ( MANUAL_VALUE ) )
        {
            // if the value is set but as "null" then clear the timestamp
            manualTimestamp = null;
        }
        return super.setAttributes ( attributes );
    }

    @Override
    protected void performWriteBinders ( Map<String, Variant> attributes )
    {
        // if we got a timestamp, store it
        if ( manualTimestamp != null )
        {
            attributes.put ( MANUAL_TIMESTAMP, new Variant ( manualTimestamp.getTimeInMillis () ) );
        }
        super.performWriteBinders ( attributes );
    }

    public void process ( Variant value, Map<String, Variant> attributes )
    {
        attributes.put ( MANUAL_ACTIVE, null );
        attributes.put ( ORIGINAL_VALUE, null );
        attributes.put ( ORIGINAL_TIMESTAMP, null );
        attributes.put ( MANUAL_TIMESTAMP, null );

        if ( !manualValue.getValue ().isNull () )
        {
            attributes.put ( ORIGINAL_VALUE, new Variant ( value ) );
            value.setValue ( new Variant ( this.manualValue.getValue () ) );
            attributes.put ( MANUAL_ACTIVE, new Variant ( true ) );
            attributes.put ( MANUAL_TIMESTAMP, new Variant ( manualTimestamp.getTimeInMillis () ) );

            // if we have an original timestamp, replace it
            Variant originalTimestamp = attributes.get ( "timestamp" );
            if ( originalTimestamp != null )
            {
                attributes.put ( ORIGINAL_TIMESTAMP, new Variant ( originalTimestamp ) );
            }
            attributes.put ( "timestamp", new Variant ( manualTimestamp.getTimeInMillis () ) );
        }
        addAttributes ( attributes );
    }
}

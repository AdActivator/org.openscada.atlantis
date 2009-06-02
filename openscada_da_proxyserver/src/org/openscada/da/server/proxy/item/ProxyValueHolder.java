/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2009 inavare GmbH (http://inavare.com)
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

package org.openscada.da.server.proxy.item;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openscada.core.Variant;
import org.openscada.core.subscription.SubscriptionState;
import org.openscada.core.utils.AttributesHelper;
import org.openscada.da.client.DataItemValue;
import org.openscada.da.client.ItemUpdateListener;
import org.openscada.da.server.common.AttributeMode;
import org.openscada.da.server.proxy.connection.ProxySubConnectionId;
import org.openscada.da.server.proxy.utils.ProxyPrefixName;

/**
 * @author Juergen Rose &lt;juergen.rose@inavare.net&gt;
 *
 */
public class ProxyValueHolder extends ProxyItemSupport
{
    private static Logger logger = Logger.getLogger ( ProxyValueHolder.class );

    protected final Map<ProxySubConnectionId, DataItemValue> values = new HashMap<ProxySubConnectionId, DataItemValue> ();

    protected ItemUpdateListener listener = null;

    /**
     * @param currentConnection
     */
    public ProxyValueHolder ( final String separator, final ProxyPrefixName prefix, final ProxySubConnectionId currentConnection, final String itemId )
    {
        super ( separator, prefix, currentConnection, itemId );
    }

    @Override
    public void switchTo ( final ProxySubConnectionId newConnection )
    {
        synchronized ( this )
        {
            final DataItemValue oldData = getItemValue ( this.currentConnection );
            final DataItemValue newData = getItemValue ( newConnection );

            if ( !oldData.equals ( newData ) )
            {
                if ( newData.getValue () != null && !newData.getValue ().equals ( oldData.getValue () ) )
                {
                    this.listener.notifyDataChange ( newData.getValue (), newData.getAttributes (), true );
                }
                else if ( newData.getAttributes () != null && !newData.getAttributes ().equals ( oldData.getAttributes () ) )
                {
                    this.listener.notifyDataChange ( newData.getValue (), newData.getAttributes (), true );
                }
            }
            super.switchTo ( newConnection );
        }
    }

    /**
     * @param connection
     * @param value
     * @param attributes
     * @param mode
     */
    public void updateData ( final ProxySubConnectionId connection, final Variant value, final Map<String, Variant> attributes, final AttributeMode mode )
    {
        try
        {
            handleUpdateData ( connection, value, attributes, mode );
        }
        catch ( final Throwable e )
        {
            logger.error ( String.format ( "Failed to update data of item '%s'", this.proxyItemId ), e );
        }
    }

    /**
     * Wrap the event of data updates
     * @param connection the connection on which the even occurred
     * @param value the new value
     * @param attributes the new attributes
     * @param mode the attribute mode
     */
    private void handleUpdateData ( final ProxySubConnectionId connection, final Variant value, final Map<String, Variant> attributes, AttributeMode mode )
    {
        final DataItemValue div;
        final boolean doSend;

        synchronized ( this )
        {
            boolean changed = false;
            div = getItemValue ( connection );

            if ( value != null && !div.getValue ().equals ( value ) )
            {
                div.setValue ( new Variant ( value ) );
                changed = true;
            }
            if ( attributes != null )
            {
                if ( mode == null )
                {
                    mode = AttributeMode.UPDATE;
                }

                final Map<String, Variant> diff = new HashMap<String, Variant> ();
                if ( mode == AttributeMode.SET )
                {
                    AttributesHelper.set ( div.getAttributes (), attributes, diff );
                }
                else
                {
                    AttributesHelper.mergeAttributes ( div.getAttributes (), attributes, diff );
                }
                changed = changed || !diff.isEmpty ();
            }

            // check if we should send changes directly
            doSend = connection.equals ( this.currentConnection );
        }

        // now send outside of sync
        if ( doSend )
        {
            this.listener.notifyDataChange ( value, attributes, false );
        }
    }

    /**
     * must be called synchronized
     * @param id if the connection
     * @return the data item value for this connection
     */
    protected DataItemValue getItemValue ( final ProxySubConnectionId id )
    {
        DataItemValue div = this.values.get ( id );
        if ( div == null )
        {
            if ( logger.isDebugEnabled () )
            {
                logger.debug ( String.format ( "Creating item value for %s on connection %s", this.proxyItemId, id.getName () ) );
            }

            // if the value holder is not set up to know .. create one
            this.values.put ( id, div = new DataItemValue () );
        }
        return div;
    }

    /**
     * @return return current attribs
     */
    public Map<String, Variant> getAttributes ()
    {
        final DataItemValue div = this.values.get ( this.currentConnection );
        return div.getAttributes ();
    }

    /**
     * @return return current value
     */
    public Variant getValue ()
    {
        final DataItemValue div = this.values.get ( this.currentConnection );
        return div.getValue ();
    }

    /**
     * @param listener
     */
    public void setListener ( final ItemUpdateListener listener )
    {
        this.listener = listener;
    }

    /**
     * @param connection
     * @param subscriptionState
     * @param subscriptionError
     */
    public void updateSubscriptionState ( final ProxySubConnectionId connection, final SubscriptionState subscriptionState, final Throwable subscriptionError )
    {
        try
        {
            handleUpdateSubscriptionChange ( connection, subscriptionState, subscriptionError );
        }
        catch ( final Throwable e )
        {
            logger.error ( String.format ( "Failed to change subscription state of item '%s'", this.proxyItemId ), e );
        }
    }

    /**
     * Handle the actual subscription change and wrap the call
     * @param connection the connection that changed 
     * @param subscriptionState the new state
     * @param subscriptionError the optional error
     */
    private void handleUpdateSubscriptionChange ( final ProxySubConnectionId connection, final SubscriptionState subscriptionState, final Throwable subscriptionError )
    {
        final boolean doSend;

        synchronized ( this )
        {
            final DataItemValue div = getItemValue ( connection );
            div.setSubscriptionState ( subscriptionState );
            div.setSubscriptionError ( subscriptionError );
            doSend = connection.equals ( this.currentConnection );
        }

        if ( doSend )
        {
            this.listener.notifySubscriptionChange ( subscriptionState, subscriptionError );
        }
    }

    public DataItemValue getCurrentValue ()
    {
        return this.values.get ( this.currentConnection );
    }
}

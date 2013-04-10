/*
 * This file is part of the OpenSCADA project
 * 
 * Copyright (C) 2006-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassi.de)
 *
 * OpenSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.da.master.common.block;

import java.util.HashMap;
import java.util.Map;

import org.openscada.ae.Event.EventBuilder;
import org.openscada.ae.Event.Fields;
import org.openscada.ae.event.EventProcessor;
import org.openscada.ca.ConfigurationAdministrator;
import org.openscada.ca.ConfigurationDataHelper;
import org.openscada.core.OperationException;
import org.openscada.core.Variant;
import org.openscada.core.server.OperationParameters;
import org.openscada.da.client.DataItemValue;
import org.openscada.da.client.DataItemValue.Builder;
import org.openscada.da.core.WriteAttributeResults;
import org.openscada.da.master.MasterItem;
import org.openscada.da.master.WriteRequest;
import org.openscada.da.master.WriteRequestResult;
import org.openscada.da.master.common.AbstractCommonHandlerImpl;
import org.openscada.da.master.common.internal.Activator;
import org.openscada.sec.UserInformation;
import org.openscada.utils.osgi.pool.ObjectPoolTracker;
import org.osgi.util.tracker.ServiceTracker;

public class BlockHandlerImpl extends AbstractCommonHandlerImpl
{
    private boolean active = false;

    private String note = null;

    private final EventProcessor eventProcessor;

    private String user;

    private Long timestamp;

    private final Variant source;

    private final String attrBlocked;

    private final String attrActive;

    private final String attrUser;

    private final String attrNote;

    private final String attrTimestamp;

    public BlockHandlerImpl ( final String configurationId, final EventProcessor eventProcessor, final ObjectPoolTracker<MasterItem> poolTracker, final int priority, final ServiceTracker<ConfigurationAdministrator, ConfigurationAdministrator> caTracker )
    {
        super ( configurationId, poolTracker, priority, caTracker, BlockHandlerFactoryImpl.FACTORY_ID, BlockHandlerFactoryImpl.FACTORY_ID );
        this.source = Variant.valueOf ( configurationId );
        this.eventProcessor = eventProcessor;

        this.attrBlocked = getPrefixed ( "blocked", Activator.getStringInterner () ); //$NON-NLS-1$
        this.attrActive = getPrefixed ( "active", Activator.getStringInterner () ); //$NON-NLS-1$
        this.attrNote = getPrefixed ( "note", Activator.getStringInterner () ); //$NON-NLS-1$
        this.attrUser = getPrefixed ( "user", Activator.getStringInterner () ); //$NON-NLS-1$
        this.attrTimestamp = getPrefixed ( "timestamp", Activator.getStringInterner () ); //$NON-NLS-1$
    }

    @Override
    protected void processDataUpdate ( final Map<String, Object> context, final DataItemValue.Builder builder ) throws Exception
    {
        injectAttributes ( builder );
    }

    @Override
    public WriteRequestResult processWrite ( final WriteRequest request )
    {
        final boolean active = this.active;

        final WriteRequestResult result = super.processWrite ( request );

        if ( active )
        {
            final WriteRequest testRequest;
            if ( result != null )
            {
                testRequest = new WriteRequest ( result.getValue (), result.getAttributes (), request.getOperationParameters () );
            }
            else
            {
                testRequest = request;
            }

            if ( !testRequest.isEmpty () )
            {
                // if there is a remaining request
                publishEvent ( testRequest.getOperationParameters () != null ? testRequest.getOperationParameters ().getUserInformation () : UserInformation.ANONYMOUS, makeNote (), makeString ( testRequest ), "BLOCK" ); //$NON-NLS-1$
                return createBlockedResult ();
            }
        }
        return result;
    }

    private String makeNote ()
    {
        if ( this.note == null || this.note.isEmpty () )
        {
            return Messages.getString ( "BlockHandlerImpl.WriteErrorNoReason" ); //$NON-NLS-1$
        }
        else
        {
            return String.format ( Messages.getString ( "BlockHandlerImpl.WriteError" ), this.note ); //$NON-NLS-1$
        }
    }

    private String makeString ( final WriteRequest result )
    {
        final StringBuilder sb = new StringBuilder ();

        if ( result.getValue () != null )
        {
            sb.append ( result.getValue ().toString () );
        }
        if ( result.getAttributes () != null && !result.getAttributes ().isEmpty () )
        {
            for ( final Map.Entry<String, Variant> entry : result.getAttributes ().entrySet () )
            {
                if ( sb.length () > 0 )
                {
                    sb.append ( ", " ); //$NON-NLS-1$
                }
                sb.append ( entry.getKey () );
                sb.append ( "->" ); //$NON-NLS-1$
                sb.append ( entry.getValue () );
            }
        }
        return sb.toString ();
    }

    private WriteRequestResult createBlockedResult ()
    {
        return new WriteRequestResult ( new OperationException ( Messages.getString ( "BlockHandlerImpl.OperationException" ) ) ); //$NON-NLS-1$
    }

    @Override
    public synchronized void update ( final UserInformation userInformation, final Map<String, String> parameters ) throws Exception
    {
        super.update ( userInformation, parameters );

        final ConfigurationDataHelper cfg = new ConfigurationDataHelper ( parameters );
        this.note = updateValue ( userInformation, Messages.getString ( "BlockHandlerImpl.UpdateConfiguration.note" ), cfg.getString ( "note", null ), this.note ); //$NON-NLS-1$
        this.active = updateValue ( userInformation, Messages.getString ( "BlockHandlerImpl.UpdateConfiguration.active" ), cfg.getBoolean ( "active", false ), this.active ); //$NON-NLS-1$
        this.user = updateValue ( userInformation, Messages.getString ( "BlockHandlerImpl.UpdateConfiguration.user" ), cfg.getString ( "user", null ), this.user ); //$NON-NLS-1$
        this.timestamp = cfg.getLong ( "timestamp" ); //$NON-NLS-1$

        reprocess ();
    }

    protected <T> T updateValue ( final UserInformation userInformation, final String message, final T newValue, final T oldValue )
    {
        if ( newValue == oldValue )
        {
            return newValue;
        }
        if ( newValue != null )
        {
            if ( newValue.equals ( oldValue ) )
            {
                return newValue;
            }
        }

        publishEvent ( userInformation, message, newValue, "CFG" ); //$NON-NLS-1$ 
        return newValue;
    }

    protected void injectAttributes ( final Builder builder )
    {
        builder.setAttribute ( this.attrBlocked, Variant.valueOf ( this.active ) );
        builder.setAttribute ( this.attrActive, Variant.valueOf ( this.active ) );
        builder.setAttribute ( this.attrNote, Variant.valueOf ( this.note ) );
        builder.setAttribute ( this.attrUser, Variant.valueOf ( this.user ) );
        builder.setAttribute ( this.attrTimestamp, Variant.valueOf ( this.timestamp ) );
    }

    @Override
    protected WriteAttributeResults handleUpdate ( final Map<String, Variant> attributes, final OperationParameters operationParameters ) throws Exception
    {
        final Map<String, String> data = new HashMap<String, String> ();

        final Variant active = attributes.get ( "active" ); //$NON-NLS-1$
        final Variant factor = attributes.get ( "note" ); //$NON-NLS-1$

        if ( active != null && !active.isNull () )
        {
            data.put ( "active", active.asString () ); //$NON-NLS-1$
        }
        if ( factor != null && !factor.isNull () )
        {
            data.put ( "note", factor.asString () ); //$NON-NLS-1$
        }
        if ( operationParameters != null && operationParameters.getUserInformation () != null )
        {
            final String name = operationParameters.getUserInformation ().getName ();
            if ( name != null )
            {
                data.put ( "user", name ); //$NON-NLS-1$
            }
        }
        data.put ( "timestamp", "" + System.currentTimeMillis () ); //$NON-NLS-1$ //$NON-NLS-2$

        return updateConfiguration ( data, attributes, false, operationParameters );
    }

    protected void publishEvent ( final UserInformation user, final String message, final Object value, final String eventType )
    {
        final EventBuilder builder = createEvent ( user, message, value );
        if ( eventType != null )
        {
            builder.attribute ( Fields.EVENT_TYPE, Variant.valueOf ( eventType ) );
        }
        this.eventProcessor.publishEvent ( builder.build () );
    }

    protected EventBuilder createEvent ( final UserInformation user, final String message, final Object value )
    {
        final EventBuilder builder = createEventBuilder ();

        builder.attribute ( Fields.SOURCE, this.source );
        builder.attribute ( Fields.MONITOR_TYPE, "BLOCK" ); //$NON-NLS-1$

        if ( user != null && user.getName () != null )
        {
            builder.attribute ( Fields.ACTOR_TYPE, "USER" ); //$NON-NLS-1$
            builder.attribute ( Fields.ACTOR_NAME, user.getName () );
        }

        if ( message != null )
        {
            builder.attribute ( Fields.MESSAGE, message );
        }
        if ( value != null )
        {
            builder.attribute ( Fields.VALUE, value );
        }

        return builder;
    }
}

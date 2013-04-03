/*
 * This file is part of the openSCADA project
 * 
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassi.de)
 *
 * openSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * openSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with openSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.sec.audit.log.ae;

import java.util.Date;

import org.openscada.ae.Event;
import org.openscada.ae.Event.EventBuilder;
import org.openscada.ae.data.Severity;
import org.openscada.ae.event.EventService;
import org.openscada.sec.AuthorizationReply;
import org.openscada.sec.AuthorizationRequest;
import org.openscada.sec.audit.AuditLogService;
import org.openscada.sec.authz.AuthorizationContext;
import org.openscada.utils.ExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class AuditLogServiceImpl implements AuditLogService
{

    private final static Logger logger = LoggerFactory.getLogger ( AuditLogServiceImpl.class );

    private static final String PROP_ENABLE_DEBUG = "org.openscada.sec.audit.log.ae.debug";

    private static final String PROP_LOG_ALL = "org.openscada.sec.audit.log.ae.logAll";

    private EventService eventService;

    public void setEventService ( final EventService eventService )
    {
        this.eventService = eventService;
    }

    protected void log ( final Severity severity, final AuthorizationContext context, final String message, final AuthorizationRequest request, final AuthorizationReply reply, final Throwable error )
    {
        final EventBuilder evt = Event.create ();

        final Date now = new Date ();

        evt.entryTimestamp ( now );
        evt.sourceTimestamp ( now );

        evt.attribute ( Event.Fields.MESSAGE, message );
        if ( request != null )
        {
            evt.attribute ( Event.Fields.ACTOR_TYPE, request.getObjectType () );
            evt.attribute ( Event.Fields.ACTOR_NAME, request.getObjectId () );
            evt.attribute ( Event.Fields.ITEM, request.getAction () );

            evt.attribute ( Event.Fields.EVENT_TYPE, "SEC" );
            evt.attribute ( Event.Fields.MONITOR_TYPE, "AUDIT" );
        }

        if ( error != null )
        {
            evt.attribute ( "errorInformation", ExceptionHelper.formatted ( error ) );
        }

        if ( context != null )
        {
            evt.attribute ( "signature", context.getContext ().get ( "signature" ) );
            evt.attribute ( "failedSignature", context.getContext ().get ( "failedSignature" ) );
        }

        final Event event = evt.build ();

        logger.debug ( "Publishing event: {}", event );

        this.eventService.publishEvent ( event );
    }

    protected void log ( final Severity severity, final String message, final Throwable error )
    {
        log ( severity, null, message, null, null, error );
    }

    @Override
    public void info ( final String message, final Object... arguments )
    {
        log ( Severity.INFORMATION, MessageFormatter.arrayFormat ( message, arguments ).getMessage (), null );
    }

    @Override
    public void debug ( final String message, final Object... arguments )
    {
        if ( Boolean.getBoolean ( PROP_ENABLE_DEBUG ) )
        {
            log ( Severity.INFORMATION, MessageFormatter.arrayFormat ( message, arguments ).getMessage (), null );
        }
    }

    @Override
    public void info ( final String message, final Throwable e, final Object... arguments )
    {
        log ( Severity.INFORMATION, MessageFormatter.arrayFormat ( message, arguments ).getMessage (), e );
    }

    @Override
    public void debug ( final String message, final Throwable e, final Object... arguments )
    {
        if ( Boolean.getBoolean ( PROP_ENABLE_DEBUG ) )
        {
            log ( Severity.INFORMATION, MessageFormatter.arrayFormat ( message, arguments ).getMessage (), e );
        }
    }

    @Override
    public void authorizationRequested ( final AuthorizationRequest request )
    {
        if ( Boolean.getBoolean ( PROP_LOG_ALL ) )
        {
            log ( Severity.INFORMATION, "Authorization requested", null );
        }
    }

    @Override
    public void authorizationFailed ( final AuthorizationContext context, final AuthorizationRequest request, final Throwable error )
    {
        log ( Severity.ERROR, context, "Authorization failed", request, null, error );
    }

    @Override
    public void authorizationDone ( final AuthorizationContext context, final AuthorizationRequest request, final AuthorizationReply reply )
    {
        if ( reply.isGranted () && Boolean.getBoolean ( PROP_LOG_ALL ) )
        {
            log ( Severity.INFORMATION, context, "Authorization granted", request, reply, null );
        }
        else
        {
            log ( Severity.WARNING, context, "Authorization rejected", request, reply, null );
        }
    }

}

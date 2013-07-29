/*
 * This file is part of the OpenSCADA project
 * 
 * Copyright (C) 2006-2011 TH4 SYSTEMS GmbH (http://th4-systems.com)
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

package org.openscada.da.server.mqtt;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.openscada.ca.ConfigurationDataHelper;
import org.openscada.core.Variant;
import org.openscada.core.data.SubscriptionState;
import org.openscada.core.server.OperationParameters;
import org.openscada.da.client.DataItemValue;
import org.openscada.da.core.WriteResult;
import org.openscada.da.server.common.AttributeMode;
import org.openscada.da.server.common.chain.DataItemInputOutputChained;
import org.openscada.mqtt.MqttBroker;
import org.openscada.mqtt.TopicListener;
import org.openscada.utils.concurrent.InstantErrorFuture;
import org.openscada.utils.concurrent.InstantFuture;
import org.openscada.utils.concurrent.NotifyFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttDataitem extends DataItemInputOutputChained implements TopicListener, Comparable<MqttDataitem>
{
    private final static Logger logger = LoggerFactory.getLogger ( MqttDataitem.class );

    private String itemId;

    private String readTopic;

    private String writeTopic;

    private boolean isReadable = true;

    private boolean isWritable = false;

    private String format = "org.openscada.da.client.DataItemValue";

    private String brokerId;

    private MqttBroker broker;

    public MqttDataitem ( final String id, final Executor executor )
    {
        super ( id, executor );
    }

    @Override
    protected NotifyFuture<WriteResult> startWriteCalculatedValue ( final Variant value, final OperationParameters operationParameters )
    {
        if ( this.broker == null )
        {
            return new InstantErrorFuture<WriteResult> ( new RuntimeException ( "broker is not set" ) );
        }
        if ( this.broker.getClient () == null )
        {
            return new InstantErrorFuture<WriteResult> ( new RuntimeException ( "MQTT client is null" ) );
        }
        if ( !this.broker.getClient ().isConnected () )
        {
            return new InstantErrorFuture<WriteResult> ( new RuntimeException ( "MQTT client is not connected" ) );
        }
        if ( !this.isWritable )
        {
            return new InstantErrorFuture<WriteResult> ( new RuntimeException ( "item is not defined as writable" ) );
        }
        try
        {
            final MqttTopic topic = this.broker.getClient ().getTopic ( getWriteTopic () );
            final byte[] message = toMessage ( new DataItemValue ( value, this.getAttributes (), SubscriptionState.CONNECTED ) );
            logger.trace ( "try to write message '{}' to topic '{}'", message, topic );
            topic.publish ( message, 0, true );
            return new InstantFuture<WriteResult> ( WriteResult.OK );
        }
        catch ( final MqttException e )
        {
            return new InstantErrorFuture<WriteResult> ( e );
        }
        catch ( final UnsupportedEncodingException e )
        {
            return new InstantErrorFuture<WriteResult> ( e );
        }
    }

    private byte[] toMessage ( final DataItemValue value ) throws UnsupportedEncodingException
    {
        if ( this.format.equals ( "org.openscada.da.client.DataItemValue" ) )
        {
            return GsonUtil.gsonDataItemValue.toJson ( value ).getBytes ( "UTF-8" );
        }
        else if ( this.format.equals ( "mihini" ) )
        {
            // FIXME: implement me
        }
        return value.getValue ().asString ( "" ).getBytes ( "UTF-8" );
    }

    private DataItemValue toValue ( final byte[] message ) throws Exception
    {
        if ( this.format.equals ( "org.openscada.da.client.DataItemValue" ) )
        {
            return GsonUtil.gsonDataItemValue.fromJson ( new String ( message, "UTF-8" ), DataItemValue.class );
        }
        else if ( this.format.equals ( "mihini" ) )
        {
            // FIXME: implement me
        }
        return new DataItemValue ( Variant.valueOf ( new String ( message, "UTF-8" ) ), Collections.<String, Variant> emptyMap (), SubscriptionState.CONNECTED );
    }

    private String getWriteTopic ()
    {
        if ( this.writeTopic != null )
        {
            return this.writeTopic;
        }
        // use the same topic for read/write
        return this.broker.getItemToTopicConverter ().convert ( this.itemId, false );
    }

    private String getReadTopic ()
    {
        if ( this.readTopic != null )
        {
            return this.readTopic;
        }
        // use the same topic for read/write
        return this.broker.getItemToTopicConverter ().convert ( this.itemId, false );
    }

    public void setBroker ( final MqttBroker broker )
    {
        this.broker = broker;
        this.broker.addListener ( getReadTopic (), this );
    }

    public void unsetBroker ()
    {
        logger.trace ( "broker unset" );
        connectionLost ( new RuntimeException ( "broker not set" ) );
        this.broker = null;
    }

    public String getBrokerId ()
    {
        return this.brokerId;
    }

    public boolean isBrokerSet ()
    {
        return this.broker != null;
    }

    public void update ( final Map<String, String> parameters )
    {
        final ConfigurationDataHelper cfg = new ConfigurationDataHelper ( parameters );
        this.itemId = cfg.getStringChecked ( "item.id", "'item.id' has to be set" );
        this.brokerId = cfg.getStringChecked ( "broker.id", "'broker.id' has to be set" );
        this.format = cfg.getString ( "format", "org.openscada.da.client.DataItemValue" );
        this.readTopic = cfg.getString ( "readTopic" );
        this.writeTopic = cfg.getString ( "writeTopic" );
        this.isReadable = cfg.getBoolean ( "readable", true );
        this.isWritable = cfg.getBoolean ( "writable", false );
    }

    @Override
    public void update ( final byte[] payload, final boolean cached )
    {
        if ( this.isReadable )
        {
            try
            {
                final DataItemValue div = toValue ( payload );
                final Map<String, Variant> attributes = new HashMap<> ( div.getAttributes () );
                attributes.remove ( "org.openscada.da.server.mqtt.error" );
                attributes.remove ( "org.openscada.da.server.mqtt.error.string" );
                this.updateData ( div.getValue (), attributes, AttributeMode.SET );
            }
            catch ( final Exception e )
            {
                final Map<String, Variant> errorAttributes = new HashMap<> ();
                errorAttributes.put ( "org.openscada.da.server.mqtt.error", Variant.valueOf ( true ) );
                errorAttributes.put ( "org.openscada.da.server.mqtt.error.string", Variant.valueOf ( e.getMessage () ) );
                this.updateData ( null, errorAttributes, AttributeMode.UPDATE );
            }
        }
        else
        {
            logger.warn ( "got message {} for topic {}, but item {} is not defined as readable", new Object[] { payload, getReadTopic (), this.itemId } );
        }
    }

    @Override
    public void connectionLost ( final Throwable th )
    {
        final Map<String, Variant> errorAttributes = new HashMap<> ();
        errorAttributes.put ( "org.openscada.da.server.mqtt.error", Variant.valueOf ( true ) );
        errorAttributes.put ( "org.openscada.da.server.mqtt.error.string", Variant.valueOf ( "connection to MQTT server lost: " + th.getMessage () ) );
        this.updateData ( null, errorAttributes, AttributeMode.UPDATE );
    }

    @Override
    public int compareTo ( final MqttDataitem o )
    {
        final StringBuilder sba = new StringBuilder ();
        final StringBuilder sbb = new StringBuilder ();
        sba.append ( this.brokerId );
        sba.append ( this.itemId );
        sba.append ( this.readTopic );
        sba.append ( this.writeTopic );
        sbb.append ( o.brokerId );
        sbb.append ( o.itemId );
        sbb.append ( o.readTopic );
        sbb.append ( o.writeTopic );
        return sba.toString ().compareTo ( sbb.toString () );
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = ( prime * result ) + ( ( this.brokerId == null ) ? 0 : this.brokerId.hashCode () );
        result = ( prime * result ) + ( ( this.itemId == null ) ? 0 : this.itemId.hashCode () );
        result = ( prime * result ) + ( ( this.readTopic == null ) ? 0 : this.readTopic.hashCode () );
        result = ( prime * result ) + ( ( this.writeTopic == null ) ? 0 : this.writeTopic.hashCode () );
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
        final MqttDataitem other = (MqttDataitem)obj;
        if ( this.brokerId == null )
        {
            if ( other.brokerId != null )
            {
                return false;
            }
        }
        else if ( !this.brokerId.equals ( other.brokerId ) )
        {
            return false;
        }
        if ( this.itemId == null )
        {
            if ( other.itemId != null )
            {
                return false;
            }
        }
        else if ( !this.itemId.equals ( other.itemId ) )
        {
            return false;
        }
        if ( this.readTopic == null )
        {
            if ( other.readTopic != null )
            {
                return false;
            }
        }
        else if ( !this.readTopic.equals ( other.readTopic ) )
        {
            return false;
        }
        if ( this.writeTopic == null )
        {
            if ( other.writeTopic != null )
            {
                return false;
            }
        }
        else if ( !this.writeTopic.equals ( other.writeTopic ) )
        {
            return false;
        }
        return true;
    }
}

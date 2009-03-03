package org.openscada.core.client.net;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.openscada.core.ConnectionInformation;
import org.openscada.core.net.MessageHelper;
import org.openscada.net.base.MessageStateListener;
import org.openscada.net.base.data.Message;

public abstract class SessionConnectionBase extends ConnectionBase
{
    public static final String SESSION_CLIENT_VERSION = "client-version";

    private static Logger logger = Logger.getLogger ( SessionConnectionBase.class );

    public SessionConnectionBase ( final ConnectionInformation connectionInformation )
    {
        super ( connectionInformation );
    }

    public abstract String getRequiredVersion ();

    @Override
    protected void onConnectionEstablished ()
    {
        requestSession ();
    }

    protected void requestSession ()
    {
        final Properties props = new Properties ();
        props.setProperty ( SESSION_CLIENT_VERSION, getRequiredVersion () );

        this.messenger.sendMessage ( MessageHelper.createSession ( props ), new MessageStateListener () {

            public void messageReply ( final Message message )
            {
                processSessionReply ( message );
            }

            public void messageTimedOut ()
            {
                disconnected ( new OperationTimedOutException ().fillInStackTrace () );
            }
        }, getMessageTimeout () );
    }

    protected void processSessionReply ( final Message message )
    {
        logger.debug ( "Got session reply!" );

        if ( message.getValues ().containsKey ( Message.FIELD_ERROR_INFO ) )
        {
            final String errorInfo = message.getValues ().get ( Message.FIELD_ERROR_INFO ).toString ();
            disconnected ( new DisconnectReason ( "Failed to create session: " + errorInfo ) );
        }
        else if ( message.getCommandCode () != Message.CC_ACK )
        {
            disconnected ( new DisconnectReason ( "Received an invalid reply when requesting session" ) );
        }
        else
        {
            setBound ();
        }
    }

}

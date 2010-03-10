package org.openscada.ae.client;

import java.util.HashMap;
import java.util.Map;

import org.openscada.ae.client.internal.MonitorSyncController;
import org.openscada.core.client.ConnectionState;
import org.openscada.core.client.ConnectionStateListener;

public class MonitorManager implements ConnectionStateListener
{
    private final Connection connection;

    private boolean connected;

    private final Map<String, MonitorSyncController> monitorListeners = new HashMap<String, MonitorSyncController> ();

    public MonitorManager ( final Connection connection )
    {
        super ();
        if ( connection == null )
        {
            throw new IllegalArgumentException ( "connection is null" );
        }
        this.connection = connection;

        synchronized ( this )
        {
            this.connection.addConnectionStateListener ( this );
            this.connected = this.connection.getState () == ConnectionState.BOUND;
        }
    }

    public void stateChange ( final org.openscada.core.client.Connection connection, final ConnectionState state, final Throwable error )
    {
        switch ( state )
        {
        case BOUND:
            if ( !this.connected )
            {
                this.connected = true;
            }
            break;
        case CLOSED:
            for ( MonitorSyncController controller : this.monitorListeners.values () )
            {
                controller.dispose ();
            }
            this.monitorListeners.clear ();
        default:
            if ( this.connected )
            {
                this.connected = false;
            }
            break;
        }
    }

    public synchronized void addMonitorListener ( final String id, final ConditionListener listener )
    {
        MonitorSyncController monitorSyncController = this.monitorListeners.get ( id );
        if ( monitorSyncController == null )
        {
            monitorSyncController = new MonitorSyncController ( this.connection, id );
            this.monitorListeners.put ( id, monitorSyncController );
        }
        monitorSyncController.addListener ( listener );
    }

    public synchronized void removeMonitorListener ( final String id, final ConditionListener listener )
    {
        MonitorSyncController monitorSyncController = this.monitorListeners.get ( id );
        if ( monitorSyncController == null )
        {
            return;
        }
        monitorSyncController.removeListener ( listener );
    }

    public boolean isConnected ()
    {
        return this.connected;
    }
}

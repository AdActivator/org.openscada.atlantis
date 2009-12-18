package org.openscada.ae.monitor.dataitem;

import java.util.Date;

import org.openscada.ae.event.EventProcessor;
import org.openscada.da.client.DataItemValue;
import org.openscada.da.client.DataItemValue.Builder;
import org.openscada.utils.osgi.pool.ObjectPoolTracker;

public abstract class AbstractBooleanMonitor extends AbstractDataItemMonitor
{
    protected Boolean value;

    protected Date timestamp;

    public AbstractBooleanMonitor ( final ObjectPoolTracker poolTracker, final EventProcessor eventProcessor, final String id, final String prefix, final String defaultMonitorType )
    {
        super ( poolTracker, eventProcessor, id, prefix, defaultMonitorType );
    }

    protected abstract void update ();

    @Override
    protected void performDataUpdate ( final Builder builder )
    {
        final DataItemValue value = builder.build ();
        this.timestamp = toTimestamp ( value );
        if ( value == null || !value.isConnected () || value.isError () || value.getValue ().isNull () )
        {
            this.value = null;
        }
        else
        {
            this.value = value.getValue ().asBoolean ();
        }

        update ();
    }

}
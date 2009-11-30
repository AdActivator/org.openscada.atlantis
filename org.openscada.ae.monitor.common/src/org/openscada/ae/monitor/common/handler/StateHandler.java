package org.openscada.ae.monitor.common.handler;

import java.util.Date;

import org.openscada.ae.ConditionStatusInformation;
import org.openscada.core.Variant;

public interface StateHandler
{
    public void akn ( String aknUser, Date aknTimestamp );

    public void enable ();

    public void disable ();

    public void ok ( Variant value, Date timestamp );

    public void fail ( Variant value, Date timestamp );

    public void unsafe ();

    public void requireAkn ();

    public void ignoreAkn ();

    public ConditionStatusInformation getState ();

    public void activate ();

    public void deactivate ();
}

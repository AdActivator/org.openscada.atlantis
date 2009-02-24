/**
 * 
 */
package org.openscada.da.server.proxy;

import java.util.concurrent.Callable;

import org.openscada.core.Variant;

final class Writer implements Callable<Object>
{
    private final ProxySubConnectionId connectionId;

    private final ProxyValueHolder pvh;

    Writer ( final ProxySubConnectionId connectionId, final ProxyValueHolder pvh )
    {
        this.connectionId = connectionId;
        this.pvh = pvh;
    }

    public Object call () throws Exception
    {
        try
        {
            for ( int i = 0; i < ApplicationRunner1.getCount (); i++ )
            {
                ApplicationRunner1.operations++;
                this.pvh.updateData ( this.connectionId, new Variant ( i ), null, null );
            }
            this.pvh.updateData ( this.connectionId, new Variant ( "complete" ), null, null );
        }
        catch ( final Throwable e )
        {
            e.printStackTrace ();
        }

        return null;
    }
}
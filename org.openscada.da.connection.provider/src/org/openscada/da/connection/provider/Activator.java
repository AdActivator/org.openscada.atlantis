package org.openscada.da.connection.provider;

import org.openscada.core.connection.provider.AbstractConnectionProvider;
import org.openscada.da.connection.provider.internal.ConnectionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{

    private AbstractConnectionProvider connectionProvider;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start ( final BundleContext context ) throws Exception
    {
        this.connectionProvider = new ConnectionProvider ( context );
        this.connectionProvider.start ();
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop ( final BundleContext context ) throws Exception
    {
        this.connectionProvider.stop ();
        this.connectionProvider = null;
    }

}

package org.openscada.da.datasource.ds;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openscada.ca.ConfigurationAdministrator;
import org.openscada.ca.ConfigurationFactory;
import org.openscada.ds.DataNodeTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class Activator implements BundleActivator
{

    private ExecutorService executor;

    private DataStoreSourceFactory factory;

    private DataNodeTracker dataNodeTracker;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start ( final BundleContext context ) throws Exception
    {
        this.executor = Executors.newSingleThreadExecutor ();

        this.dataNodeTracker = new DataNodeTracker ( context );

        this.factory = new DataStoreSourceFactory ( context, this.executor, this.dataNodeTracker );

        final Dictionary<String, String> properties = new Hashtable<String, String> ();
        properties.put ( Constants.SERVICE_DESCRIPTION, "A data store data source" );
        properties.put ( Constants.SERVICE_VENDOR, "inavare GmbH" );
        properties.put ( ConfigurationAdministrator.FACTORY_ID, context.getBundle ().getSymbolicName () );

        context.registerService ( ConfigurationFactory.class.getName (), this.factory, properties );

        this.dataNodeTracker.open ();
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop ( final BundleContext context ) throws Exception
    {
        this.factory.dispose ();
        this.executor.shutdown ();

        this.dataNodeTracker.close ();
    }

}

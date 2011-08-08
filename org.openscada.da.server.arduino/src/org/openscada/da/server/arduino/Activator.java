package org.openscada.da.server.arduino;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openscada.ca.ConfigurationAdministrator;
import org.openscada.ca.ConfigurationFactory;
import org.openscada.da.server.arduino.factory.ConfigurationFactoryImpl;
import org.openscada.da.server.common.DataItem;
import org.openscada.utils.concurrent.NamedThreadFactory;
import org.openscada.utils.osgi.pool.ObjectPoolHelper;
import org.openscada.utils.osgi.pool.ObjectPoolImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator
{

    private static BundleContext context;

    private ObjectPoolImpl itemPool;

    private ServiceRegistration itemPoolHandle;

    private ExecutorService executor;

    private ConfigurationFactoryImpl service;

    static BundleContext getContext ()
    {
        return context;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start ( final BundleContext bundleContext ) throws Exception
    {
        Activator.context = bundleContext;

        this.itemPool = new ObjectPoolImpl ();

        this.itemPoolHandle = ObjectPoolHelper.registerObjectPool ( context, this.itemPool, DataItem.class.getName () );

        this.executor = Executors.newSingleThreadExecutor ( new NamedThreadFactory ( context.getBundle ().getSymbolicName () ) );

        this.service = new ConfigurationFactoryImpl ( context, this.itemPool, this.executor );

        {
            final Dictionary<String, Object> properties = new Hashtable<String, Object> ();
            properties.put ( ConfigurationAdministrator.FACTORY_ID, "org.openscada.da.server.arduino.device" );
            properties.put ( Constants.SERVICE_DESCRIPTION, "Arduino OpenSCADA Device" );
            context.registerService ( ConfigurationFactory.class.getName (), this.service, properties );
        }

    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( final BundleContext bundleContext ) throws Exception
    {
        this.itemPoolHandle.unregister ();
        this.itemPool.dispose ();

        this.service.dispose ();

        this.executor.shutdown ();
        this.executor = null;

        Activator.context = null;
    }

}

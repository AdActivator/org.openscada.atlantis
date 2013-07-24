package org.openscada.mqtt;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openscada.ca.ConfigurationAdministrator;
import org.openscada.ca.ConfigurationFactory;
import org.openscada.utils.concurrent.NamedThreadFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator
{

    private static BundleContext context;

    private ExecutorService executor;

    private MqttBrokerFactory mqttBrokerFactory;

    private ServiceRegistration<ConfigurationFactory> mqttBrokerFactoryRegistration;

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

        this.executor = Executors.newSingleThreadExecutor ( new NamedThreadFactory ( getClass ().getName () ) );

        this.mqttBrokerFactory = new MqttBrokerFactory ( context, this.executor );

        final Dictionary<String, String> properties = new Hashtable<String, String> ( 3 );
        properties.put ( Constants.SERVICE_DESCRIPTION, "A factory which wraps a MQTT connection" );
        properties.put ( Constants.SERVICE_VENDOR, "IBH SYSTEMS GmbH" );
        properties.put ( ConfigurationAdministrator.FACTORY_ID, context.getBundle ().getSymbolicName () );

        this.mqttBrokerFactoryRegistration = context.registerService ( ConfigurationFactory.class, this.mqttBrokerFactory, properties );
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( final BundleContext bundleContext ) throws Exception
    {
        if ( this.mqttBrokerFactoryRegistration != null )
        {
            this.mqttBrokerFactoryRegistration.unregister ();
        }
        this.mqttBrokerFactoryRegistration = null;
        if ( this.mqttBrokerFactory != null )
        {
            this.mqttBrokerFactory.dispose ();
        }
        this.mqttBrokerFactory = null;
        if ( this.executor != null )
        {
            this.executor.shutdownNow ();
        }
        this.executor = null;

        Activator.context = null;
    }
}

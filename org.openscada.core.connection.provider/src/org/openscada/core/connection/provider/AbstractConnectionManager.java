package org.openscada.core.connection.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openscada.core.ConnectionInformation;
import org.openscada.core.client.DriverFactory;
import org.openscada.utils.osgi.FilterUtil;
import org.openscada.utils.osgi.SingleServiceListener;
import org.openscada.utils.osgi.SingleServiceTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConnectionManager implements SingleServiceListener
{

    protected static final Logger logger = LoggerFactory.getLogger ( AbstractConnectionManager.class );

    private final ConnectionInformation connectionInformation;

    private final BundleContext context;

    private final String connectionId;

    private SingleServiceTracker tracker;

    private AbstractConnectionService connection;

    private ServiceRegistration serviceReg;

    private DriverFactory factory;

    private final Integer autoReconnectDelay;

    private final boolean initialOpen;

    private final Set<ConnectionRequest> requests = new HashSet<ConnectionRequest> ();

    public AbstractConnectionManager ( final BundleContext context, final ConnectionInformation connectionInformation, final String connectionId, final Integer autoReconnectDelay, final boolean initialOpen )
    {
        super ();
        this.context = context;

        this.connectionInformation = connectionInformation;
        this.connectionId = connectionId;
        this.autoReconnectDelay = autoReconnectDelay;
        this.initialOpen = initialOpen;

        final String interfaceName = this.connectionInformation.getInterface ();
        final String driverName = this.connectionInformation.getDriver ();

        Filter filter;
        try
        {
            final Map<String, String> parameters = new HashMap<String, String> ();
            parameters.put ( DriverFactory.INTERFACE_NAME, interfaceName );
            parameters.put ( DriverFactory.DRIVER_NAME, driverName );
            filter = FilterUtil.createAndFilter ( DriverFactory.class.getName (), parameters );
            logger.debug ( "Created filter: {}", filter );
        }
        catch ( final InvalidSyntaxException e )
        {
            filter = null;
            logger.warn ( "Failed to create filter", e );
        }

        if ( filter != null )
        {
            this.tracker = new SingleServiceTracker ( this.context, filter, this );
            this.tracker.open ();
        }
        else
        {
            this.tracker = null;
        }
    }

    public Integer getAutoReconnectDelay ()
    {
        return this.autoReconnectDelay;
    }

    public boolean isInitialOpen ()
    {
        return this.initialOpen;
    }

    public DriverFactory getFactory ()
    {
        return this.factory;
    }

    public ConnectionInformation getConnectionInformation ()
    {
        return this.connectionInformation;
    }

    /**
     * Dispose the tracker and the connection
     */
    public void dispose ()
    {
        if ( this.tracker != null )
        {
            this.tracker.close ();
            this.tracker = null;
        }
        disposeConnection ();
    }

    public void update ( final ConnectionInformation connectionInformation )
    {
        // FIXME: implement
    }

    public void serviceChange ( final ServiceReference reference, final Object factory )
    {
        logger.info ( "Service changed: {} / {} ({}:{})", new Object[] { reference, factory, this.connectionInformation.getInterface (), this.connectionInformation.getDriver () } );

        disposeConnection ();

        this.factory = (DriverFactory)factory;

        if ( this.factory != null )
        {
            createAndRegisterConnection ();
        }
    }

    protected abstract AbstractConnectionService createConnection ();

    /**
     * Create a new connection and register it with OSGi
     */
    private void createAndRegisterConnection ()
    {
        logger.debug ( "Creating new connection" );

        this.connection = createConnection ();

        if ( this.connection != null )
        {
            if ( this.initialOpen )
            {
                logger.debug ( "Initially open" );
                this.connection.connect ();
            }

            final Hashtable<String, String> properties = new Hashtable<String, String> ();
            if ( this.connectionId != null )
            {
                properties.put ( Constants.SERVICE_PID, this.connectionId );
            }
            properties.put ( DriverFactory.INTERFACE_NAME, this.connectionInformation.getInterface () );
            properties.put ( DriverFactory.DRIVER_NAME, this.connectionInformation.getDriver () );
            properties.put ( ConnectionService.CONNECTION_URI, this.connectionInformation.toString () );

            logger.info ( "Registered new connection service: " + properties );
            this.serviceReg = this.context.registerService ( new String[] { ConnectionService.class.getName (), org.openscada.core.connection.provider.ConnectionService.class.getName () }, this.connection, properties );
        }
    }

    /**
     * Revoke the service registration and dispose the service
     * <p>
     * Does nothing if there is no current registration
     */
    protected void disposeConnection ()
    {
        if ( this.serviceReg != null )
        {
            logger.info ( "We got an old service ... dispose" );
            final AbstractConnectionService connection = this.connection;
            this.connection = null;

            this.serviceReg.unregister ();
            this.serviceReg = null;

            // now dispose the connection
            connection.dispose ();
        }
    }

    public void addRequest ( final ConnectionRequest request )
    {
        this.requests.add ( request );
    }

    public void removeRequest ( final ConnectionRequest request )
    {
        this.requests.remove ( request );
    }

    public boolean isIdle ()
    {
        return this.requests.isEmpty ();
    }

}
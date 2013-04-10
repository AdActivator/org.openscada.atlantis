/*
 * This file is part of the openSCADA project
 * 
 * Copyright (C) 2013 Jürgen Rose (cptmauli@googlemail.com)
 *
 * openSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * openSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with openSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.ae.server.storage.postgres.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.openscada.ae.server.storage.Storage;
import org.openscada.utils.concurrent.NamedThreadFactory;
import org.openscada.utils.interner.InternerHelper;
import org.openscada.utils.osgi.SingleServiceListener;
import org.openscada.utils.osgi.SingleServiceTracker;
import org.openscada.utils.osgi.jdbc.DataSourceHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Interner;

public class Activator implements BundleActivator
{
    private static final Logger logger = LoggerFactory.getLogger ( Activator.class );

    private static BundleContext context;

    private JdbcStorage jdbcStorage;

    private ServiceRegistration<?> jdbcStorageHandle;

    private SingleServiceTracker<DataSourceFactory> dataSouceFactoryTracker;

    private Interner<String> stringInterner;

    private ScheduledExecutorService scheduler;

    static BundleContext getContext ()
    {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start ( final BundleContext bundleContext ) throws Exception
    {
        Activator.context = bundleContext;

        this.stringInterner = InternerHelper.makeInterner ( "org.openscada.ae.monitor.dataitem.stringInternerType", "weak" );

        final String driver = DataSourceHelper.getDriver ( "org.openscada.ae.server.storage.jdbc.driver", DataSourceHelper.DEFAULT_PREFIX );

        final Filter filter = context.createFilter ( "(&(objectClass=" + DataSourceFactory.class.getName () + ")(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=" + driver + "))" );
        this.dataSouceFactoryTracker = new SingleServiceTracker<DataSourceFactory> ( bundleContext, filter, new SingleServiceListener<DataSourceFactory> () {
            @Override
            public void serviceChange ( final ServiceReference<DataSourceFactory> reference, final DataSourceFactory dsf )
            {
                try
                {
                    deactivate ();
                }
                catch ( final Exception e )
                {
                    logger.error ( "an error occured on deactivating ae postgre storage", e );
                }
                if ( dsf != null )
                {
                    try
                    {
                        activate ( dsf );
                    }
                    catch ( final Exception e )
                    {
                        logger.error ( "an error occured on activating ae postgres storage", e );
                    }
                }
            }
        } );
        this.dataSouceFactoryTracker.open ();
    }

    private void activate ( final DataSourceFactory dataSourceFactory ) throws Exception
    {
        this.scheduler = Executors.newSingleThreadScheduledExecutor ( new NamedThreadFactory ( "org.openscada.ae.server.storage.postgresql/ScheduledExecutor" ) );

        final Properties dbProperties = DataSourceHelper.getDataSourceProperties ( "org.openscada.ae.server.storage.jdbc", DataSourceHelper.DEFAULT_PREFIX );

        final String schema = getSchema ();
        final String instance = getInstance ();
        this.jdbcStorage = new JdbcStorage ( dataSourceFactory, this.scheduler, dbProperties, DataSourceHelper.isConnectionPool ( "org.openscada.ae.server.storage.jdbc", DataSourceHelper.DEFAULT_PREFIX, false ), schema, instance );
        this.jdbcStorage.start ();

        final Dictionary<String, Object> properties = new Hashtable<String, Object> ( 2 );
        properties.put ( Constants.SERVICE_DESCRIPTION, "PostgreSQL specific JDBC implementation for org.openscada.ae.server.storage.Storage" );
        properties.put ( Constants.SERVICE_VENDOR, "IBH SYSTEMS GmbH" );
        this.jdbcStorageHandle = context.registerService ( new String[] { JdbcStorage.class.getName (), Storage.class.getName () }, this.jdbcStorage, properties );
    }

    private String getSchema ()
    {
        if ( !System.getProperty ( "org.openscada.ae.server.storage.jdbc.schema", "" ).trim ().isEmpty () )
        {
            return System.getProperty ( "org.openscada.ae.server.storage.jdbc.schema" ) + ".";
        }
        return "";
    }

    private String getInstance ()
    {
        return System.getProperty ( "org.openscada.ae.server.storage.jdbc.instance", "default" );
    }

    private void deactivate () throws Exception
    {
        if ( this.scheduler != null )
        {
            this.scheduler.shutdownNow ();
        }
        if ( this.jdbcStorageHandle != null )
        {
            this.jdbcStorageHandle.unregister ();
            this.jdbcStorageHandle = null;
        }
        if ( this.jdbcStorage != null )
        {
            this.jdbcStorage.dispose ();
            this.jdbcStorage = null;
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( final BundleContext bundleContext ) throws Exception
    {
        if ( this.dataSouceFactoryTracker != null )
        {
            this.dataSouceFactoryTracker.close ();
        }
        deactivate (); // redundant, but if something happened with the tracker
                       // we are sure it is shut down
        Activator.context = null;
    }
}

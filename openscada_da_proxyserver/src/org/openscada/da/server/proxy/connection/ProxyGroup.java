/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2009 inavare GmbH (http://inavare.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openscada.da.server.proxy.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.openscada.core.InvalidOperationException;
import org.openscada.core.NotConvertableException;
import org.openscada.core.NullValueException;
import org.openscada.core.client.ConnectionStateListener;
import org.openscada.da.client.Connection;
import org.openscada.da.client.ItemManager;
import org.openscada.da.core.Location;
import org.openscada.da.server.browser.common.FolderCommon;
import org.openscada.da.server.common.configuration.ConfigurationError;
import org.openscada.da.server.common.factory.FactoryHelper;
import org.openscada.da.server.common.factory.FactoryTemplate;
import org.openscada.da.server.proxy.Hive;
import org.openscada.da.server.proxy.item.ProxyDataItem;
import org.openscada.da.server.proxy.item.ProxyItemUpdateListener;
import org.openscada.da.server.proxy.item.ProxyValueHolder;
import org.openscada.da.server.proxy.item.ProxyWriteHandler;
import org.openscada.da.server.proxy.item.ProxyWriteHandlerImpl;
import org.openscada.da.server.proxy.utils.ProxyPrefixName;
import org.openscada.da.server.proxy.utils.ProxySubConnectionId;
import org.openscada.da.server.proxy.utils.ProxyUtils;

/**
 * @author Juergen Rose &lt;juergen.rose@inavare.net&gt;
 *
 */
public class ProxyGroup
{
    private final static class ThreadFactoryImplementation implements ThreadFactory
    {
        private final String name;

        public ThreadFactoryImplementation ( final String name )
        {
            this.name = name;
        }

        public Thread newThread ( final Runnable r )
        {
            final Thread t = new Thread ( r, "ProxyItemListener/" + this.name );
            t.setDaemon ( true );
            return t;
        }
    }

    private static Executor defaultExecutor = new Executor () {
        public void execute ( final Runnable r )
        {
            r.run ();
        }
    };

    private static Logger logger = Logger.getLogger ( ProxyGroup.class );

    private final List<ConnectionStateListener> connectionStateListeners = Collections.synchronizedList ( new ArrayList<ConnectionStateListener> () );

    private ProxySubConnectionId currentConnection;

    private ProxyPrefixName prefix;

    private final Map<String, ProxyDataItem> registeredItems = Collections.synchronizedMap ( new HashMap<String, ProxyDataItem> () );

    private FolderCommon connectionFolder;

    private final Hive hive;

    private final Lock switchLock = new ReentrantLock ();

    private Executor itemListenerExecutor = defaultExecutor;

    private final Map<ProxySubConnectionId, ProxySubConnection> subConnections = new HashMap<ProxySubConnectionId, ProxySubConnection> ();

    private Integer wait = 0;

    private ProxyFolder proxyFolder;

    /**
     * @param hive
     * @param prefix
     */
    public ProxyGroup ( final Hive hive, final ProxyPrefixName prefix )
    {
        this.hive = hive;
        this.prefix = prefix;

        if ( Boolean.getBoolean ( "org.openscada.da.server.proxy.asyncListener" ) )
        {
            final ThreadFactory tf = new ThreadFactoryImplementation ( prefix.getName () );
            this.itemListenerExecutor = Executors.newSingleThreadExecutor ( tf );
        }
    }

    public void start ()
    {
        createProxyFolder ();
    }

    public void stop ()
    {
        destroyProxyFolder ();
    }

    /**
     * @return folder which holds items and connection information
     */
    public FolderCommon getConnectionFolder ()
    {
        return this.connectionFolder;
    }

    /**
     * @param connectionFolder
     */
    public void setConnectionFolder ( final FolderCommon connectionFolder )
    {
        this.connectionFolder = connectionFolder;
    }

    /**
     * @return the current selected connection
     */
    private Connection currentConnection ()
    {
        return currentSubConnection ().getConnection ();
    }

    /**
     * @return the current selected connection
     */
    private ProxySubConnection currentSubConnection ()
    {
        return this.subConnections.get ( this.currentConnection );
    }

    /**
     * @param connection
     * @param id
     * @param prefix
     * @param folderCommon 
     * @throws InvalidOperationException
     * @throws NullValueException
     * @throws NotConvertableException
     */
    public void addConnection ( final Connection connection, final String id, final ProxyPrefixName prefix, final FolderCommon connectionFolder ) throws InvalidOperationException, NullValueException, NotConvertableException
    {
        final ProxySubConnectionId proxySubConnectionId = new ProxySubConnectionId ( id );
        if ( this.subConnections.containsKey ( proxySubConnectionId ) )
        {
            throw new IllegalArgumentException ( "connection with id " + proxySubConnectionId + " already exists!" );
        }
        logger.info ( String.format ( "Adding new connection: %s -> %s", id, connection.getConnectionInformation () ) );
        final ProxySubConnection proxySubConnection = new ProxySubConnection ( connection, this.prefix, proxySubConnectionId, prefix, this.hive, connectionFolder );
        this.subConnections.put ( proxySubConnectionId, proxySubConnection );

        if ( this.currentConnection == null )
        {
            this.currentConnection = proxySubConnectionId;
        }
    }

    /**
     * @param connectionStateListener
     */
    public void addConnectionStateListener ( final ConnectionStateListener connectionStateListener )
    {
        this.connectionStateListeners.add ( connectionStateListener );
        currentConnection ().addConnectionStateListener ( connectionStateListener );
    }

    /**
     * @return name of currently active connection
     */
    public ProxySubConnectionId getCurrentConnection ()
    {
        return this.currentConnection;
    }

    /**
     * @return item prefix for this proxy, which will replace original prefix
     */
    public ProxyPrefixName getPrefix ()
    {
        return this.prefix;
    }

    /**
     * @return all available items which are already subscribed
     */
    public Map<String, ProxyDataItem> getRegisteredItems ()
    {
        return this.registeredItems;
    }

    /**
     * @return separator which separates prefix from rest of item name
     */
    public String getSeparator ()
    {
        return this.hive.getSeparator ();
    }

    /**
     * @return map with all added subconnections
     */
    public Map<ProxySubConnectionId, ProxySubConnection> getSubConnections ()
    {
        return this.subConnections;
    }

    /**
     * @return time how long proxy should wait if subconnection is lost,
     * before item is set on error
     */
    public Integer getWait ()
    {
        return this.wait;
    }

    /**
     * @param itemId
     * @return original item id
     */
    public String convertToOriginalId ( final String itemId )
    {
        return ProxyUtils.originalItemId ( itemId, this.hive.getSeparator (), getPrefix (), currentSubConnection ().getPrefix () );
    }

    /**
     * @param itemId the item id to convert (from the original source)
     * @return return name of item in proxy or <code>null</code> if the item does not match the proxy group
     */
    public String convertToProxyId ( final String itemId )
    {
        if ( ProxyUtils.isOriginalItemForProxyGroup ( itemId, this.hive.getSeparator (), currentSubConnection ().getPrefix () ) )
        {
            return ProxyUtils.proxyItemId ( itemId, this.hive.getSeparator (), getPrefix (), currentSubConnection ().getPrefix () );
        }
        return null;
    }

    /**
     * 
     */
    public void disconnectCurrentConnection ()
    {
        currentSubConnection ().disconnect ();
    }

    /**
     * 
     */
    public void connectCurrentConnection ()
    {
        currentSubConnection ().connect ();
    }

    /**
     * @param id
     * @return creates item and puts it in map
     */
    public ProxyDataItem realizeItem ( final String id )
    {
        ProxyDataItem item = this.registeredItems.get ( id );
        if ( item == null )
        {
            // create actual item
            final ProxyValueHolder pvh = new ProxyValueHolder ( this.hive.getSeparator (), this.getPrefix (), this.getCurrentConnection (), id );
            final ProxyWriteHandler pwh = new ProxyWriteHandlerImpl ( this.hive.getSeparator (), this.getPrefix (), this.getSubConnections (), this.getCurrentConnection (), id );
            item = new ProxyDataItem ( id, pvh, pwh, this.hive.getOperationService () );
            this.registeredItems.put ( id, item );

            setUpItem ( item, id );
        }
        return item;
    }

    private void setUpItem ( final ProxyDataItem item, final String requestId )
    {
        // add item chains
        applyTemplate ( item );

        // hook up item
        for ( final ProxySubConnection subConnection : getSubConnections ().values () )
        {
            final ItemManager itemManager = subConnection.getItemManager ();
            final String originalItemId = ProxyUtils.originalItemId ( requestId, this.hive.getSeparator (), getPrefix (), subConnection.getPrefix () );

            itemManager.addItemUpdateListener ( originalItemId, new ProxyItemUpdateListener ( this.itemListenerExecutor, item, subConnection ) );
        }
    }

    /**
     * Apply the item template as configured in the hive
     * @param item the item to which a template should by applied
     */
    private void applyTemplate ( final ProxyDataItem item )
    {
        final String itemId = item.getInformation ().getName ();
        final FactoryTemplate ft = this.hive.findFactoryTemplate ( itemId );
        logger.debug ( String.format ( "Find template for item '%s' : %s", itemId, ft ) );
        if ( ft != null )
        {
            try
            {
                item.setChain ( FactoryHelper.instantiateChainList ( this.hive, ft.getChainEntries () ) );
            }
            catch ( final ConfigurationError e )
            {
                logger.warn ( "Failed to apply item template", e );
            }
            item.setTemplateAttributes ( ft.getItemAttributes () );
        }
    }

    /**
     * @param connectionStateListener
     */
    public void removeConnectionStateListener ( final ConnectionStateListener connectionStateListener )
    {
        this.connectionStateListeners.remove ( connectionStateListener );
        currentConnection ().removeConnectionStateListener ( connectionStateListener );
    }

    /**
     * @param prefix
     */
    public void setPrefix ( final ProxyPrefixName prefix )
    {
        this.prefix = prefix;
    }

    /**
     * @param wait
     */
    public void setWait ( final Integer wait )
    {
        this.wait = wait;
    }

    /**
     * @param newConnectionId
     */
    public void switchTo ( final ProxySubConnectionId newConnectionId )
    {
        logger.warn ( String.format ( "Switching from '%s' to '%s'", this.currentConnection, newConnectionId ) );

        boolean locked = false;
        try
        {
            locked = this.switchLock.tryLock ( Integer.getInteger ( "org.openscada.da.server.proxy.switchLockTimeout", 1000 ), TimeUnit.MILLISECONDS );
        }
        catch ( final InterruptedException e )
        {
            logger.warn ( String.format ( "Failed switching from '%s' to '%s'. Got interrupted while waiting!", this.currentConnection, newConnectionId ), e );
            return;
        }

        if ( !locked )
        {
            logger.warn ( String.format ( "Failed switching from '%s' to '%s'. Switching is still in progress!", this.currentConnection, newConnectionId ) );
            return;
        }

        try
        {
            // remove 
            for ( final ConnectionStateListener listener : this.connectionStateListeners )
            {
                currentConnection ().removeConnectionStateListener ( listener );
            }

            for ( final ProxyDataItem proxyDataItem : this.registeredItems.values () )
            {
                proxyDataItem.getProxyValueHolder ().switchTo ( newConnectionId );
            }
            this.currentConnection = newConnectionId;
            for ( final ConnectionStateListener listener : this.connectionStateListeners )
            {
                currentConnection ().addConnectionStateListener ( listener );
            }

            // create the proxy folder
            createProxyFolder ();
        }
        finally
        {
            logger.info ( "Release switch lock" );
            this.switchLock.unlock ();
        }

    }

    private void destroyProxyFolder ()
    {
        // remove old folder
        if ( this.proxyFolder != null )
        {
            this.connectionFolder.remove ( this.proxyFolder );
            this.proxyFolder = null;
        }
    }

    private void createProxyFolder ()
    {
        // add new folder
        destroyProxyFolder ();

        this.proxyFolder = new ProxyFolder ( currentSubConnection ().getFolderManager (), this, new Location () );

        this.connectionFolder.add ( "items", this.proxyFolder, null );
    }

}

/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2008 inavare GmbH (http://inavare.com)
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

package org.openscada.da.server.opc2.configuration;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.openscada.da.opc.configuration.ConfigurationType;
import org.openscada.da.opc.configuration.RootDocument;
import org.openscada.da.server.common.configuration.ConfigurationError;
import org.openscada.da.server.opc2.Hive;
import org.openscada.da.server.opc2.connection.AccessMethod;
import org.openscada.da.server.opc2.connection.ConnectionSetup;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.w3c.dom.Node;

public class XMLConfigurator
{

    private RootDocument rootDocument = null;

    public XMLConfigurator ( final RootDocument rootDocument )
    {
        this.rootDocument = rootDocument;
    }

    public XMLConfigurator ( final Node node ) throws XmlException
    {
        this ( RootDocument.Factory.parse ( node ) );
    }

    public XMLConfigurator ( final String filename ) throws XmlException, IOException
    {
        this ( RootDocument.Factory.parse ( new File ( filename ) ) );
    }

    public void configure ( final Hive hive ) throws ConfigurationError
    {
        // first configure the base hive
        new org.openscada.da.server.common.configuration.xml.XMLConfigurator ( null, this.rootDocument.getRoot ().getItemTemplates (), null, null ).configure ( hive );

        // now configure the opc hive
        for ( final ConfigurationType configuration : this.rootDocument.getRoot ().getConnections ().getConfigurationList () )
        {
            if ( !configuration.getEnabled () )
            {
                continue;
            }

            final ConnectionInformation ci = new ConnectionInformation ();
            ci.setUser ( configuration.getUser () );
            ci.setPassword ( configuration.getPassword () );
            ci.setDomain ( configuration.getDomain () );
            ci.setHost ( configuration.getHost () );
            ci.setClsid ( configuration.getClsid () );
            ci.setProgId ( configuration.getProgid () );

            final ConnectionSetup setup = new ConnectionSetup ( ci );

            if ( configuration.isSetIgnoreTimestampOnlyChange () )
            {
                setup.setIgnoreTimestampOnlyChange ( configuration.getIgnoreTimestampOnlyChange () );
            }

            if ( configuration.isSetReconnectDelay () )
            {
                setup.setReconnectDelay ( configuration.getReconnectDelay () );
            }
            else
            {
                setup.setReconnectDelay ( 5000 );
            }

            final String access = configuration.getAccess ();
            if ( access.equalsIgnoreCase ( "sync" ) )
            {
                setup.setAccessMethod ( AccessMethod.SYNC );
            }
            else if ( access.equalsIgnoreCase ( "async" ) )
            {
                setup.setAccessMethod ( AccessMethod.ASYNC20 );
            }
            else if ( access.equalsIgnoreCase ( "async20" ) )
            {
                setup.setAccessMethod ( AccessMethod.ASYNC20 );
            }

            setup.setFlatBrowser ( configuration.getFlatBrowser () );
            setup.setTreeBrowser ( configuration.getTreeBrowser () );

            setup.setRefreshTimeout ( configuration.getRefresh () );
            setup.setInitialConnect ( configuration.getInitialRefresh () );
            setup.setDeviceTag ( configuration.getAlias () );

            setup.setItemIdPrefix ( configuration.getItemIdPrefix () );
            if ( configuration.isSetInitialItemResource () )
            {
                setup.setFileSourceUri ( configuration.getInitialItemResource () );
            }

            if ( setup.getDeviceTag () == null )
            {
                setup.setDeviceTag ( setup.getConnectionInformation ().getHost () + ":" + setup.getConnectionInformation ().getClsOrProgId () );
            }

            hive.addConnection ( setup, configuration.getConnected (), configuration.getInitialItemList () );
        }
    }
}

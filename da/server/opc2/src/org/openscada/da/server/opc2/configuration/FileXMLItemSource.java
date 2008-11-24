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
import org.openscada.da.opc.configuration.InitialItemsType;
import org.openscada.da.opc.configuration.ItemsDocument;
import org.openscada.da.server.common.item.factory.FolderItemFactory;

public class FileXMLItemSource extends AbstractXMLItemSource
{
    private final File file;

    public FileXMLItemSource ( final String file, final FolderItemFactory itemFactory, final String baseId )
    {
        super ( itemFactory, baseId );
        this.file = new File ( file );
    }

    @Override
    protected InitialItemsType parse () throws XmlException, IOException
    {
        final InitialItemsType items = ItemsDocument.Factory.parse ( this.file ).getItems ();
        return items;
    }

}

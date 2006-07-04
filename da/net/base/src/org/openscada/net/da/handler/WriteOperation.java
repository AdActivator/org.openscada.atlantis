/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006 inavare GmbH (http://inavare.com)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openscada.net.da.handler;

import org.openscada.da.core.data.Variant;
import org.openscada.net.base.data.Message;
import org.openscada.net.base.data.StringValue;
import org.openscada.utils.lang.Holder;

public class WriteOperation
{

    public static Message create ( String itemName, Variant value )
    {
        Message message = new Message ( Messages.CC_WRITE_OPERATION );
        
        message.getValues().put ( "item-name", new StringValue(itemName) );
        message.getValues().put ( "value", Messages.variantToValue(value) );
        
        return message;
    }

    public static void parse ( Message message, Holder<String> itemName, Holder<Variant> value )
    {
        // FIXME: handle missing item name
        itemName.value = message.getValues().get ( "item-name" ).toString();
        
        value.value = Messages.valueToVariant ( message.getValues().get("value"), new Variant() );
    }
}

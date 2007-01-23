/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2007 inavare GmbH (http://inavare.com)
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

package org.openscada.da.client.ice;

import org.openscada.utils.exec.LongRunningListener;
import org.openscada.utils.exec.LongRunningOperation;

import Ice.LocalException;
import Ice.UserException;
import OpenSCADA.DA.AMI_Hive_write;

public class AsyncWriteOperation extends AMI_Hive_write implements LongRunningOperation 
{
    private AsyncBaseOperation _op;
    
    public AsyncWriteOperation ( LongRunningListener listener )
    {
        super ();
        _op = new AsyncBaseOperation ( listener );
    }
    
    @Override
    public void ice_exception ( LocalException ex )
    {
        _op.failure ( ex );
    }

    @Override
    public void ice_exception ( UserException ex )
    {
        _op.failure ( ex );
    }

    @Override
    public void ice_response ()
    {
        _op.success ();
    }

    // Forward to AsyncBaseOperation
    
    public void cancel ()
    {
        _op.cancel ();
    }

    public Throwable getError ()
    {
        return _op.getError ();
    }

    public boolean isComplete ()
    {
        return _op.isComplete ();
    }

    public void waitForCompletion () throws InterruptedException
    {
        _op.waitForCompletion ();
    }

    public void waitForCompletion ( int timeout ) throws InterruptedException
    {
       _op.waitForCompletion ( timeout );
    }
}

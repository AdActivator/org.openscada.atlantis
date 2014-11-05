/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.openscada.protocol.iec60870.client.data;

import org.openscada.protocol.iec60870.asdu.types.ASDUAddress;

public interface DataModuleContext
{
    public void requestStartData ();

    public void startInterrogation ( ASDUAddress address, short qualifierOfInterrogation );
}

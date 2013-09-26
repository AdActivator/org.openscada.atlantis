/*
 * This file is part of the openSCADA project
 * Copyright (C) 2011-2012 TH4 SYSTEMS GmbH (http://th4-systems.com)
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

package org.eclipse.scada.da.master.mapper;

import java.util.Dictionary;
import java.util.Map;

import org.eclipse.scada.ca.ConfigurationDataHelper;
import org.eclipse.scada.core.Variant;
import org.eclipse.scada.da.client.DataItemValue;
import org.eclipse.scada.da.mapper.ValueMapper;
import org.eclipse.scada.da.mapper.ValueMapperListener;
import org.eclipse.scada.da.master.AbstractMasterHandlerImpl;
import org.eclipse.scada.da.master.MasterItem;
import org.eclipse.scada.utils.osgi.pool.ObjectPoolTracker;
import org.eclipse.scada.utils.osgi.pool.SingleObjectPoolServiceTracker;
import org.eclipse.scada.utils.osgi.pool.SingleObjectPoolServiceTracker.ServiceListener;

public class MapperMasterHandler extends AbstractMasterHandlerImpl implements ValueMapperListener
{

    private String sourceAttributeName;

    private String targetAttributeName;

    private final ObjectPoolTracker<ValueMapper> mapperPoolTracker;

    private volatile ValueMapper mapper;

    private SingleObjectPoolServiceTracker<ValueMapper> mapperTracker;

    private final String id;

    public MapperMasterHandler ( final String id, final ObjectPoolTracker<MasterItem> poolTracker, final ObjectPoolTracker<ValueMapper> mapperPoolTracker, final int defaultPriority )
    {
        super ( poolTracker, defaultPriority );
        this.id = id;
        this.mapperPoolTracker = mapperPoolTracker;
    }

    @Override
    public synchronized void update ( final org.eclipse.scada.sec.UserInformation userInformation, final java.util.Map<String, String> parameters ) throws Exception
    {
        super.update ( userInformation, parameters );

        if ( this.mapperTracker != null )
        {
            this.mapperTracker.close ();
            this.mapperTracker = null;
        }

        final ConfigurationDataHelper cfg = new ConfigurationDataHelper ( parameters );

        this.mapperTracker = new SingleObjectPoolServiceTracker<ValueMapper> ( this.mapperPoolTracker, cfg.getStringChecked ( "mapper.id", "'mapper.id' must be specified" ), new ServiceListener<ValueMapper> () {

            @Override
            public void serviceChange ( final ValueMapper service, final Dictionary<?, ?> properties )
            {
                setMapper ( service );
            }
        } );

        this.sourceAttributeName = cfg.getString ( "sourceAttributeName" );
        this.targetAttributeName = cfg.getString ( "targetAttributeName" );

        this.mapperTracker.open ();

        reprocess ();
    };

    protected void setMapper ( final ValueMapper mapper )
    {
        if ( this.mapper != null )
        {
            this.mapper.removeListener ( this );
        }
        this.mapper = mapper;
        if ( this.mapper != null )
        {
            this.mapper.addListener ( this );
        }
        reprocess ();
    }

    @Override
    public synchronized void dataUpdate ( final Map<String, Object> context, final DataItemValue.Builder builder )
    {
        Variant sourceValue;
        if ( this.sourceAttributeName == null || this.sourceAttributeName.isEmpty () )
        {
            sourceValue = builder.getValue ();
        }
        else
        {
            sourceValue = builder.getAttributes ().get ( this.sourceAttributeName );
        }
        final ValueMapper mapper = getMapper ();
        if ( mapper != null )
        {
            if ( this.targetAttributeName == null || this.targetAttributeName.isEmpty () )
            {
                builder.setValue ( mapper.mapValue ( sourceValue ) );
            }
            else
            {
                builder.setAttribute ( this.targetAttributeName, mapper.mapValue ( sourceValue ) );
            }
        }
        else
        {
            builder.setAttribute ( this.id + ".mapperMissing", Variant.TRUE );
        }
    }

    protected ValueMapper getMapper ()
    {
        return this.mapper;
    }

    @Override
    public void stateChanged ()
    {
        reprocess ();
    }

}

/**
 * This file is part of the openSCADA project
 * 
 * Copyright (C) 2013 Jens Reimann (ctron@dentrassi.de)
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
package org.eclipse.scada.da.exec.configuration.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.scada.da.exec.configuration.ConfigurationPackage;
import org.eclipse.scada.da.exec.configuration.HiveProcessCommandType;
import org.eclipse.scada.da.exec.configuration.QueueType;
import org.eclipse.scada.da.exec.configuration.RootType;
import org.eclipse.scada.da.exec.configuration.SplitContinuousCommandType;
import org.eclipse.scada.da.exec.configuration.TriggerCommandType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Root Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.impl.RootTypeImpl#getGroup <em>Group</em>}</li>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.impl.RootTypeImpl#getQueue <em>Queue</em>}</li>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.impl.RootTypeImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.impl.RootTypeImpl#getHiveProcess <em>Hive Process</em>}</li>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.impl.RootTypeImpl#getTrigger <em>Trigger</em>}</li>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.impl.RootTypeImpl#getAdditionalConfigurationDirectory <em>Additional Configuration Directory</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RootTypeImpl extends MinimalEObjectImpl.Container implements RootType
{
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "This file is part of the openSCADA project\n\nCopyright (C) 2013 Jens Reimann (ctron@dentrassi.de)\n\nopenSCADA is free software: you can redistribute it and/or modify\nit under the terms of the GNU Lesser General Public License version 3\nonly, as published by the Free Software Foundation.\n\nopenSCADA is distributed in the hope that it will be useful,\nbut WITHOUT ANY WARRANTY; without even the implied warranty of\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\nGNU Lesser General Public License version 3 for more details\n(a copy is included in the LICENSE file that accompanied this code).\n\nYou should have received a copy of the GNU Lesser General Public License\nversion 3 along with openSCADA. If not, see\n<http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License."; //$NON-NLS-1$

    /**
     * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroup()
     * @generated
     * @ordered
     */
    protected FeatureMap group;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected RootTypeImpl ()
    {
        super ();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass ()
    {
        return ConfigurationPackage.Literals.ROOT_TYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getGroup ()
    {
        if ( group == null )
        {
            group = new BasicFeatureMap ( this, ConfigurationPackage.ROOT_TYPE__GROUP );
        }
        return group;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<QueueType> getQueue ()
    {
        return getGroup ().list ( ConfigurationPackage.Literals.ROOT_TYPE__QUEUE );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<SplitContinuousCommandType> getCommand ()
    {
        return getGroup ().list ( ConfigurationPackage.Literals.ROOT_TYPE__COMMAND );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<HiveProcessCommandType> getHiveProcess ()
    {
        return getGroup ().list ( ConfigurationPackage.Literals.ROOT_TYPE__HIVE_PROCESS );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<TriggerCommandType> getTrigger ()
    {
        return getGroup ().list ( ConfigurationPackage.Literals.ROOT_TYPE__TRIGGER );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<String> getAdditionalConfigurationDirectory ()
    {
        return getGroup ().list ( ConfigurationPackage.Literals.ROOT_TYPE__ADDITIONAL_CONFIGURATION_DIRECTORY );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove ( InternalEObject otherEnd, int featureID, NotificationChain msgs )
    {
        switch ( featureID )
        {
            case ConfigurationPackage.ROOT_TYPE__GROUP:
                return ( (InternalEList<?>)getGroup () ).basicRemove ( otherEnd, msgs );
            case ConfigurationPackage.ROOT_TYPE__QUEUE:
                return ( (InternalEList<?>)getQueue () ).basicRemove ( otherEnd, msgs );
            case ConfigurationPackage.ROOT_TYPE__COMMAND:
                return ( (InternalEList<?>)getCommand () ).basicRemove ( otherEnd, msgs );
            case ConfigurationPackage.ROOT_TYPE__HIVE_PROCESS:
                return ( (InternalEList<?>)getHiveProcess () ).basicRemove ( otherEnd, msgs );
            case ConfigurationPackage.ROOT_TYPE__TRIGGER:
                return ( (InternalEList<?>)getTrigger () ).basicRemove ( otherEnd, msgs );
        }
        return super.eInverseRemove ( otherEnd, featureID, msgs );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet ( int featureID, boolean resolve, boolean coreType )
    {
        switch ( featureID )
        {
            case ConfigurationPackage.ROOT_TYPE__GROUP:
                if ( coreType )
                    return getGroup ();
                return ( (FeatureMap.Internal)getGroup () ).getWrapper ();
            case ConfigurationPackage.ROOT_TYPE__QUEUE:
                return getQueue ();
            case ConfigurationPackage.ROOT_TYPE__COMMAND:
                return getCommand ();
            case ConfigurationPackage.ROOT_TYPE__HIVE_PROCESS:
                return getHiveProcess ();
            case ConfigurationPackage.ROOT_TYPE__TRIGGER:
                return getTrigger ();
            case ConfigurationPackage.ROOT_TYPE__ADDITIONAL_CONFIGURATION_DIRECTORY:
                return getAdditionalConfigurationDirectory ();
        }
        return super.eGet ( featureID, resolve, coreType );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public void eSet ( int featureID, Object newValue )
    {
        switch ( featureID )
        {
            case ConfigurationPackage.ROOT_TYPE__GROUP:
                ( (FeatureMap.Internal)getGroup () ).set ( newValue );
                return;
            case ConfigurationPackage.ROOT_TYPE__QUEUE:
                getQueue ().clear ();
                getQueue ().addAll ( (Collection<? extends QueueType>)newValue );
                return;
            case ConfigurationPackage.ROOT_TYPE__COMMAND:
                getCommand ().clear ();
                getCommand ().addAll ( (Collection<? extends SplitContinuousCommandType>)newValue );
                return;
            case ConfigurationPackage.ROOT_TYPE__HIVE_PROCESS:
                getHiveProcess ().clear ();
                getHiveProcess ().addAll ( (Collection<? extends HiveProcessCommandType>)newValue );
                return;
            case ConfigurationPackage.ROOT_TYPE__TRIGGER:
                getTrigger ().clear ();
                getTrigger ().addAll ( (Collection<? extends TriggerCommandType>)newValue );
                return;
            case ConfigurationPackage.ROOT_TYPE__ADDITIONAL_CONFIGURATION_DIRECTORY:
                getAdditionalConfigurationDirectory ().clear ();
                getAdditionalConfigurationDirectory ().addAll ( (Collection<? extends String>)newValue );
                return;
        }
        super.eSet ( featureID, newValue );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset ( int featureID )
    {
        switch ( featureID )
        {
            case ConfigurationPackage.ROOT_TYPE__GROUP:
                getGroup ().clear ();
                return;
            case ConfigurationPackage.ROOT_TYPE__QUEUE:
                getQueue ().clear ();
                return;
            case ConfigurationPackage.ROOT_TYPE__COMMAND:
                getCommand ().clear ();
                return;
            case ConfigurationPackage.ROOT_TYPE__HIVE_PROCESS:
                getHiveProcess ().clear ();
                return;
            case ConfigurationPackage.ROOT_TYPE__TRIGGER:
                getTrigger ().clear ();
                return;
            case ConfigurationPackage.ROOT_TYPE__ADDITIONAL_CONFIGURATION_DIRECTORY:
                getAdditionalConfigurationDirectory ().clear ();
                return;
        }
        super.eUnset ( featureID );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet ( int featureID )
    {
        switch ( featureID )
        {
            case ConfigurationPackage.ROOT_TYPE__GROUP:
                return group != null && !group.isEmpty ();
            case ConfigurationPackage.ROOT_TYPE__QUEUE:
                return !getQueue ().isEmpty ();
            case ConfigurationPackage.ROOT_TYPE__COMMAND:
                return !getCommand ().isEmpty ();
            case ConfigurationPackage.ROOT_TYPE__HIVE_PROCESS:
                return !getHiveProcess ().isEmpty ();
            case ConfigurationPackage.ROOT_TYPE__TRIGGER:
                return !getTrigger ().isEmpty ();
            case ConfigurationPackage.ROOT_TYPE__ADDITIONAL_CONFIGURATION_DIRECTORY:
                return !getAdditionalConfigurationDirectory ().isEmpty ();
        }
        return super.eIsSet ( featureID );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString ()
    {
        if ( eIsProxy () )
            return super.toString ();

        StringBuffer result = new StringBuffer ( super.toString () );
        result.append ( " (group: " ); //$NON-NLS-1$
        result.append ( group );
        result.append ( ')' );
        return result.toString ();
    }

} //RootTypeImpl

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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.scada.da.exec.configuration.ConfigurationPackage;
import org.eclipse.scada.da.exec.configuration.SingleCommandType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Single Command Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.impl.SingleCommandTypeImpl#getPeriod <em>Period</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SingleCommandTypeImpl extends CommandTypeImpl implements SingleCommandType
{
    //$NON-NLS-1$

    /**
     * The default value of the '{@link #getPeriod() <em>Period</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPeriod()
     * @generated
     * @ordered
     */
    protected static final int PERIOD_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getPeriod() <em>Period</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPeriod()
     * @generated
     * @ordered
     */
    protected int period = PERIOD_EDEFAULT;

    /**
     * This is true if the Period attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean periodESet;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected SingleCommandTypeImpl ()
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
        return ConfigurationPackage.Literals.SINGLE_COMMAND_TYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public int getPeriod ()
    {
        return period;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPeriod ( int newPeriod )
    {
        int oldPeriod = period;
        period = newPeriod;
        boolean oldPeriodESet = periodESet;
        periodESet = true;
        if ( eNotificationRequired () )
            eNotify ( new ENotificationImpl ( this, Notification.SET, ConfigurationPackage.SINGLE_COMMAND_TYPE__PERIOD, oldPeriod, period, !oldPeriodESet ) );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetPeriod ()
    {
        int oldPeriod = period;
        boolean oldPeriodESet = periodESet;
        period = PERIOD_EDEFAULT;
        periodESet = false;
        if ( eNotificationRequired () )
            eNotify ( new ENotificationImpl ( this, Notification.UNSET, ConfigurationPackage.SINGLE_COMMAND_TYPE__PERIOD, oldPeriod, PERIOD_EDEFAULT, oldPeriodESet ) );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetPeriod ()
    {
        return periodESet;
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
            case ConfigurationPackage.SINGLE_COMMAND_TYPE__PERIOD:
                return getPeriod ();
        }
        return super.eGet ( featureID, resolve, coreType );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet ( int featureID, Object newValue )
    {
        switch ( featureID )
        {
            case ConfigurationPackage.SINGLE_COMMAND_TYPE__PERIOD:
                setPeriod ( (Integer)newValue );
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
            case ConfigurationPackage.SINGLE_COMMAND_TYPE__PERIOD:
                unsetPeriod ();
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
            case ConfigurationPackage.SINGLE_COMMAND_TYPE__PERIOD:
                return isSetPeriod ();
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
        result.append ( " (period: " ); //$NON-NLS-1$
        if ( periodESet )
            result.append ( period );
        else
            result.append ( "<unset>" ); //$NON-NLS-1$
        result.append ( ')' );
        return result.toString ();
    }

} //SingleCommandTypeImpl

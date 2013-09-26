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
package org.openscada.da.exec.configuration.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.openscada.da.exec.configuration.ConfigurationPackage;
import org.openscada.da.exec.configuration.TriggerCommandType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Trigger Command Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openscada.da.exec.configuration.impl.TriggerCommandTypeImpl#getArgumentPlaceholder <em>Argument Placeholder</em>}</li>
 *   <li>{@link org.openscada.da.exec.configuration.impl.TriggerCommandTypeImpl#isFork <em>Fork</em>}</li>
 *   <li>{@link org.openscada.da.exec.configuration.impl.TriggerCommandTypeImpl#isSkipIfNull <em>Skip If Null</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TriggerCommandTypeImpl extends CommandTypeImpl implements TriggerCommandType
{
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "This file is part of the openSCADA project\n\nCopyright (C) 2013 Jens Reimann (ctron@dentrassi.de)\n\nopenSCADA is free software: you can redistribute it and/or modify\nit under the terms of the GNU Lesser General Public License version 3\nonly, as published by the Free Software Foundation.\n\nopenSCADA is distributed in the hope that it will be useful,\nbut WITHOUT ANY WARRANTY; without even the implied warranty of\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\nGNU Lesser General Public License version 3 for more details\n(a copy is included in the LICENSE file that accompanied this code).\n\nYou should have received a copy of the GNU Lesser General Public License\nversion 3 along with openSCADA. If not, see\n<http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License."; //$NON-NLS-1$

    /**
     * The default value of the '{@link #getArgumentPlaceholder() <em>Argument Placeholder</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getArgumentPlaceholder()
     * @generated
     * @ordered
     */
    protected static final String ARGUMENT_PLACEHOLDER_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getArgumentPlaceholder() <em>Argument Placeholder</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getArgumentPlaceholder()
     * @generated
     * @ordered
     */
    protected String argumentPlaceholder = ARGUMENT_PLACEHOLDER_EDEFAULT;

    /**
     * The default value of the '{@link #isFork() <em>Fork</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isFork()
     * @generated
     * @ordered
     */
    protected static final boolean FORK_EDEFAULT = true;

    /**
     * The cached value of the '{@link #isFork() <em>Fork</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isFork()
     * @generated
     * @ordered
     */
    protected boolean fork = FORK_EDEFAULT;

    /**
     * This is true if the Fork attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean forkESet;

    /**
     * The default value of the '{@link #isSkipIfNull() <em>Skip If Null</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSkipIfNull()
     * @generated
     * @ordered
     */
    protected static final boolean SKIP_IF_NULL_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isSkipIfNull() <em>Skip If Null</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSkipIfNull()
     * @generated
     * @ordered
     */
    protected boolean skipIfNull = SKIP_IF_NULL_EDEFAULT;

    /**
     * This is true if the Skip If Null attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean skipIfNullESet;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected TriggerCommandTypeImpl ()
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
        return ConfigurationPackage.Literals.TRIGGER_COMMAND_TYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getArgumentPlaceholder ()
    {
        return argumentPlaceholder;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setArgumentPlaceholder ( String newArgumentPlaceholder )
    {
        String oldArgumentPlaceholder = argumentPlaceholder;
        argumentPlaceholder = newArgumentPlaceholder;
        if ( eNotificationRequired () )
            eNotify ( new ENotificationImpl ( this, Notification.SET, ConfigurationPackage.TRIGGER_COMMAND_TYPE__ARGUMENT_PLACEHOLDER, oldArgumentPlaceholder, argumentPlaceholder ) );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isFork ()
    {
        return fork;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setFork ( boolean newFork )
    {
        boolean oldFork = fork;
        fork = newFork;
        boolean oldForkESet = forkESet;
        forkESet = true;
        if ( eNotificationRequired () )
            eNotify ( new ENotificationImpl ( this, Notification.SET, ConfigurationPackage.TRIGGER_COMMAND_TYPE__FORK, oldFork, fork, !oldForkESet ) );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetFork ()
    {
        boolean oldFork = fork;
        boolean oldForkESet = forkESet;
        fork = FORK_EDEFAULT;
        forkESet = false;
        if ( eNotificationRequired () )
            eNotify ( new ENotificationImpl ( this, Notification.UNSET, ConfigurationPackage.TRIGGER_COMMAND_TYPE__FORK, oldFork, FORK_EDEFAULT, oldForkESet ) );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetFork ()
    {
        return forkESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSkipIfNull ()
    {
        return skipIfNull;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSkipIfNull ( boolean newSkipIfNull )
    {
        boolean oldSkipIfNull = skipIfNull;
        skipIfNull = newSkipIfNull;
        boolean oldSkipIfNullESet = skipIfNullESet;
        skipIfNullESet = true;
        if ( eNotificationRequired () )
            eNotify ( new ENotificationImpl ( this, Notification.SET, ConfigurationPackage.TRIGGER_COMMAND_TYPE__SKIP_IF_NULL, oldSkipIfNull, skipIfNull, !oldSkipIfNullESet ) );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetSkipIfNull ()
    {
        boolean oldSkipIfNull = skipIfNull;
        boolean oldSkipIfNullESet = skipIfNullESet;
        skipIfNull = SKIP_IF_NULL_EDEFAULT;
        skipIfNullESet = false;
        if ( eNotificationRequired () )
            eNotify ( new ENotificationImpl ( this, Notification.UNSET, ConfigurationPackage.TRIGGER_COMMAND_TYPE__SKIP_IF_NULL, oldSkipIfNull, SKIP_IF_NULL_EDEFAULT, oldSkipIfNullESet ) );
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetSkipIfNull ()
    {
        return skipIfNullESet;
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
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__ARGUMENT_PLACEHOLDER:
                return getArgumentPlaceholder ();
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__FORK:
                return isFork ();
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__SKIP_IF_NULL:
                return isSkipIfNull ();
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
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__ARGUMENT_PLACEHOLDER:
                setArgumentPlaceholder ( (String)newValue );
                return;
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__FORK:
                setFork ( (Boolean)newValue );
                return;
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__SKIP_IF_NULL:
                setSkipIfNull ( (Boolean)newValue );
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
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__ARGUMENT_PLACEHOLDER:
                setArgumentPlaceholder ( ARGUMENT_PLACEHOLDER_EDEFAULT );
                return;
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__FORK:
                unsetFork ();
                return;
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__SKIP_IF_NULL:
                unsetSkipIfNull ();
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
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__ARGUMENT_PLACEHOLDER:
                return ARGUMENT_PLACEHOLDER_EDEFAULT == null ? argumentPlaceholder != null : !ARGUMENT_PLACEHOLDER_EDEFAULT.equals ( argumentPlaceholder );
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__FORK:
                return isSetFork ();
            case ConfigurationPackage.TRIGGER_COMMAND_TYPE__SKIP_IF_NULL:
                return isSetSkipIfNull ();
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
        result.append ( " (argumentPlaceholder: " ); //$NON-NLS-1$
        result.append ( argumentPlaceholder );
        result.append ( ", fork: " ); //$NON-NLS-1$
        if ( forkESet )
            result.append ( fork );
        else
            result.append ( "<unset>" ); //$NON-NLS-1$
        result.append ( ", skipIfNull: " ); //$NON-NLS-1$
        if ( skipIfNullESet )
            result.append ( skipIfNull );
        else
            result.append ( "<unset>" ); //$NON-NLS-1$
        result.append ( ')' );
        return result.toString ();
    }

} //TriggerCommandTypeImpl

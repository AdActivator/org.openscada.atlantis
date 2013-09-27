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
package org.eclipse.scada.da.exec.configuration;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Trigger Command Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#getArgumentPlaceholder <em>Argument Placeholder</em>}</li>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#isFork <em>Fork</em>}</li>
 *   <li>{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#isSkipIfNull <em>Skip If Null</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.scada.da.exec.configuration.ConfigurationPackage#getTriggerCommandType()
 * @model extendedMetaData="name='TriggerCommandType' kind='elementOnly'"
 * @generated
 */
public interface TriggerCommandType extends CommandType
{

    /**
     * Returns the value of the '<em><b>Argument Placeholder</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Argument Placeholder</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Argument Placeholder</em>' attribute.
     * @see #setArgumentPlaceholder(String)
     * @see org.eclipse.scada.da.exec.configuration.ConfigurationPackage#getTriggerCommandType_ArgumentPlaceholder()
     * @model dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='argumentPlaceholder'"
     * @generated
     */
    String getArgumentPlaceholder ();

    /**
     * Sets the value of the '{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#getArgumentPlaceholder <em>Argument Placeholder</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Argument Placeholder</em>' attribute.
     * @see #getArgumentPlaceholder()
     * @generated
     */
    void setArgumentPlaceholder ( String value );

    /**
     * Returns the value of the '<em><b>Fork</b></em>' attribute.
     * The default value is <code>"true"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Fork</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Fork</em>' attribute.
     * @see #isSetFork()
     * @see #unsetFork()
     * @see #setFork(boolean)
     * @see org.eclipse.scada.da.exec.configuration.ConfigurationPackage#getTriggerCommandType_Fork()
     * @model default="true" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Boolean"
     *        extendedMetaData="kind='attribute' name='fork'"
     * @generated
     */
    boolean isFork ();

    /**
     * Sets the value of the '{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#isFork <em>Fork</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Fork</em>' attribute.
     * @see #isSetFork()
     * @see #unsetFork()
     * @see #isFork()
     * @generated
     */
    void setFork ( boolean value );

    /**
     * Unsets the value of the '{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#isFork <em>Fork</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetFork()
     * @see #isFork()
     * @see #setFork(boolean)
     * @generated
     */
    void unsetFork ();

    /**
     * Returns whether the value of the '{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#isFork <em>Fork</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Fork</em>' attribute is set.
     * @see #unsetFork()
     * @see #isFork()
     * @see #setFork(boolean)
     * @generated
     */
    boolean isSetFork ();

    /**
     * Returns the value of the '<em><b>Skip If Null</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Skip If Null</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Skip If Null</em>' attribute.
     * @see #isSetSkipIfNull()
     * @see #unsetSkipIfNull()
     * @see #setSkipIfNull(boolean)
     * @see org.eclipse.scada.da.exec.configuration.ConfigurationPackage#getTriggerCommandType_SkipIfNull()
     * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Boolean"
     *        extendedMetaData="kind='attribute' name='skipIfNull'"
     * @generated
     */
    boolean isSkipIfNull ();

    /**
     * Sets the value of the '{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#isSkipIfNull <em>Skip If Null</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Skip If Null</em>' attribute.
     * @see #isSetSkipIfNull()
     * @see #unsetSkipIfNull()
     * @see #isSkipIfNull()
     * @generated
     */
    void setSkipIfNull ( boolean value );

    /**
     * Unsets the value of the '{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#isSkipIfNull <em>Skip If Null</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetSkipIfNull()
     * @see #isSkipIfNull()
     * @see #setSkipIfNull(boolean)
     * @generated
     */
    void unsetSkipIfNull ();

    /**
     * Returns whether the value of the '{@link org.eclipse.scada.da.exec.configuration.TriggerCommandType#isSkipIfNull <em>Skip If Null</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Skip If Null</em>' attribute is set.
     * @see #unsetSkipIfNull()
     * @see #isSkipIfNull()
     * @see #setSkipIfNull(boolean)
     * @generated
     */
    boolean isSetSkipIfNull ();

} // TriggerCommandType

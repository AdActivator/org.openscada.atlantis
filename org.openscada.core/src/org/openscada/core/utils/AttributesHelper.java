/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2010 TH4 SYSTEMS GmbH (http://th4-systems.com)
 *
 * OpenSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openscada.core.Variant;

public class AttributesHelper
{
    /**
     * merges the difference attributes into the target
     * <p>
     * returns the real changes performed on <code>target</code> in <code>diff</code>
     * @param target the attributes to merge the difference in
     * @param change the attributes to change
     * @param diff output of real changes that were made
     */
    public static void mergeAttributes ( final Map<String, Variant> target, final Map<String, Variant> change, final Map<String, Variant> diff )
    {
        if ( change == null )
        {
            // no change
            return;
        }

        for ( final Map.Entry<String, Variant> entry : change.entrySet () )
        {
            if ( entry.getKey () == null )
            {
                continue;
            }

            if ( entry.getValue () == null )
            {
                if ( target.containsKey ( entry.getKey () ) )
                {
                    target.remove ( entry.getKey () );
                    if ( diff != null )
                    {
                        diff.put ( entry.getKey (), null );
                    }
                }
            }
            else if ( entry.getValue ().isNull () )
            {
                if ( target.containsKey ( entry.getKey () ) )
                {
                    target.remove ( entry.getKey () );
                    if ( diff != null )
                    {
                        diff.put ( entry.getKey (), null );
                    }
                }
            }
            else
            {
                if ( diff != null && !target.containsKey ( entry.getKey () ) )
                {
                    diff.put ( entry.getKey (), entry.getValue () );
                }
                else if ( diff != null && !target.get ( entry.getKey () ).equals ( entry.getValue () ) )
                {
                    diff.put ( entry.getKey (), entry.getValue () );
                }

                target.put ( entry.getKey (), entry.getValue () );
            }
        }
    }

    /**
     * merges the difference attributes into the target
     * @param target the attributes to merge the difference in
     * @param change the attributes to change
     */
    public static void mergeAttributes ( final Map<String, Variant> target, final Map<String, Variant> change )
    {
        mergeAttributes ( target, change, null );
    }

    /**
     * merges the attribute differences. But respects the initial flag sent by many events.
     * 
     * in the case the difference is flagged initial the target will be cleared first. This
     * is a convenient method to easy the merge.
     * 
     * @param target the attributes to merge the difference in
     * @param diff the difference attributes
     * @param initial initial flag
     */
    public static void mergeAttributes ( final Map<String, Variant> target, final Map<String, Variant> diff, final boolean initial )
    {
        if ( initial )
        {
            target.clear ();
        }

        mergeAttributes ( target, diff );
    }

    /**
     * update the target attributes to contain only the requested attributes 
     * @param target the attributes to update the difference to
     * @param attributes the new attributes set
     * @param diff the actual difference
     */
    public static void set ( final Map<String, Variant> target, Map<String, Variant> attributes, final Map<String, Variant> diff )
    {
        if ( attributes == null )
        {
            attributes = Collections.emptyMap ();
        }

        final Set<String> initialSet = new HashSet<String> ( target.keySet () );

        for ( final Map.Entry<String, Variant> entry : attributes.entrySet () )
        {
            if ( target.containsKey ( entry.getKey () ) )
            {
                if ( entry.getValue () == null || entry.getValue ().isNull () )
                {
                    // entry removed
                    target.remove ( entry.getKey () );
                    diff.put ( entry.getKey (), null );
                }
                else
                {
                    if ( !target.get ( entry.getKey () ).equals ( entry.getValue () ) )
                    {
                        // entry changed
                        diff.put ( entry.getKey (), entry.getValue () );
                        target.put ( entry.getKey (), entry.getValue () );
                    }
                }
                initialSet.remove ( entry.getKey () );
            }
            else
            {
                if ( entry.getValue () != null && !entry.getValue ().isNull () )
                {
                    // entry is new
                    diff.put ( entry.getKey (), entry.getValue () );
                    target.put ( entry.getKey (), entry.getValue () );
                }
            }
        }

        for ( final String key : initialSet )
        {
            target.remove ( key );
            diff.put ( key, null );
        }
    }

    /**
     * Generate the difference between two maps
     * @param source the source map
     * @param target the target map
     * @return the difference
     */
    public static Map<String, Variant> diff ( Map<String, Variant> source, final Map<String, Variant> target )
    {
        final Map<String, Variant> result;
        if ( target != null )
        {
            result = new HashMap<String, Variant> ( target );
        }
        else
        {
            result = Collections.emptyMap ();
        }

        if ( source == null )
        {
            source = Collections.emptyMap ();
        }

        final Set<String> removeSet = new HashSet<String> ();

        for ( final Map.Entry<String, Variant> entry : source.entrySet () )
        {
            final Variant value = result.get ( entry.getKey () );
            if ( value == null )
            {
                result.put ( entry.getKey (), null );
            }
            else if ( value.equals ( entry.getValue () ) )
            {
                removeSet.add ( entry.getKey () );
            }
        }

        for ( final String key : removeSet )
        {
            result.remove ( key );
        }

        return result;
    }

}

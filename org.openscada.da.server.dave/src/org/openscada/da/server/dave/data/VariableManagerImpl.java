package org.openscada.da.server.dave.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.openscada.ca.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class VariableManagerImpl implements VariableManager, ConfigurationFactory
{

    private final static Logger logger = LoggerFactory.getLogger ( VariableManagerImpl.class );

    private static enum TYPE
    {
        BIT,
        BYTE,
        FLOAT,
        UDT
    }

    private static class TypeEntry
    {
        private final String name;

        private final TYPE type;

        private String typeName;

        private final int index;

        private int subIndex;

        private final TypeEntry[] attributes;

        public TypeEntry ( final String name, final String typeName, final int index )
        {
            this.name = name;
            this.type = TYPE.UDT;
            this.typeName = typeName;
            this.attributes = null;
            this.index = index;
        }

        public TypeEntry ( final String name, final int index, final int subIndex, final TypeEntry... attributes )
        {
            this.name = name;
            this.index = index;
            this.subIndex = subIndex;
            this.type = TYPE.BIT;
            this.attributes = attributes;
        }

        public TypeEntry ( final String name, final TYPE type, final int index, final TypeEntry... attributes )
        {
            this.name = name;
            this.index = index;
            this.type = type;
            this.attributes = attributes;
        }

        public TypeEntry[] getAttributes ()
        {
            return this.attributes;
        }

        public int getIndex ()
        {
            return this.index;
        }

        public String getName ()
        {
            return this.name;
        }

        public int getSubIndex ()
        {
            return this.subIndex;
        }

        public TYPE getType ()
        {
            return this.type;
        }

        public String getTypeName ()
        {
            return this.typeName;
        }

        @Override
        public String toString ()
        {
            switch ( this.type )
            {
            default:
                return String.format ( "%s:%s", this.name, this.type );
            }
        }
    }

    private final Multimap<String, VariableListener> listeners = HashMultimap.create ();

    private final Multimap<String, String> typeDeps = HashMultimap.create ();

    private final Map<String, Collection<TypeEntry>> types = new HashMap<String, Collection<TypeEntry>> ();

    private final Executor executor;

    public VariableManagerImpl ( final Executor executor )
    {
        this.executor = executor;
    }

    public void dispose ()
    {
    }

    public void addVariableListener ( final String type, final VariableListener listener )
    {
        this.listeners.put ( type, listener );
        listener.variableConfigurationChanged ( createVariables ( type ) );
    }

    public void removeVariableListener ( final String type, final VariableListener listener )
    {
        this.listeners.remove ( type, listener );
    }

    public void delete ( final String configurationId ) throws Exception
    {
        this.types.remove ( configurationId );
        this.typeDeps.removeAll ( configurationId );
        fireTypeChange ( configurationId );
    }

    private void fireTypeChange ( final String type )
    {
        logger.debug ( "Fire type change: {}", type );

        for ( final VariableListener listener : this.listeners.get ( type ) )
        {
            listener.variableConfigurationChanged ( createVariables ( type ) );
        }
    }

    public void update ( final String configurationId, final Map<String, String> properties ) throws Exception
    {
        logger.debug ( "Adding type: {}", configurationId );

        final Collection<TypeEntry> config = parseConfig ( properties );
        config.addAll ( parseConfig2 ( properties ) );

        this.types.put ( configurationId, config );

        logger.debug ( "Generate deps:" );
        final Set<String> types = new HashSet<String> ();
        for ( final TypeEntry entry : config )
        {
            if ( entry.getType () == TYPE.UDT )
            {
                logger.debug ( "'{}' depends on '{}'", new Object[] { configurationId, entry.getTypeName () } );
                types.add ( entry.getTypeName () );
            }
        }
        this.typeDeps.putAll ( configurationId, types );

        handleTypeChange ( configurationId );
    }

    /**
     * Handle a type change and fire change events for all dependent types
     * @param configurationId
     */
    private void handleTypeChange ( final String configurationId )
    {
        // FIXME: for now re-compile all types
        for ( final String type : this.listeners.keySet () )
        {
            fireTypeChange ( type );
        }
        /*
        fireTypeChange ( configurationId );
        for ( final String type : this.typeDeps.values () )
        {
            logger.debug ( "Trigger dependency: {} (referenced by: {})", new Object[] { type, configurationId } );
            fireTypeChange ( type );
        }
        */
    }

    private Variable[] createVariables ( final String type )
    {
        logger.debug ( "Creating variables for type: {}", type );

        final Collection<TypeEntry> entries = this.types.get ( type );
        if ( entries == null )
        {
            return new Variable[0];
        }
        else
        {
            final Collection<Variable> result = new ArrayList<Variable> ();
            for ( final TypeEntry entry : entries )
            {
                switch ( entry.getType () )
                {
                case BIT:
                    result.add ( new BitVariable ( entry.getName (), entry.getIndex (), entry.getSubIndex (), this.executor, createAttributes ( entry ) ) );
                    break;
                case BYTE:
                    result.add ( new ByteVariable ( entry.getName (), entry.getIndex (), this.executor, createAttributes ( entry ) ) );
                    break;
                case FLOAT:
                    result.add ( new FloatVariable ( entry.getName (), entry.getIndex (), this.executor, createAttributes ( entry ) ) );
                    break;
                case UDT:
                    result.add ( new UdtVariable ( entry.getName (), entry.getIndex (), createVariables ( entry.getTypeName () ) ) );
                    break;
                default:
                    break;
                }
            }
            return result.toArray ( new Variable[0] );
        }
    }

    private Attribute[] createAttributes ( final TypeEntry entry )
    {
        logger.debug ( "Creating attributes for {}", entry );

        final Collection<Attribute> result = new LinkedList<Attribute> ();

        for ( final TypeEntry attrEntry : entry.getAttributes () )
        {
            logger.debug ( "Creating attribute: {}", attrEntry );

            switch ( attrEntry.getType () )
            {
            case BIT:
                result.add ( new BitAttribute ( attrEntry.getName (), attrEntry.getIndex (), attrEntry.getSubIndex () ) );
                break;
            default:
                break;
            }
        }
        return result.toArray ( new Attribute[0] );
    }

    private Collection<TypeEntry> parseConfig2 ( final Map<String, String> properties )
    {
        final Collection<TypeEntry> result = new LinkedList<TypeEntry> ();

        for ( final Map.Entry<String, String> entry : properties.entrySet () )
        {
            final String key = entry.getKey ();
            if ( !key.startsWith ( "variable." ) )
            {
                continue;
            }
            final String varName = key.substring ( "variable.".length () );

            final String toks[] = entry.getValue ().split ( ":" );

            switch ( TYPE.valueOf ( toks[0] ) )
            {
            case BIT:
                result.add ( new TypeEntry ( varName, Integer.parseInt ( toks[1] ), Integer.parseInt ( toks[2] ), parseAttributes ( properties, varName ) ) );
                break;
            case BYTE:
                result.add ( new TypeEntry ( varName, TYPE.BYTE, Integer.parseInt ( toks[1] ), parseAttributes ( properties, varName ) ) );
                break;
            case FLOAT:
                result.add ( new TypeEntry ( varName, TYPE.FLOAT, Integer.parseInt ( toks[1] ), parseAttributes ( properties, varName ) ) );
                break;
            case UDT:
                result.add ( new TypeEntry ( varName, toks[1], Integer.parseInt ( toks[2] ) ) );
                break;
            default:
                break;
            }
        }

        return result;
    }

    private Collection<TypeEntry> parseConfig ( final Map<String, String> properties )
    {
        // 'definition' is: "var1:UDT:test:2 var2:BYTE:1 var3:BIT:1:0"
        // 'attribute.XXX' is attribute for XXX : "attr1:BYTE:1 attr2:BIT:1:0"

        final Collection<TypeEntry> result = new LinkedList<TypeEntry> ();

        final String def = properties.get ( "definition" );

        if ( def == null )
        {
            return result;
        }

        for ( final String tok : def.split ( " " ) )
        {
            final String toks[] = tok.split ( ":" );
            if ( toks.length < 3 )
            {
                // FIXME: throw error
                continue;
            }

            switch ( TYPE.valueOf ( toks[1] ) )
            {
            case BIT:
                result.add ( new TypeEntry ( toks[0], Integer.parseInt ( toks[2] ), Integer.parseInt ( toks[3] ), parseAttributes ( properties, toks[0] ) ) );
                break;
            case BYTE:
                result.add ( new TypeEntry ( toks[0], TYPE.BYTE, Integer.parseInt ( toks[2] ), parseAttributes ( properties, toks[0] ) ) );
                break;
            case FLOAT:
                result.add ( new TypeEntry ( toks[0], TYPE.FLOAT, Integer.parseInt ( toks[2] ), parseAttributes ( properties, toks[0] ) ) );
                break;
            case UDT:
                result.add ( new TypeEntry ( toks[0], toks[2], Integer.parseInt ( toks[3] ) ) );
                break;
            default:
                break;
            }
        }

        return result;
    }

    private TypeEntry[] parseAttributes ( final Map<String, String> properties, final String varName )
    {
        final String definition = properties.get ( "attribute." + varName );

        if ( definition == null )
        {
            logger.debug ( "No attributes for '{}'", varName );
            return new TypeEntry[0];
        }

        logger.debug ( "Attribute definition for '{}': {}", new Object[] { varName, definition } );

        final Collection<TypeEntry> result = new LinkedList<TypeEntry> ();

        for ( final String tok : definition.split ( " " ) )
        {
            final String toks[] = tok.split ( ":" );
            if ( toks.length < 3 )
            {
                // FIXME: throw error
                continue;
            }

            switch ( TYPE.valueOf ( toks[1] ) )
            {
            case BIT:
                result.add ( new TypeEntry ( toks[0], Integer.parseInt ( toks[2] ), Integer.parseInt ( toks[3] ) ) );
                break;
            case BYTE:
                result.add ( new TypeEntry ( toks[0], TYPE.BYTE, Integer.parseInt ( toks[2] ) ) );
                break;
            default:
                break;
            }
        }
        return result.toArray ( new TypeEntry[0] );
    }
}

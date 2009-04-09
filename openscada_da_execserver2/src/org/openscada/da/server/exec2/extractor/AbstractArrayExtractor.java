package org.openscada.da.server.exec2.extractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscada.core.Variant;
import org.openscada.da.server.common.AttributeMode;
import org.openscada.da.server.common.chain.DataItemInputChained;
import org.openscada.da.server.common.item.factory.FolderItemFactory;
import org.openscada.da.server.exec2.Hive;
import org.openscada.da.server.exec2.command.ExecutionResult;

/**
 * Aid in extracing based on arrays
 * @author Jens Reimann
 *
 */
public abstract class AbstractArrayExtractor extends AbstractBaseExtractor
{

    private final List<FieldMapping> fields;

    private final Map<Integer, DataItemInputChained> inputs = new HashMap<Integer, DataItemInputChained> ();

    public enum FieldType
    {
        STRING,
        BOOLEAN,
        INTEGER,
        LONG,
        DOUBLE
    }

    public static class FieldMapping
    {
        private String name;

        private FieldType type;

        public String getName ()
        {
            return this.name;
        }

        public void setName ( final String name )
        {
            this.name = name;
        }

        public FieldType getType ()
        {
            return this.type;
        }

        public void setType ( final FieldType type )
        {
            this.type = type;
        }
    }

    public AbstractArrayExtractor ( final String id, final List<FieldMapping> fields )
    {
        super ( id );
        this.fields = fields;
    }

    @Override
    protected void doProcess ( final ExecutionResult result ) throws Exception
    {
        final String[] fields = getFields ( result );

        for ( int i = 0; i < fields.length; i++ )
        {
            updateData ( fields[i], i );
        }
    }

    protected abstract String[] getFields ( ExecutionResult result );

    @Override
    public void register ( final Hive hive, final FolderItemFactory folderItemFactory )
    {
        super.register ( hive, folderItemFactory );
        int i = 0;
        for ( final FieldMapping group : this.fields )
        {
            final String valueName = group.getName ();
            if ( valueName != null && valueName.length () > 0 )
            {
                this.inputs.put ( i, createInput ( valueName ) );
            }
            i++;
        }
    }

    protected void updateData ( final String value, final int group )
    {
        // get the mapping
        FieldMapping mapping;
        try
        {
            mapping = this.fields.get ( group );
        }
        catch ( final Throwable e )
        {
            return;
        }
        if ( mapping == null )
        {
            return;
        }

        // get the item
        final DataItemInputChained item = getItemForGroup ( group );
        if ( item == null )
        {
            return;
        }

        // create initial attribute settings
        final Map<String, Variant> attributes = new HashMap<String, Variant> ();
        fillNoError ( attributes );
        attributes.put ( "extractor.conversion.error", null );
        attributes.put ( "extractor.conversion.error.message", null );

        // convert the value
        Variant variant;

        try
        {
            switch ( mapping.getType () )
            {
            case BOOLEAN:
                variant = new Variant ( Boolean.parseBoolean ( value ) );
                break;
            case DOUBLE:
                variant = new Variant ( Double.parseDouble ( value ) );
                break;
            case INTEGER:
                variant = new Variant ( Integer.parseInt ( value ) );
                break;
            case LONG:
                variant = new Variant ( Long.parseLong ( value ) );
                break;
            default:
                variant = new Variant ( value );
                break;
            }
        }
        catch ( final Throwable e )
        {
            attributes.put ( "extractor.conversion.error", new Variant ( true ) );
            attributes.put ( "extractor.conversion.error.message", new Variant ( e.getMessage () ) );
            variant = new Variant ();
        }

        item.updateData ( variant, attributes, AttributeMode.UPDATE );
    }

    protected DataItemInputChained getItemForGroup ( final int group )
    {
        return this.inputs.get ( group );
    }

}
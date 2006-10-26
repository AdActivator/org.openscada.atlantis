package org.openscada.da.client.viewer.model.impl.containers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.openscada.da.client.viewer.configurator.ConfigurationError;
import org.openscada.da.client.viewer.model.Connector;
import org.openscada.da.client.viewer.model.Container;
import org.openscada.da.client.viewer.model.DynamicObject;
import org.openscada.da.client.viewer.model.DynamicUIObject;
import org.openscada.da.client.viewer.model.InputDefinition;
import org.openscada.da.client.viewer.model.OutputDefinition;
import org.openscada.da.client.viewer.model.impl.InputExport;
import org.openscada.da.client.viewer.model.impl.OutputExport;
import org.openscada.da.client.viewer.model.impl.figures.BaseFigure;

public class FigureContainer extends BaseFigure implements Container
{
    private Figure _figure = null;
    private List<Connector> _connectors = new LinkedList<Connector> ();
    private Map<String, DynamicObject> _objects = new HashMap<String, DynamicObject> ();
    
    private Map<String,Export> _outputExports = new HashMap<String, Export> ();
    private Map<String,Export> _inputExports = new HashMap<String, Export> ();
    
    public FigureContainer ( String id )
    {
        super ( id );
    }
    
    public void createFigure ( IFigure parent )
    {
        _figure = new Figure ();
        _figure.setLayoutManager ( new XYLayout () );
        
        parent.add ( _figure );
        
        update ();
        
        for ( DynamicObject object : _objects.values () )
        {
            if ( object instanceof DynamicUIObject )
            {
                ((DynamicUIObject)object).createFigure ( _figure );
            }
        }
    }

    public void dispose ()
    {
        // dispose connections
        for ( Connector connector : _connectors )
        {
            connector.dispose ();
        }
        
        // dispose objects
        for ( DynamicObject object : _objects.values () )
        {
            object.dispose ();
        }
        _objects.clear ();
        
        // dispose gui stuff
        if ( _figure != null )
        {
            _figure.getParent ().remove ( _figure );
            _figure = null;
        }
    }

    public void add ( DynamicObject object )
    {
        _objects.put ( object.getId (), object );
        if ( _figure != null )
        {
            if ( object instanceof DynamicUIObject )
            {
                ((DynamicUIObject)object).createFigure ( _figure );
            }
        }
    }

    public void remove ( DynamicObject object )
    {
        // FIXME: cannot use getFigure anymore!!
        _objects.remove ( object.getId () );
        if ( _figure != null )
        {
            if ( object instanceof DynamicUIObject )
            {
                ((DynamicUIObject)object).dispose ();
            }
        }
    }
    
    public Collection<DynamicObject> getObjects ()
    {
        return Collections.unmodifiableCollection ( _objects.values () );
    }
    
    protected void update ()
    {
        if ( _figure == null )
            return;
        
        updateFigure ( _figure );
    }

    public void addInputExport ( Export export ) throws ConfigurationError
    {
        DynamicObject object = _objects.get ( export.getObject () );
        if ( object == null )
            throw new ConfigurationError ( String.format ( "Unable to export input since object %s is unknown", export.getObject () ) );
         
        InputDefinition inputDef = object.getInputByName ( export.getName () );
        if ( inputDef == null )
            throw new ConfigurationError ( String.format ( "Unable to export input since input %s of object %s is unknown", export.getName (), export.getObject () ) );
        
        addInput ( new InputExport ( inputDef, export.getAlias () ) );
    }

    public void addOutputExport ( Export export ) throws ConfigurationError
    {
        DynamicObject object = _objects.get ( export.getObject () );
        if ( object == null )
            throw new ConfigurationError ( String.format ( "Unable to export output since object %s is unknown", export.getObject () ) );
         
        OutputDefinition outputDef = object.getOutputByName ( export.getName () );
        if ( outputDef == null )
            throw new ConfigurationError ( String.format ( "Unable to export input since output %s of object %s is unknown", export.getName (), export.getObject () ) );
        
        addOutput ( new OutputExport ( outputDef, export.getAlias () ) );
    }

    public void add ( Connector connector )
    {
        _connectors.add ( connector );
    }

    public Collection<Connector> getConnectors ()
    {
        return _connectors;
    }

    public void remove ( Connector connector )
    {
        _connectors.remove ( connector );
    }


    public Collection<Export> getInputExports ()
    {
        return _inputExports.values ();
    }

    public Collection<Export> getOutputExports ()
    {
        return _outputExports.values ();
    }

    public synchronized void removeInputExport ( String exportName )
    {
        if ( _inputExports.remove ( exportName ) != null )
        {
            removeInput ( exportName );
        }
    }

    public void removeOutputExport ( String exportName )
    {
        if ( _outputExports.remove ( exportName ) != null )
        {
            removeOutput ( exportName );
        }        
    }

}

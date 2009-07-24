package org.openscada.da.server.simulation.scriptomatic.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.openscada.da.server.simulation.scriptomatic.Hive;
import org.openscada.da.server.simulation.scriptomatic.ItemDefinition;

public class OpenOfficeLoader
{
    private final Hive hive;

    public OpenOfficeLoader ( final Hive hive )
    {
        this.hive = hive;
    }

    public void load ( final File file ) throws IOException
    {
        final SpreadSheet spreadSheet = SpreadSheet.createFromFile ( file );
        for ( int i = 0; i < spreadSheet.getSheetCount (); i++ )
        {
            loadSheet ( spreadSheet.getSheet ( 0 ) );
        }
    }

    private void loadSheet ( final Sheet sheet ) throws FileNotFoundException
    {
        for ( int i = 1; i < sheet.getRowCount (); i++ )
        {
            loadRow ( sheet, i );
        }
    }

    private void loadRow ( final Sheet sheet, final int row ) throws FileNotFoundException
    {
        final String itemId = (String)sheet.getValueAt ( 0, row );
        final Object cycleTimeSpec = sheet.getValueAt ( 1, row );
        final Object initCode = getCode ( sheet, 3, row );
        final Object cycleCode = getCode ( sheet, 4, row );
        final Object triggerCode = getCode ( sheet, 5, row );

        final long cycleTime;
        if ( cycleTimeSpec instanceof Number )
        {
            cycleTime = ( (Number)cycleTimeSpec ).longValue ();
        }
        else
        {
            cycleTime = 0;
        }

        this.hive.addItemDefinition ( new ItemDefinition ( itemId, "javascript", triggerCode, initCode, cycleCode, cycleTime, true ) );
    }

    private Object getCode ( final Sheet sheet, final int i, final int row ) throws FileNotFoundException
    {
        final Object value = sheet.getValueAt ( i, row );
        if ( value == null )
        {
            return null;
        }
        if ( value instanceof String )
        {
            final String strValue = (String)value;
            if ( strValue.startsWith ( "@@" ) )
            {
                final String fileName = strValue.substring ( 2 );
                return new File ( fileName );
            }
            else
            {
                return strValue;
            }
        }
        return null;
    }
}

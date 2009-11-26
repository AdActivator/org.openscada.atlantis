package org.openscada.da.server.dave;

import org.openscada.utils.lang.Immutable;

@Immutable
public class BlockConfiguration
{
    private String name;

    private int area;

    private int block;

    private int start;

    private int count;

    private String daveDevice;

    private String type;

    public BlockConfiguration ()
    {
    }

    public BlockConfiguration ( final String daveDevice, final String name, final String type, final int area, final int block, final int start, final int count )
    {
        this.daveDevice = daveDevice;
        this.type = type;
        this.name = name;
        this.area = area;
        this.block = block;
        this.start = start;
        this.count = count;
    }

    public String getType ()
    {
        return this.type;
    }

    public void setType ( final String type )
    {
        this.type = type;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public int getArea ()
    {
        return this.area;
    }

    public void setArea ( final int area )
    {
        this.area = area;
    }

    public int getBlock ()
    {
        return this.block;
    }

    public void setBlock ( final int block )
    {
        this.block = block;
    }

    public int getStart ()
    {
        return this.start;
    }

    public void setStart ( final int start )
    {
        this.start = start;
    }

    public int getCount ()
    {
        return this.count;
    }

    public void setCount ( final int count )
    {
        this.count = count;
    }

    public String getDaveDevice ()
    {
        return this.daveDevice;
    }

    public void setDaveDevice ( final String daveDevice )
    {
        this.daveDevice = daveDevice;
    }

    @Override
    public String toString ()
    {
        return String.format ( "{device: %s, name: %s, area: 0x%02x, block: %s, start: %s, count: %s}", this.daveDevice, this.name, this.area, this.block, this.start, this.count );
    }

}

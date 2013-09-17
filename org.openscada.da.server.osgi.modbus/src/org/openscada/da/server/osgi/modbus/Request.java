package org.openscada.da.server.osgi.modbus;

public class Request
{
    private final int startAddress;

    private final int count;

    private final long period;

    private final String mainTypeName;

    private final long timeout;

    private final RequestType type;

    public Request ( final RequestType type, final int startAddress, final int count, final long period, final long timeout, final String mainTypeName )
    {
        this.type = type;
        this.startAddress = startAddress;
        this.count = count;
        this.period = period;
        this.timeout = timeout;
        this.mainTypeName = mainTypeName;
    }

    public String getMainTypeName ()
    {
        return this.mainTypeName;
    }

    public long getPeriod ()
    {
        return this.period;
    }

    public long getTimeout ()
    {
        return this.timeout;
    }

    public int getStartAddress ()
    {
        return this.startAddress;
    }

    public int getCount ()
    {
        return this.count;
    }

    public RequestType getType ()
    {
        return this.type;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.count;
        result = prime * result + ( this.mainTypeName == null ? 0 : this.mainTypeName.hashCode () );
        result = prime * result + (int) ( this.period ^ this.period >>> 32 );
        result = prime * result + this.startAddress;
        result = prime * result + (int) ( this.timeout ^ this.timeout >>> 32 );
        result = prime * result + ( this.type == null ? 0 : this.type.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass () != obj.getClass () )
        {
            return false;
        }
        final Request other = (Request)obj;
        if ( this.count != other.count )
        {
            return false;
        }
        if ( this.mainTypeName == null )
        {
            if ( other.mainTypeName != null )
            {
                return false;
            }
        }
        else if ( !this.mainTypeName.equals ( other.mainTypeName ) )
        {
            return false;
        }
        if ( this.period != other.period )
        {
            return false;
        }
        if ( this.startAddress != other.startAddress )
        {
            return false;
        }
        if ( this.timeout != other.timeout )
        {
            return false;
        }
        if ( this.type != other.type )
        {
            return false;
        }
        return true;
    }

}

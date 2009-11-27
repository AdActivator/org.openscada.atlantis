package org.openscada.da.server.dave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.openscada.protocols.dave.DaveMessage;
import org.openscada.protocols.dave.DaveReadRequest;
import org.openscada.protocols.dave.DaveReadResult;
import org.openscada.protocols.dave.DaveWriteRequest;
import org.openscada.protocols.dave.DaveReadResult.Result;
import org.openscada.utils.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaveJobManager
{
    private final static Logger logger = LoggerFactory.getLogger ( DaveJobManager.class );

    private IoSession session;

    private final Map<String, DaveRequestBlock> blocks = new HashMap<String, DaveRequestBlock> ();

    private final ScheduledExecutorService executor;

    private ScheduledFuture<?> job;

    private Job currentJob;

    private final Queue<Job> writeQueue = new LinkedList<Job> ();

    private static interface Job
    {
        public void handleMessage ( final DaveMessage message );

        public void start ( IoSession session );
    }

    private static class ReadJob implements Job
    {
        private final DaveRequestBlock block;

        public ReadJob ( final DaveRequestBlock block )
        {
            this.block = block;
        }

        public void handleMessage ( final DaveMessage message )
        {
            if ( message instanceof DaveReadResult )
            {
                for ( final Result result : ( (DaveReadResult)message ).getResult () )
                {
                    this.block.handleResponse ( result );
                }
            }
        }

        public void start ( final IoSession session )
        {
            final DaveReadRequest request = new DaveReadRequest ();
            request.addRequest ( this.block.getRequest () );
            session.write ( request );
        }

    }

    private static class WriteJob implements Job
    {
        private final DaveWriteRequest request;

        public WriteJob ( final DaveWriteRequest request )
        {
            this.request = request;
        }

        public void start ( final IoSession session )
        {
            session.write ( this.request );
        }

        public void handleMessage ( final DaveMessage message )
        {
            // TODO: no-op for now
        }
    }

    public DaveJobManager ( final DaveDevice device )
    {
        this.executor = Executors.newSingleThreadScheduledExecutor ( new NamedThreadFactory ( "DaveJobManager/" + device.getId () ) );
    }

    public void setSession ( final IoSession session )
    {
        logger.debug ( "Setting session: {}", session );

        this.session = session;
        setTimerState ( session != null );
        if ( session == null )
        {
            this.currentJob = null;
            // discard write requests
            this.writeQueue.clear ();
            // handle data disconnect
            handleDataDisconnected ();
        }
    }

    private void setTimerState ( final boolean flag )
    {
        final boolean currentState = this.job != null;

        if ( currentState == flag )
        {
            logger.info ( "Timer is in correct state: {} / {}", new Object[] { currentState, flag } );
            return;
        }

        if ( flag )
        {
            logger.info ( "Starting timer" );
            this.job = this.executor.scheduleWithFixedDelay ( new Runnable () {

                public void run ()
                {
                    DaveJobManager.this.tick ();
                }
            }, 0, 100, TimeUnit.MILLISECONDS );
        }
        else
        {
            logger.info ( "Stopping timer" );
            this.job.cancel ( false );
            this.job = null;
        }
    }

    public void messageReceived ( final DaveMessage message )
    {
        if ( this.currentJob != null )
        {
            try
            {
                this.currentJob.handleMessage ( message );
            }
            finally
            {
                this.currentJob = null;
            }
        }
    }

    protected void tick ()
    {
        if ( this.currentJob != null )
        {
            return;
        }

        this.currentJob = getNextWriteJob ();
        if ( this.currentJob == null )
        {
            this.currentJob = getNextReadJob ();
        }
        if ( this.currentJob != null )
        {
            this.currentJob.start ( this.session );
        }
    }

    /**
     * Get the next read job
     * @return the next read job or <code>null</code> if no blocks need to be refreshed
     */
    private Job getNextReadJob ()
    {
        final List<DaveRequestBlock> blocks = new ArrayList<DaveRequestBlock> ( this.blocks.values () );
        Collections.sort ( blocks, new Comparator<DaveRequestBlock> () {

            public int compare ( final DaveRequestBlock o1, final DaveRequestBlock o2 )
            {
                final long l1 = o1.updatePriority ();
                final long l2 = o2.updatePriority ();
                return (int) ( l2 - l1 );
            }
        } );

        if ( !blocks.isEmpty () )
        {
            return new ReadJob ( blocks.get ( 0 ) );
        }
        else
        {
            return null;
        }
    }

    /**
     * Get the next job from the write queue if there is any
     * @return the next write job or <code>null</code> if there is none
     */
    private Job getNextWriteJob ()
    {
        return this.writeQueue.poll ();
    }

    public void dispose ()
    {
        for ( final DaveRequestBlock block : this.blocks.values () )
        {
            block.dispose ();
        }
        this.executor.shutdown ();
    }

    protected void handleDataDisconnected ()
    {
        for ( final DaveRequestBlock block : this.blocks.values () )
        {
            block.handleDisconnect ();
        }
    }

    public void addBlock ( final String id, final DaveRequestBlock block )
    {
        logger.debug ( "Adding block: {}", id );

        if ( this.blocks.containsKey ( id ) )
        {
            throw new IllegalArgumentException ( String.format ( "Block '%s' is already registered with device", id ) );
        }

        this.blocks.put ( id, block );
    }

    public void removeBlock ( final String id )
    {
        logger.debug ( "Removing block: {}", id );

        final DaveRequestBlock oldBlock = this.blocks.remove ( id );
        if ( oldBlock != null )
        {
            oldBlock.dispose ();
        }
    }

    public void addWriteRequest ( final DaveWriteRequest request )
    {
        this.writeQueue.add ( new WriteJob ( request ) );
    }

}

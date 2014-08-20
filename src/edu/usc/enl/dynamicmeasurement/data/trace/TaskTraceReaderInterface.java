package edu.usc.enl.dynamicmeasurement.data.trace;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/12/13
 * Time: 10:06 PM <br/>
 * It is assigned to a task to feed it with its traces
 */
public interface TaskTraceReaderInterface {
    /**
     * Give me the next packet to process
     *
     * @param p
     * @return
     * @throws DataPacket.PacketParseException
     * @throws IOException
     */
    public DataPacket getNextPacket(DataPacket p) throws DataPacket.PacketParseException, IOException;

    public void finish();

    /**
     * It is enough keep this packet also for the next epoch
     *
     * @param p
     */
    public void keepForNext(DataPacket p);
}

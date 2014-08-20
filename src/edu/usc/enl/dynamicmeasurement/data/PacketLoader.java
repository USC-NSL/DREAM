package edu.usc.enl.dynamicmeasurement.data;

import edu.usc.enl.dynamicmeasurement.model.Packet;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/24/13
 * Time: 9:50 AM  <br/>
 * A simple class that can read traces and make data packets and put into a queue.
 * It is mostly used for just trace analysis scripts
 */
public class PacketLoader extends Thread {
    private final TraceReader br;
    private final long maxTime;
    private final BlockingQueue<Packet> queue;
    private boolean finish = false;

    public PacketLoader(BlockingQueue<Packet> queue, TraceReader br) {
        this(queue, br, 0);
    }


    public PacketLoader(BlockingQueue<Packet> queue, TraceReader br, long maxTime) {
        this.br = br;
        this.maxTime = maxTime;
        this.queue = queue;
    }

    public void run() {
        long start = -1;
        long lastTime = 0;
        try (TraceReader br2 = br) {
            while (true) {
                String line = br2.readLine();
                if (line == null) {
                    break;
                }
                DataPacket p = null;
                try {
                    p = new DataPacket(new StringTokenizer(line, ","));
                } catch (DataPacket.PacketParseException e) {
                    System.out.println(line);
                    throw e;
                }
                lastTime = p.getTime();
                queue.put(p);
                if (finish) {
                    queue.put(new FinishPacket(lastTime));
                    return;
                }
                if (start < 0) {
                    start = p.getTime();
                } else {
                    if (maxTime > 0 && p.getTime() - start > maxTime) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                queue.put(new FinishPacket(lastTime));
            } catch (InterruptedException e) {
            }
        }
    }

    public void finish() {
        finish = true;
        queue.clear();
    }

    /**
     * A traceReader is a simple class that can read files one line at a time
     */
    public interface TraceReader extends AutoCloseable {
        public void close() throws IOException;

        /**
         * @return null if the end of traces
         */
        public String readLine() throws IOException;

    }
}

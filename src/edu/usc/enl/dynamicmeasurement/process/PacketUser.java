package edu.usc.enl.dynamicmeasurement.process;

import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.Packet;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/31/13
 * Time: 7:03 PM  <br/>
 * Any class that wants to use a stream of packets can implement this.
 * It can either be used as a thread that reads from a blocking queue or run separately
 */
public abstract class PacketUser extends Thread{
    /**
     * gather incoming packets
     */
    private final BlockingQueue<Packet> queue;
    public static final int Q_CAPACITY = 1000;

    public PacketUser() {
        this.queue = new ArrayBlockingQueue<>(Q_CAPACITY);
    }

    /**
     * pass this queue to the a PacketLoader thread to fill it.
     *
     * @return
     */
    public BlockingQueue<Packet> getQueue() {
        return queue;
    }

    @Override
    public final void run() {
        try {
            while (true) {
                Packet p = queue.take();
                if (p instanceof FinishPacket) {
                    finish((FinishPacket) p);
                    break;
                }
                process(p);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract void process(Packet p);

    public abstract void finish(FinishPacket p);
}

package edu.usc.enl.dynamicmeasurement.process;

import edu.usc.enl.dynamicmeasurement.data.MultiFileTraceReader;
import edu.usc.enl.dynamicmeasurement.data.PacketLoader;

import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/25/13
 * Time: 3:01 PM <br/>
 * A very simple class that matches a loader and a user and runs them.
 * This is mustly used for debugging
 */
public class Simulator {
    private final long maxTime;

    public Simulator(long maxTime) {
        this.maxTime = maxTime;
    }

    public void run(String[] traceFiles, PacketUser user) throws FileNotFoundException {
        PacketLoader loader = new PacketLoader(user.getQueue(), new MultiFileTraceReader(traceFiles), maxTime);
        run(user, loader);
    }

    public void run(PacketUser user, PacketLoader loader) throws FileNotFoundException {

        long time = System.currentTimeMillis();

        loader.start();
        user.start();


        try {
            loader.join();
            user.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(System.currentTimeMillis() - time);
    }
}

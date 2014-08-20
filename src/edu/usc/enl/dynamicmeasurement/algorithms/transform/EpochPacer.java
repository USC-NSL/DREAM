package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/4/13
 * Time: 4:58 PM <br/>
 * A special packet pipe that generates epoch messages based on the timestamp of input packets.
 * It uses the timestamp of the first packet as the base and generates steps if the incoming packets have timestamp
 * diff larger than stepsDuration
 */
public class EpochPacer extends PacketPipe {
    private long stepsDuration = -1;
    private long lastTime = -1;
    private int step = 0;

    public EpochPacer(PacketUser nextUser, long stepsDuration) {
        super(nextUser);
        this.stepsDuration = stepsDuration;
    }

    @Override
    protected void process2(DataPacket p) {
        if (lastTime < 0) {
            lastTime = (p.getTime() / stepsDuration) * stepsDuration;
        }
        while (p.getTime() >= lastTime + stepsDuration) {
//            System.out.println("Epoch: " + step);
            passPacket(new EpochPacket(lastTime + stepsDuration, step));
            lastTime = lastTime + stepsDuration;
            step++;
        }
        passPacket(p);
    }

    @Override
    protected void step(EpochPacket p) {
        System.err.println("Epoch pacer got Epoch packet!! " + p);
    }

    @Override
    public void finish(FinishPacket p) {
        //System.out.println("Epoch: " + step);
        passPacket(new EpochPacket(p.getTime(), step));
        super.finish(p);
    }
}

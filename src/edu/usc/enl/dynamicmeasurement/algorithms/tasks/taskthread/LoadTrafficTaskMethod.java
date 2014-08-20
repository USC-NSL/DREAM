package edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.TrafficTransformer;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.TransformHandler;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.data.trace.TaskTraceReaderInterface;
import edu.usc.enl.dynamicmeasurement.model.Packet;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.util.Util;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/23/13
 * Time: 6:41 PM <br/>
 * This is the actual method that loads traffic and matches against packet users of a task in the simulation
 */
public class LoadTrafficTaskMethod implements Runnable {
    private Task2 task;
    private EpochPacket stepPacket;
    private TransformHandler transformHandler;
    private DataPacket p = null;

    public LoadTrafficTaskMethod(Task2 task) {
        this.task = task;
        transformHandler = new TransformHandler(new TransformPacketUser());
    }

    public void setEpoch(EpochPacket p) {
        this.stepPacket = p;
    }

    @Override
    public void run() {
        int epochSize = (int) Util.getSimulationConfiguration().getEpoch();
        try {
            int num = 0;
            while (true) {
                //load task traffic from
                TaskTraceReaderInterface traceReader = task.getTraceReader();
                p = traceReader.getNextPacket(p);
                if (p != null) {
                    if (p.getTime() < stepPacket.getTime() + epochSize) {//load traffic until the next epoch
                        transformHandler.process(p);
                        num++;
                    } else {
                        traceReader.keepForNext(p);
                        break;
                    }
                } else {
                    break;
                }
            }
            transformHandler.process(stepPacket);
        } catch (DataPacket.PacketParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public void addTransform(TrafficTransformer transformer) {
        transformHandler.addTransform(transformer);
    }

    public void removeTransform(String name) {
        transformHandler.removeTransform(name);
    }

    class TransformPacketUser extends PacketUser {

        @Override
        public void process(Packet p) {
            if (p instanceof DataPacket) {
                task.process((DataPacket) p);
            }//in case of epoch the task handler must run its step
        }

        @Override
        public void finish(FinishPacket p) {
            task.finish(p);
        }
    }
}

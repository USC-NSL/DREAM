package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 2:33 PM  <br/>
 * A simple traffic transformer handler.
 * Inside it has a pipe of transformers.
 * Once the pipe finishes processing the packet it passes it to the next user.
 * The order of transformers in the pipe is FIFO
 */
public class TransformHandler extends PacketPipe implements TransformHandlerInterface {
    private LinkedList<TrafficTransformer> transforms;

    public TransformHandler(PacketUser nextUser) {
        super(nextUser);
        transforms = new LinkedList<>();
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        if (transforms.size() == 0 || transforms.getLast().sent(p)) {
            passPacket(p);
        } else {
            transforms.getFirst().process(p);
        }
    }

    @Override
    protected void step(EpochPacket p) {
        if (transforms.size() == 0 || transforms.getLast().sent(p)) {
            passPacket(p);
        } else {
            transforms.getFirst().process(p);
        }
    }

    public void addTransform(TrafficTransformer t) {
        if (transforms.size() > 0) {
            transforms.getLast().setNextUser(t);
        }
        transforms.add(t);
        t.setNextUser(this);
    }

    public void removeTransform(String transformName) {
        TrafficTransformer last = null;
        for (Iterator<TrafficTransformer> iterator = transforms.iterator(); iterator.hasNext(); ) {
            TrafficTransformer transform = iterator.next();
            if (transform.getName2().equals(transformName)) {
                iterator.remove();
                if (last != null) {
                    last.setNextUser(iterator.hasNext() ? iterator.next() : this);
                }
                return;
            }
            last = transform;
        }
//        System.err.println("Transform " + transformName + " not found");
    }
}

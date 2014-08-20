package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.Packet;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 7/25/13
 * Time: 1:44 PM  <br/>
 * The subclasses are supposed to pass the packets to the next user.
 * Note that the pipe may generate packets or filter some.
 */
public abstract class PacketPipe extends StepPacketUser {
    protected PacketUser nextUser;

    protected PacketPipe() {
        super();
    }

    public PacketPipe(PacketUser nextUser) {
        super();
        this.nextUser = nextUser;
    }

    public void setNextUser(PacketUser nextUser) {
        this.nextUser = nextUser;
    }

    public void finish(FinishPacket p) {
        nextUser.finish(p);
    }

    /**
     * Pass packet to the next user
     *
     * @param p
     */
    protected void passPacket(Packet p) {
        nextUser.process(p);
    }

    protected abstract void process2(DataPacket p);

}

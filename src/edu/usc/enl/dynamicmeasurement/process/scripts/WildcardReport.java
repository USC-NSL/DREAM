package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.Packet;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/31/13
 * Time: 6:06 PM
 */
public class WildcardReport extends PacketUser {
    private final Set<WildcardPattern> wildcards;

    public WildcardReport() {
        wildcards = new HashSet<>();
    }

    public Set<WildcardPattern> getWildcards() {
        return wildcards;
    }

    @Override
    public void process(Packet p) {
        if (p instanceof DataPacket) {
            long srcIP = ((DataPacket) p).getSrcIP();
            for (int i = 0; i < WildcardPattern.TOTAL_LENGTH + 1; i++) {
                wildcards.add(new WildcardPattern(srcIP >>> i, i, 0));
            }
        }
    }

    @Override
    public void finish(FinishPacket p) {

    }


}

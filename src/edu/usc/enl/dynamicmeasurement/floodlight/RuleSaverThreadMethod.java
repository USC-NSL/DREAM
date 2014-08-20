package edu.usc.enl.dynamicmeasurement.floodlight;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import net.floodlightcontroller.core.IOFSwitch;
import org.openflow.protocol.*;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/18/13
 * Time: 3:57 PM <br/>
 * Tries to save rules in a single switch
 */
class RuleSaverThreadMethod implements Runnable {
    public static final WildcardPattern DEFAULT_PATTERN = new WildcardPattern(0, WildcardPattern.TOTAL_LENGTH, 0);
    private final SwitchData sw;
    private final Map<WildcardPattern, WildcardPattern> toSaveRules;
    /**
     * Keeps track of the last fetched rules, to not save rules that have not changed.
     */
    private final Map<WildcardPattern, WildcardPattern> lastFetchedRules;
    private final List<OFMessage> deleteFlows;
    private final List<OFMessage> installFlows;
    private final OFFlowMod defaultFlowMod;
    private int epoch;

    //Statistics information
    private int toDelete;
    private int notInstall;
    private int toInstall;

    public RuleSaverThreadMethod(SwitchData aSwitch, int capacity, Map<WildcardPattern, WildcardPattern> toSaveRules,
                                 Map<WildcardPattern, WildcardPattern> lastFetchedRules) {
        this.sw = aSwitch;
        this.toSaveRules = toSaveRules;
        this.lastFetchedRules = lastFetchedRules;
        deleteFlows = new ArrayList<>();
        WildcardPattern pattern = new WildcardPattern(0, 0, 0);
        for (int i = 0; i < capacity; i++) {
            deleteFlows.add(getDeleteRule(pattern, sw.getFlow()));
        }
        defaultFlowMod = getOfFlowMod(DEFAULT_PATTERN, sw, true);
        installFlows = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            installFlows.add(getOfFlowMod(pattern, sw, true));
        }
    }

    public static OFFlowMod getOfFlowMod(WildcardPattern pattern, SwitchData sw, boolean denyAccept) {
        OFFlowMod flowMod = sw.getFlow();
        long srcIP = pattern.getData() << pattern.getWildcardNum();
        //                long dstIP=1;
        int srcIPWildcardNum = pattern.getWildcardNum();
        flowMod.setCommand(OFFlowMod.OFPFC_ADD);
        flowMod.setFlags((short) 0);
        OFMatch match = new OFMatch();
        match.setNetworkSource((int) srcIP);
        //                match.setNetworkDestination(IPv4.toIPv4Address(dstIP));
        if (srcIPWildcardNum == WildcardPattern.TOTAL_LENGTH) {
            match.setWildcards(Wildcards.FULL);
            flowMod.setPriority(((short) (1)));
        } else {
            match.setWildcards(Wildcards.FULL.matchOn(Wildcards.Flag.DL_TYPE).withNwSrcMask(WildcardPattern.TOTAL_LENGTH - srcIPWildcardNum));
            match.setDataLayerType(Short.parseShort("800", 16));
            flowMod.setPriority(((short) (10)));
        }
        flowMod.setMatch(match);
        flowMod.setOutPort(OFPort.OFPP_NONE.getValue());
        flowMod.setBufferId(OFPacketOut.BUFFER_ID_NONE);
        if (denyAccept) {
            flowMod.setActions(new ArrayList<OFAction>());
            flowMod.setLength((short) (OFFlowMod.MINIMUM_LENGTH));
        } else {
            List<OFAction> actions = new ArrayList<OFAction>();
            if (sw.getSw().hasAttribute(IOFSwitch.PROP_SUPPORTS_OFPP_FLOOD)) {
                actions.add(new OFActionOutput(OFPort.OFPP_FLOOD.getValue(),
                        (short) 0xFFFF));
            } else {
                actions.add(new OFActionOutput(OFPort.OFPP_ALL.getValue(),
                        (short) 0xFFFF));
            }
            flowMod.setActions(actions);
            flowMod.setLength((short) (OFFlowMod.MINIMUM_LENGTH + OFActionOutput.MINIMUM_LENGTH));
        }

        flowMod.setHardTimeout((short) 0);
        flowMod.setIdleTimeout((short) 0);

        return flowMod;
    }

    public static OFFlowMod getDeleteRule(WildcardPattern pattern, OFFlowMod flowMod) {
        int srcIPWildcardNum = pattern.getWildcardNum();
        OFMatch match = new OFMatch();
        long srcIP = pattern.getData() << pattern.getWildcardNum();
        match.setNetworkSource((int) srcIP);
        if (srcIPWildcardNum == WildcardPattern.TOTAL_LENGTH) {
            match.setWildcards(Wildcards.FULL);
            flowMod.setPriority(((short) (1)));
        } else {
            match.setWildcards(Wildcards.FULL.matchOn(Wildcards.Flag.DL_TYPE).withNwSrcMask(WildcardPattern.TOTAL_LENGTH - srcIPWildcardNum));
            match.setDataLayerType(Short.parseShort("800", 16));
            flowMod.setPriority(((short) (10)));
        }
        flowMod.setMatch(match);
        flowMod.setOutPort(OFPort.OFPP_NONE);
        flowMod.setCommand(OFFlowMod.OFPFC_DELETE);
        return flowMod;
    }

    @Override
    public void run() {

        try {
            //find rules that must be deleted
            toDelete = 0;
            notInstall = 0;
            toInstall = 0;
            toSaveRules.put(DEFAULT_PATTERN, DEFAULT_PATTERN);
            {
                for (WildcardPattern wildcardPattern : lastFetchedRules.keySet()) {
                    if (!toSaveRules.containsKey(wildcardPattern)) {
//                    System.out.println(sw + " delete " + wildcardPattern);
                        //toDeleteFlowMods.add(getDeleteRule(wildcardPattern));
                        changeDeleteRule(wildcardPattern, (OFFlowMod) deleteFlows.get(toDelete++));
                    }
                }
                if (toDelete > 0) {
                    sw.write(deleteFlows.subList(0, toDelete));
                    sw.sendBarrier();
                }
            }
            {
                if (lastFetchedRules.size() == 0) {
                    sw.write(defaultFlowMod);
                } else {

                    for (WildcardPattern pattern : toSaveRules.keySet()) {
                        if (!lastFetchedRules.containsKey(pattern)) {
                            changeDeleteRule(pattern, (OFFlowMod) installFlows.get(toInstall++));
                        } else {
                            notInstall++;
                        }
                    }
                    sw.write(installFlows.subList(0, toInstall));
                }
                sw.sendBarrier();
                sw.getLogWriter().println(epoch + "," + toDelete + "," + notInstall + "," + toInstall);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sw.flush();
    }

    private void changeDeleteRule(WildcardPattern pattern, OFFlowMod mod) {
        int srcIPWildcardNum = pattern.getWildcardNum();
        long srcIP = pattern.getData() << pattern.getWildcardNum();
        OFMatch match = mod.getMatch();
        match.setNetworkSource((int) srcIP);
        match.setWildcards(Wildcards.FULL.matchOn(Wildcards.Flag.DL_TYPE).withNwSrcMask(WildcardPattern.TOTAL_LENGTH - srcIPWildcardNum));
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }
}

package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/10/13
 * Time: 5:15 PM
 */
public class TrimmedTrafficReport extends StepPacketUser {
    private final int levels;
    private final Map<WildcardPattern, WildcardPattern> snapshot;
    private final PrintWriter pw;
    private double sum = 0;

    public TrimmedTrafficReport(boolean resetOnStep, int levels, PrintWriter pw) {
        super();
        this.levels = levels;
        this.pw = pw;
        snapshot = new HashMap<>(1 << (levels + 1), 1);
        for (int i = 0; i < (1 << levels); i++) {
            WildcardPattern w = new WildcardPattern(i, levels, 0);
            snapshot.put(w, w);
            while (w.canGoUp()) {
                if (w.isLeft()) {
                    w = w.clone().goUp();
                    snapshot.put(w, w);
                } else {
                    break;
                }
            }
        }
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        long srcIP = p.getSrcIP();
        srcIP >>>= levels;
        for (int i = 0; i < levels; i++) {
            WildcardPattern w = snapshot.get(new WildcardPattern(srcIP, levels + i, 0));
            w.setWeight(w.getWeight() + p.getSize());
            srcIP >>>= 1;
        }
    }

    @Override
    protected void step(EpochPacket p) {
        //write the snapshot to file
        for (WildcardPattern wildcardPattern : snapshot.keySet()) {
            pw.println(p.getStep() + "," +
                    ((wildcardPattern.getData() << wildcardPattern.getWildcardNum()) * 100 + wildcardPattern.getWildcardNum()) + "," +
                    wildcardPattern.getWildcardNum() + "," +
                    wildcardPattern.getWeight());
        }
        pw.flush();
    }

    @Override
    public void finish(FinishPacket p) {

    }
}

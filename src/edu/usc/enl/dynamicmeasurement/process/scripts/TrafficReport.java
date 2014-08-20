package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/31/13
 * Time: 7:02 PM
 */
public class TrafficReport extends StepPacketUser {
    private Map<WildcardPattern, WildcardPattern> snapshot = new HashMap<>();
    private final PrintWriter pw;
    private double sum = 0;

    public TrafficReport(boolean resetOnStep, Set<WildcardPattern> wildcards,
                         String outputFile) throws IOException {
        super();
        snapshot = new HashMap<>(wildcards.size());
        for (WildcardPattern wildcard : wildcards) {
            snapshot.put(wildcard, wildcard);
        }
        new File(outputFile).getParentFile().mkdirs();
        pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
    }

    @Override
    public void finish(FinishPacket p) {
        pw.close();
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        long srcIP = p.getSrcIP();
        sum += p.getSize();
        for (int i = 0; i < WildcardPattern.TOTAL_LENGTH + 1; i++) {
            WildcardPattern wildcardPattern = snapshot.get(new WildcardPattern(srcIP >>> i, i, 0));
            if (wildcardPattern != null) {
                wildcardPattern.setWeight(wildcardPattern.getWeight() + p.getSize());
            }
        }
    }

    @Override
    protected void step(EpochPacket p) {
        //write the snapshot to file
        for (WildcardPattern wildcardPattern : snapshot.keySet()) {
            pw.println(p.getStep() + "," +
                    ((wildcardPattern.getData() << (wildcardPattern.getWildcardNum() + 6)) + wildcardPattern.getWildcardNum()) + "," +
                    wildcardPattern.getWildcardNum() + "," +
                    wildcardPattern.getWeight());
        }
        pw.flush();
        for (WildcardPattern wildcardPattern : snapshot.keySet()) {
            wildcardPattern.setWeight(0);
        }
        System.out.println(p.getStep() + "," + sum);
    }

    @Override
    protected void reset() {
        super.reset();

        sum = 0;
    }
}

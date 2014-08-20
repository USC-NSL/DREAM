package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.algorithms.matcher.HashMatcher;
import edu.usc.enl.dynamicmeasurement.algorithms.matcher.Matcher;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/26/13
 * Time: 5:49 PM
 */
public class SumReportPrefixesWrite extends StepPacketUser {
    private final Map<WildcardPattern, PrintWriter> wildcardPatterns;
    private Matcher matcher;

    public SumReportPrefixesWrite(List<WildcardPattern> wildcardPatterns, String outputFolder) {
        matcher = new HashMatcher();
        this.wildcardPatterns = new HashMap<>();
        matcher.setMonitors(wildcardPatterns);
        new File(outputFolder).mkdirs();
        try {
            for (WildcardPattern wildcardPattern : wildcardPatterns) {
                this.wildcardPatterns.put(wildcardPattern, new PrintWriter(new BufferedWriter(new FileWriter(outputFolder + "/" + wildcardPattern.toStringNoWeight()))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process2(DataPacket p) {
        WildcardPattern match = matcher.match(p.getSrcIP());
        match.setWeight(match.getWeight() + p.getSize());
    }

    @Override
    protected void step(EpochPacket p) {
        for (Map.Entry<WildcardPattern, PrintWriter> entry : wildcardPatterns.entrySet()) {
            entry.getValue().println(entry.getKey().getWeight());
        }
    }

    @Override
    protected void reset() {
        for (WildcardPattern wildcardPattern : wildcardPatterns.keySet()) {
            wildcardPattern.setWeight(0);
        }
    }

    @Override
    public void finish(FinishPacket p) {
        for (PrintWriter printWriter : wildcardPatterns.values()) {
            printWriter.close();
        }
    }
}

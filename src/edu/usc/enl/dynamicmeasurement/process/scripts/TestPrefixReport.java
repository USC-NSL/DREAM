package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.algorithms.matcher.HashMatcher;
import edu.usc.enl.dynamicmeasurement.algorithms.matcher.Matcher;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 2/4/13
 * Time: 9:29 AM
 */
public class TestPrefixReport extends StepPacketUser {
    private final Matcher matcher;
    private double sum = 0;
    private Map<WildcardPattern, Double[]> maxSumWeights;

    public TestPrefixReport(boolean resetOnStep, List<WildcardPattern> monitors) {
        super();
        this.matcher = new HashMatcher();
        matcher.setMonitors(monitors);
        maxSumWeights = new TreeMap<>();
        for (WildcardPattern monitor : monitors) {
            maxSumWeights.put(monitor, new Double[]{0d, 0d});
        }
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        sum += p.getSize();
        WildcardPattern match = matcher.match(p);
        if (match != null) {
            match.setWeight(match.getWeight() + p.getSize());
        }
    }

    @Override
    protected void step(EpochPacket p) {
        System.out.println(p.getStep() + "," + sum);
    }

    @Override
    protected void reset() {
        sum = 0;
        for (WildcardPattern monitor : maxSumWeights.keySet()) {
            Double[] stats = maxSumWeights.get(monitor);
            stats[0] += monitor.getWeight();
            stats[1] = Math.max(stats[1], monitor.getWeight());
            monitor.setWeight(0);
        }
    }

    @Override
    public void finish(FinishPacket p) {
        System.out.println("wildcard,sum,max");
        for (WildcardPattern monitor : maxSumWeights.keySet()) {
            Double[] stats = maxSumWeights.get(monitor);
            System.out.println(monitor + ", " + stats[0] + "," + stats[1]);
        }
    }
}

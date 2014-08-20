package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.algorithms.matcher.HashMatcher;
import edu.usc.enl.dynamicmeasurement.algorithms.matcher.Matcher;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.StepPacketUser;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/26/13
 * Time: 5:49 PM
 */
public class SumReportPrefixes extends StepPacketUser {
    private final List<WildcardPattern> wildcardPatterns;
    private Matcher matcher;
    private boolean resetOnStep;

    public SumReportPrefixes(List<WildcardPattern> wildcardPatterns, boolean resetOnStep) {
        matcher = new HashMatcher();
        this.wildcardPatterns = wildcardPatterns;
        matcher.setMonitors(this.wildcardPatterns);
        this.resetOnStep = resetOnStep;
    }

    @Override
    public void process2(DataPacket p) {
        WildcardPattern match = matcher.match(p.getSrcIP());
        match.setWeight(match.getWeight() + p.getSize());
    }

    public List<WildcardPattern> getWildcardPatterns() {
        return wildcardPatterns;
    }

    @Override
    protected void step(EpochPacket p) {
//        System.out.println(p.getStep() + ":" + wildcardPatterns);

    }

    @Override
    protected void reset() {
        if (resetOnStep) {
            for (WildcardPattern wildcardPattern : wildcardPatterns) {
                wildcardPattern.setWeight(0);
            }
        }
    }

    @Override
    public void finish(FinishPacket p) {

    }
}

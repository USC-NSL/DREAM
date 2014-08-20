package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.flow;

import edu.usc.enl.dynamicmeasurement.algorithms.matcher.HashMatcher;
import edu.usc.enl.dynamicmeasurement.algorithms.matcher.Matcher;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.HHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.overlappingsingleswitch.OverlappingSingleSwitch;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/11/14
 * Time: 5:21 PM <br/>
 * The algorithm for finding heavy hitters based on the Overlapping monitors.
 *
 * @see edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.overlappingsingleswitch.OverlappingSingleSwitch
 */
public class MaxCoverHHDetectionOverlapping extends HHAlgorithm implements SingleSwitchTask.SingleSwitchTaskImplementation {
    private final Matcher matcher;
    private MultiSwitch2ForHHDetection algorithm;

    public MaxCoverHHDetectionOverlapping(Element element) {
        super(element);
        algorithm = new MultiSwitch2ForHHDetection(element);
        matcher = new HashMatcher();
        matcher.setMonitors(algorithm.getMonitors());
    }

    @Override
    public Collection<WildcardPattern> findHH() {
        Collection<WildcardPattern> hhh = algorithm.findHHH();
        for (Iterator<WildcardPattern> iterator = hhh.iterator(); iterator.hasNext(); ) {
            WildcardPattern next = iterator.next();
            if (next.getWildcardNum() > 0) {
                iterator.remove();
            }
        }
        return hhh;
    }

    @Override
    public void match(long item, double diff) {
        WildcardPattern match = matcher.match(item);
        match.setWeight(match.getWeight() + diff);
    }

    @Override
    public void update(int step) {
        algorithm.update();
        matcher.setMonitors(algorithm.getMonitors());
    }

    @Override
    public void setSum(double sum) {

    }

    @Override
    public void reset() {
        super.reset();
        algorithm.reset();
    }

    @Override
    public void setFolder(String folder) {
        super.setFolder(folder);
        algorithm.setFolder(folder);
    }

    @Override
    public void finish() {
        algorithm.finish();
        super.finish();
    }

    @Override
    public void setCapacityShare(int resource) {
        algorithm.setCapacityShare(resource);
    }

    @Override
    public double estimateAccuracy() {
        return algorithm.estimateAccuracy();
    }

    @Override
    public int getUsedResourceShare() {
        return algorithm.getUsedResourceShare();
    }

    private static class MultiSwitch2ForHHDetection extends OverlappingSingleSwitch {

        public MultiSwitch2ForHHDetection(Element element) {
            super(element);
//            maxSplitThreshold = threshold * 10000000;
        }

        @Override
        public Collection<WildcardPattern> findHHH() {
            Collection<WildcardPattern> output = new ArrayList<>();
            for (WildcardPattern wildcardPattern : getMonitors()) {
                if (wildcardPattern.getWildcardNum() == 0 && wildcardPattern.getWeight() >= threshold) {
                    output.add(wildcardPattern);
                }
            }
            return output;
        }

        @Override
        public void update() {
            for (WildcardPattern wildcardPattern : getMonitors()) {
                double weight = wildcardPattern.getWeight();
                int level = wildcardPattern.getWildcardNum();
//                double alpha = Math.exp(-weight/threshold / (1l << level));
//                wildcardPattern.setWeight(alpha * (weight >= threshold ? 1 : 0) + (1 - alpha) * Math.min(weight / threshold, (1l << level)));

                wildcardPattern.setWeight(weight / (level + 1));

//                level++;
//                if (weight > threshold) {
//                    double k = weight / threshold;
//                    int logK = (int) (Math.log(k) / Math.log(2));
//                    double sum = level;
//                    for (int i = 1; i < logK; i++) {
//                        sum += (1l << (i - 1)) * (level - i);
//                    }
//                    int remK = (int) (k - (1 << logK));
//                    sum += remK * (level - logK - 1);
//                    wildcardPattern.setWeight(weight / sum);
//                } else {
//                    wildcardPattern.setWeight(weight / level);
//                }
            }
            super.update();
        }
    }
}

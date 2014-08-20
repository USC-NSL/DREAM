package edu.usc.enl.dynamicmeasurement.algorithms.tasks.changedetection.multiswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.matcher.HashMatcher;
import edu.usc.enl.dynamicmeasurement.algorithms.matcher.Matcher;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.changedetection.ChangeDetectionAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.flow.MultiSwitch2ForHHDetection;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MultiSwitchWildcardPattern;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.floodlight.TCAMAlgorithm;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.profile.Profilable;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/11/14
 * Time: 4:20 PM  <br/>
 * Uses the divide and merge algorithm to find large changes
 */

public class MultiSwitchChangeDetection extends ChangeDetectionAlgorithm implements MultiSwitchTask.MultiSwitchTaskImplementation, TCAMAlgorithm, Profilable {
    private final Matcher matcher;
    private MultiSwitch2ForChangeDetection algorithm;

    public MultiSwitchChangeDetection(Element element) {
        super(element);
        algorithm = new MultiSwitch2ForChangeDetection(element);
        matcher = new HashMatcher();
        matcher.setMonitors(algorithm.getMonitors());
    }

    @Override
    public void match(long item, double diff) {
        WildcardPattern match = matcher.match(item);
        match.setWeight(match.getWeight() + diff);
    }

    @Override
    public Collection<WildcardPattern> findBigChanges(int step) {
        algorithm.setStep(step);
        return algorithm.findHHH();
    }

    @Override
    public void update(int step) {
        super.update(step);
        algorithm.update();
        matcher.setMonitors(algorithm.getMonitors());
    }

    @Override
    public void setCapacityShare(Map<MonitorPoint, Integer> resource) {
        algorithm.setCapacityShare(resource);
    }

    @Override
    public void estimateAccuracy(Map<MonitorPoint, Double> accuracy) {
        algorithm.estimateAccuracy(accuracy);
    }

    @Override
    public double getGlobalAccuracy() {
        return algorithm.getGlobalAccuracy();
    }

    @Override
    public void getUsedResources(Map<MonitorPoint, Integer> resource) {
        algorithm.getUsedResources(resource);
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
    public void finish(FinishPacket p) {
        algorithm.finish();
        super.finish(p);
    }

    @Override
    public void writeProfiles() {
        algorithm.writeProfiles();
    }

    @Override
    public void createProfiler() {
        algorithm.createProfiler();
    }

    @Override
    public void finishProfiler() {
        algorithm.finishProfiler();
    }

    @Override
    public Collection<MonitorPoint> getWhichSwitch(WildcardPattern monitor) {
        return algorithm.getWhichSwitch(monitor);
    }

    @Override
    public Collection<WildcardPattern> getMonitors() {
        return algorithm.getMonitors();
    }

    /**
     * The adapter class for divide and merge algorithm. The changes are as follows
     * <ul>
     * <li> Divide & Merge: must update the history of the children or the parent based on the monitored data</li>
     * <li> set the weight of monitored nodes as weight-mean before finding big items</li>
     * <li> set the score for updating the items based on weight/(level+1) as we are only interested in
     * lowest level big changes</li>
     * </ul>
     */
    private class MultiSwitch2ForChangeDetection extends MultiSwitch2ForHHDetection {
        private Map<WildcardPattern, Double> monitorMean = new HashMap<>();


        public MultiSwitch2ForChangeDetection(Element element) {
            super(element);
            monitorMean.put(taskWildcardPattern.clone(), 0d);
        }

        @Override
        public Collection<WildcardPattern> findHHH() {
            //update the weight for finding big items
            for (WildcardPattern wildcardPattern : monitors.keySet()) {
                Double mean = monitorMean.get(wildcardPattern);
                double weight = wildcardPattern.getWeight();
                monitorMean.put(wildcardPattern, mean * ewmaAlpha + weight * (1 - ewmaAlpha));
                wildcardPattern.setWeight(Math.abs(mean - weight));
            }
            return super.findHHH();
        }

        @Override
        protected void updateForMerge(WildcardPattern toMergeSiblingsParent, MultiSwitchWildcardPattern toMerge, MultiSwitchWildcardPattern foundNode1, MultiSwitchWildcardPattern foundNode2) throws WildcardPattern.InvalidWildCardValue {
            super.updateForMerge(toMergeSiblingsParent, toMerge, foundNode1, foundNode2);
            Double mean1 = monitorMean.remove(foundNode1.getWildcardPattern());
            Double mean2 = monitorMean.remove(foundNode2.getWildcardPattern());
            monitorMean.put(toMerge.getWildcardPattern(), mean1 + mean2);
        }

        @Override
        protected void updateForDivide(MultiSwitchWildcardPattern divideCandidate, MultiSwitchWildcardPattern divideChild1, MultiSwitchWildcardPattern divideChild2) throws WildcardPattern.InvalidWildCardValue {
            super.updateForDivide(divideCandidate, divideChild1, divideChild2);
            Double mean = monitorMean.remove(divideCandidate.getWildcardPattern());
            monitorMean.put(divideChild1.getWildcardPattern(), mean / 2);
            monitorMean.put(divideChild2.getWildcardPattern(), mean / 2);
        }
    }
}



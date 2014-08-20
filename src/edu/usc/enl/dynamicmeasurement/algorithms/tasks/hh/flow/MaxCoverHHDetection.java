package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.flow;

import edu.usc.enl.dynamicmeasurement.algorithms.matcher.HashMatcher;
import edu.usc.enl.dynamicmeasurement.algorithms.matcher.Matcher;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.HHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.floodlight.TCAMAlgorithm;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.profile.Profilable;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/11/14
 * Time: 5:21 PM  <br/>
 * The algorithm for HH detection on multiple switches that uses TCAMs
 */
public class MaxCoverHHDetection extends HHAlgorithm implements MultiSwitchTask.MultiSwitchTaskImplementation, TCAMAlgorithm, Profilable {
    private final Matcher matcher;
    private MultiSwitch2ForHHDetection algorithm;

    public MaxCoverHHDetection(Element element) {
        super(element);
        algorithm = new MultiSwitch2ForHHDetection(element);
        matcher = new HashMatcher();
        matcher.setMonitors(algorithm.getMonitors());
    }

    @Override
    public Collection<WildcardPattern> findHH() {
        return algorithm.findHHH();
    }

    @Override
    public void setStep(int step) {
        super.setStep(step);
        algorithm.setStep(step);
    }

    @Override
    public void match(long item, double diff) {
        WildcardPattern match = matcher.match(item);
        try {
            match.setWeight(match.getWeight() + diff);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void finish() {
        algorithm.finish();
        super.finish();
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

}

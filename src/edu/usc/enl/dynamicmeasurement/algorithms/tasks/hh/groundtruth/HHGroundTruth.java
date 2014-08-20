package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.groundtruth;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.HHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/18/13
 * Time: 7:21 AM <br/>
 * The groundtruth algorithm for finding heavy hitters at the level specified by wildcardNum property.
 */
public class HHGroundTruth extends HHAlgorithm implements SingleSwitchTask.SingleSwitchTaskImplementation {
    private Map<Long, Double> data = new HashMap<>();

    public HHGroundTruth(Element element) {
        super(element);
    }

    public HHGroundTruth(double threshold, int wildcardNum1, WildcardPattern taskWildcardPattern1) {
        super(threshold, wildcardNum1, taskWildcardPattern1);
    }

    @Override
    public Collection<WildcardPattern> findHH() {
        List<WildcardPattern> output = new LinkedList<>();
        for (Map.Entry<Long, Double> entry : data.entrySet()) {
            if (entry.getValue() >= threshold) {
                output.add(new WildcardPattern(entry.getKey(), wildcardNum, entry.getValue()));
            }
        }
        return output;
    }

    @Override
    public void reset() {
        data.clear();
    }

    @Override
    public void update(int step) {

    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void setSum(double sum) {
    }

    @Override
    public void match(long item, double diff) {
        item >>>= wildcardNum;
        Double aDouble = data.get(item);
        if (aDouble == null) {
            data.put(item, diff);
        } else {
            data.put(item, diff + aDouble);
        }
    }

    public void setCapacity(int c) {

    }

    @Override
    public double estimateAccuracy() {
        return 1;
    }

    @Override
    public int getUsedResourceShare() {
        return 0;
    }

    @Override
    public void setCapacityShare(int c) {

    }
}

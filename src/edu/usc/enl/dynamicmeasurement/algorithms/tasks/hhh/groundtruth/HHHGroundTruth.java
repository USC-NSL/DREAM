package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.groundtruth;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.HHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 11:06 PM <br/>
 * The groundtruth for finding Hierarchical Heavy Hitters.
 * It tracks the lowest level IPs and then finds HHHs by traversing the tree bottom-up
 */
public class HHHGroundTruth extends HHHAlgorithm implements SingleSwitchTask.SingleSwitchTaskImplementation {
    double sum = 0;
    private Map<Long, Double> data = new HashMap<>();

    public HHHGroundTruth(Element element) {
        super(element);
    }

    @Override
    protected void update() {

    }

    @Override
    public Collection<WildcardPattern> findHHH() {
        List<WildcardPattern> output = new LinkedList<>();
        Map<Long, Double> data2 = new HashMap<>(data.size());
        //traverse the tree bottom-up. If an item > threshold report otherwise add to parent.
        //each loop is for one level of tree
        for (int wildcards = wildcardNum; wildcards <= WildcardPattern.TOTAL_LENGTH; wildcards++) {
            for (Map.Entry<Long, Double> entry : data.entrySet()) {
                double value = entry.getValue();
                long key = entry.getKey();
                if (value >= threshold) {
                    output.add(new WildcardPattern(key, wildcards, value));
                    value = 0;
                }
                key >>>= 1;
                Double newValue = data2.get(key);
                if (newValue == null) {
                    newValue = 0d;
                }
                data2.put(key, newValue + value);
            }
            Map<Long, Double> dataTemp = data;
            data = data2;
            data2 = dataTemp;
            data2.clear();
        }
        return output;
    }

    @Override
    public void reset() {
        data.clear();
        sum = 0;
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
    public void finish() {
        super.finish();
    }

    @Override
    public void setSum(double sum) {
    }

    @Override
    public void match(long item, double diff) {
//        System.out.println(item + "," + diff);
        item >>>= wildcardNum;
        sum += diff;
        Double aDouble = data.get(item);
        if (aDouble == null) {
            data.put(item, diff);
        } else {
            data.put(item, diff + aDouble);
        }
    }

    @Override
    public void setCapacityShare(int c) {

    }
}

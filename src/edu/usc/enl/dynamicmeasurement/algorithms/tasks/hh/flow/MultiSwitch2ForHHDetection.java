package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh.flow;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MonitorPointData;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MultiSwitch2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MultiSwitchWildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/7/2014
 * Time: 9:12 PM <br/>
 * Adapting HHH detection for HH detection.
 * Only keeps the lowest level of HHHs.
 * Changes the weights before updating monitors
 */
public class MultiSwitch2ForHHDetection extends MultiSwitch2 {

//        private final int HHWildcards;

    public MultiSwitch2ForHHDetection(Element element) {
        super(element);
//            Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
//            HHWildcards = Integer.parseInt(childrenProperties.get("WildcardNum").getAttribute(ConfigReader.PROPERTY_VALUE));
        maxSplitThreshold = threshold * 10000000;
    }

    @Override
    public Collection<WildcardPattern> findHHH() {

        //create the report
        Collection<WildcardPattern> output = new ArrayList<>();
        for (WildcardPattern wildcardPattern : getMonitors()) {
            if (wildcardPattern.getWildcardNum() == wildcardNum && wildcardPattern.getWeight() >= threshold) {
                output.add(wildcardPattern);
            }
        }

        //estimate accuracy
//            double medianHHWeights = getMedianHHWeights(output);
        double medianHHWeights = threshold;

        int notFoundHHs = 0;
        Collection<WildcardPattern> monitorsCollection = getMonitors();
        for (WildcardPattern monitor : monitorsCollection) {
            double weight = monitor.getWeight();
            int level = monitor.getWildcardNum() - wildcardNum;
            MultiSwitchWildcardPattern mswp = monitors.get(monitor);
            if (weight < threshold) {
                continue;
            }

            if (level == 0) {
                Set<MonitorPoint> monitorPoints = mswp.getMonitorPoints();
                for (MonitorPoint monitorPoint : monitorPoints) {
                    monitorPointDatas.get(monitorPoint).addPrecision(1);
                }
                continue;
            }

            //upperbound for the missed HHs of the internal node
            int notFoundHHs1 = Math.min(1 << level, (int) (weight / medianHHWeights));

            //distribute the precision among multiple switches
            Set<MonitorPoint> monitorPoints = mswp.getMonitorPoints();
            boolean bottleneck = contributingMonitorsHaveBottleneck(monitorPoints);
            for (MonitorPoint monitorPoint : monitorPoints) {
                MonitorPointData monitorPointData = monitorPointDatas.get(monitorPoint);
                if (monitorPointData.isFull() || !bottleneck) {//can be no bottleneck and I'm not full because of optimization
                    monitorPointData.addPrecision(0, notFoundHHs1);
                } else { //if there is a bottleneck and I'm not the one, just say I'm good
                    monitorPointData.addPrecision(1);
                }
            }

            notFoundHHs += notFoundHHs1;
        }
        int allHHs = notFoundHHs + output.size();
        if (allHHs == 0) {
            lastAccuracy = 1;
        } else {
            lastAccuracy = (1.0 * output.size() / allHHs);
        }
        for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
            monitorPointData.computeAccuracy();
        }

        accuracyWriter.println(getStep() + "," + lastAccuracy);

        return output;
    }

    /**
     * find median of the set of prefix patterns.
     *
     * @param patterns
     * @return
     */
    private double getMedianHHWeights(Collection<WildcardPattern> patterns) {
        List<Double> hhWeightsList = new ArrayList<>(patterns.size());
        for (WildcardPattern wildcardPattern : patterns) {
            hhWeightsList.add(wildcardPattern.getWeight());
        }

        double hhWeights = 0;

        if (patterns.size() < 5) {
            hhWeights = threshold;
        } else {
            Collections.sort(hhWeightsList);
            if (hhWeightsList.size() % 2 == 0) {
                hhWeights = (hhWeightsList.get(hhWeightsList.size() / 2 - 1) + hhWeightsList.get(hhWeightsList.size() / 2)) / 2;
            } else {
                hhWeights = hhWeightsList.get(hhWeightsList.size() / 2);
            }
        }
        return hhWeights;
    }

    @Override
    public void update() {
        for (WildcardPattern wildcardPattern : monitors.keySet()) {
            double weight = wildcardPattern.getWeight();
            int level = wildcardPattern.getWildcardNum();
            wildcardPattern.setWeight(weight / (level + 1));
        }
        super.update();
    }

}

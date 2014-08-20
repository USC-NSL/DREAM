package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.hhfringe;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.NeedInitHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.FlowHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/25/13
 * Time: 9:11 AM <br/>
 * The TCAM based HHH detection algorithm on a single switch that tries to maximize the total weight of nodes
 * only at the fringe of monitored prefix tree (monitored prefixes). <br/>
 * Note that this algorithm is only to test ideas and does not respect resource allocation or estimate accuracy yet
 */
public class HHFringe extends FlowHHHAlgorithm implements NeedInitHHHAlgorithm, SingleSwitchTask.SingleSwitchTaskImplementation {
    private final TreeMap<WildcardPattern, WildcardPattern> monitors;

    public HHFringe(Element element) {
        super(element);
        int capacity = Util.getNetwork().getFirstMonitorPoints().getCapacity();
        monitors = new TreeMap<>(WildcardPattern.WILDCARDNUM_COMPARATOR);
        initMonitors(capacity, this, taskWildcardPattern);
    }

    @Override
    public Collection<WildcardPattern> getMonitors() {
        return monitors.keySet();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void setCapacityShare(int resource) {

    }

    @Override
    public double estimateAccuracy() {
        return 1;
    }

    @Override
    public int getUsedResourceShare() {
        return monitors.size();
    }

    @Override
    public void update() {
        //assumes a tree that covers all leaves
        LinkedList<WildcardPattern> waitingLeftsStack = new LinkedList<>();
        LinkedList<WildcardPattern> toProcess = new LinkedList<>(monitors.keySet());
        while (toProcess.size() > 0) {
            WildcardPattern wildcardPattern = toProcess.pop();
            if (wildcardPattern.getWeight() > threshold) {
                if (wildcardPattern.canGoDown()) {
                    //go down
                    monitors.remove(wildcardPattern);
                    try {
                        WildcardPattern left = wildcardPattern.clone().goDown(false);
                        WildcardPattern right = wildcardPattern.clone().goDown(true);
                        left.setWeight(wildcardPattern.getWeight() / 2);
                        right.setWeight(wildcardPattern.getWeight() / 2);
                        monitors.put(left, left);
                        monitors.put(right, right);
                        toProcess.push(left);
                        toProcess.push(right);
                    } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
                        invalidWildCardValue.printStackTrace();
                    }
                }
            } else {
                //candidate for going up
                if (wildcardPattern.canGoUp()) {
                    if (wildcardPattern.isLeft()) {
                        waitingLeftsStack.push(wildcardPattern);
                    } else {
                        WildcardPattern left = null;
                        if (waitingLeftsStack.size() > 0) {
                            left = waitingLeftsStack.pop();
                            if (!left.isSibling(wildcardPattern)) {
                                waitingLeftsStack.push(left);
                                left = null;
                            }
                        }
                        if (left != null) {
                            //merge
                            double parentWeight = wildcardPattern.getWeight() + left.getWeight();
                            if (parentWeight <= threshold) {
                                monitors.remove(wildcardPattern);
                                monitors.remove(left);
                                WildcardPattern parent = wildcardPattern.goUp();
                                parent.setWeight(parentWeight);
                                monitors.put(parent, parent);
                                toProcess.push(parent);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addMonitor(WildcardPattern wildcardPattern) {
        monitors.put(wildcardPattern, wildcardPattern);
    }

    @Override
    public WildcardPattern pollAMonitor() {
        return monitors.pollLastEntry().getKey();
    }
}

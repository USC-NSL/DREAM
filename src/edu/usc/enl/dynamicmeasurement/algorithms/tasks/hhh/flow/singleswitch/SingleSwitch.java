package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.singleswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.NeedInitHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.FlowHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/22/13
 * Time: 11:23 AM <br/>
 * The single switch implementation of divide & merge algorithm.
 * It picks the node with maximum weight to divide and picks two sibling nodes to merge
 */
public class SingleSwitch extends FlowHHHAlgorithm implements NeedInitHHHAlgorithm, SingleSwitchTask.SingleSwitchTaskImplementation {
    protected TreeMap<WildcardPattern, WildcardPattern> monitors;
    protected Set<WildcardPattern> siblings;
    protected int capacity;
    private double lastHHHAvgRatio = 0;
    private FalseHHHFinder falseHHHFinder;
    private double lastAccuracy = 0;

    public SingleSwitch(Element element) {
        super(element);
        int capacity = Util.getNetwork().getFirstMonitorPoints().getCapacity();
        setCapacityShare(capacity);
        monitors = new TreeMap<>(WildcardPattern.WILDCARDNUM_COMPARATOR);

        initMonitors(capacity, this, taskWildcardPattern);

        // find siblings in monitors
        initSiblings();
        falseHHHFinder = new FalseHHHFinder(false, wildcardNum);
    }

    @Override
    public void setCapacityShare(int c) {
        this.capacity = c;
    }

    @Override
    public double estimateAccuracy() {
        return lastAccuracy;
//        return -lastHHHAvgRatio;
//        return 1.0 / (5 * (lastHHHAvgRatio - 1));
        //1 - Math.exp(-4 * sum);

    }

    @Override
    public int getUsedResourceShare() {
        return monitors.size();
    }

    public Collection<WildcardPattern> getMonitors() {
        return monitors.keySet();
    }

    @Override
    public Collection<WildcardPattern> findHHH() {
        Collection<WildcardPattern> hhh = super.findHHH();
        double sum = 0;
        for (WildcardPattern wildcardPattern : hhh) {
            sum += wildcardPattern.getWeight();
        }
        sum /= hhh.size() * threshold;
        lastHHHAvgRatio = sum;
        double accuracy = falseHHHFinder.findFalseHHHs2(hhh, monitors.keySet(), threshold);
        if (hhh.size() == 0) {
            lastAccuracy = 1;
        } else {
//            double falseHHHs = falseHHHFinder.getLastFalseHHHs().size();
//            double trueHHHs = hhh.size() - falseHHHs;
//            lastAccuracy = (trueHHHs + falseHHHs * trueHHHs / hhh.size()) / hhh.size();
            lastAccuracy = accuracy / hhh.size();
//            System.out.println(lastAccuracy);
        }
        return hhh;
    }

    @Override
    public void update() {
        //create the treeset
        TreeSet<WildcardPattern> weights = new TreeSet<>(WildcardPattern.WEIGHT_COMPARATOR);
        weights.addAll(monitors.keySet());
        update(weights);
    }

    private void initSiblings() {
        //assumes monitors is already sorted
        siblings = new TreeSet<WildcardPattern>();//capacity, 1
        WildcardPattern last = null;
        for (WildcardPattern monitor : monitors.keySet()) {
            if (last == null) {
                last = monitor;
            } else {
                if (last.isSibling(monitor)) {
                    siblings.add(last);
                    siblings.add(monitor);
                }
                last = monitor;
            }
        }
    }

    public void update(final TreeSet<WildcardPattern> weights) {
//        printMonitors();

        boolean canUpdate = true;
        int step = 0;
        while (weights.size() > 0 && canUpdate) {
            canUpdate = updateStep(weights);
            displayHook(null, step++);
        }


//        System.out.println("////////////////////");
//        printMonitors();

    }

    protected boolean updateStep(TreeSet<WildcardPattern> weights) {

        if (monitors.size() < capacity) {
            WildcardPattern divideCandidate = findDivideCandidate(weights);
            if (divideCandidate == null) {
                return false;
            }
            commitDivide(weights, divideCandidate);
            return true;
        }

        if (monitors.size() > capacity) {
            MinSiblingsFinder minSiblingsFinder = new MinSiblingsFinder(weights, Double.MAX_VALUE).invoke();
            WildcardPattern toMerge1 = minSiblingsFinder.getToMerge1();
            WildcardPattern toMerge2 = minSiblingsFinder.getToMerge2();
            if (toMerge1 == null) {
                return false;
            }
            commitMerge(weights, toMerge1, toMerge2);
            return true;
        }
        if (monitors.size() == capacity) {
            WildcardPattern maxEntry = findDivideCandidate(weights);
            if (maxEntry == null) {
                return false;
            }
            MinSiblingsFinder minSiblingsFinder = new MinSiblingsFinder(weights, maxEntry.getWeight()).invoke();
            WildcardPattern foundNode1 = minSiblingsFinder.getToMerge1();
            WildcardPattern foundNode2 = minSiblingsFinder.getToMerge2();
            if (foundNode1 == null) {
                return false;
            }
            commitDivide(weights, maxEntry);
            commitMerge(weights, foundNode1, foundNode2);
            return true;
        }

        return false;
    }

    protected WildcardPattern findDivideCandidate(TreeSet<WildcardPattern> weights) {
        WildcardPattern maxEntry = null;
        while (weights.size() > 0) {
            WildcardPattern maxEntry2 = weights.pollFirst();
            if (maxEntry2.canGoDown()) {
                maxEntry = maxEntry2;
                break;
            }
        }
        return maxEntry;
    }

    protected void commitMerge(TreeSet<WildcardPattern> weights,
                               WildcardPattern toMerge1, WildcardPattern toMerge2) {
        weights.remove(toMerge1);
        weights.remove(toMerge2);

        siblings.remove(toMerge1);
        siblings.remove(toMerge2);
        //update monitors
        monitors.remove(toMerge1);
        monitors.remove(toMerge2);

        //go up
        WildcardPattern minsParent = toMerge1.goUp();
        //update weights
        minsParent.setWeight(toMerge1.getWeight() + toMerge2.getWeight());
        weights.add(minsParent);

        //update siblings
        WildcardPattern newSibling = minsParent.getSibling();
        WildcardPattern sibling = monitors.get(newSibling);
        if (sibling != null) {
            //newSibling.setWeight(weight);
            siblings.add(sibling);
            siblings.add(minsParent);
        }

        monitors.put(minsParent, minsParent);
    }

    protected void commitDivide(TreeSet<WildcardPattern> weights, WildcardPattern divideCandidate) {
        double maxWeight = divideCandidate.getWeight();
        if (siblings.contains(divideCandidate)) {
            //remove old sibling
            siblings.remove(divideCandidate);//siblings is a set don't mess with hash function
            siblings.remove(divideCandidate.getSibling());
        }
        monitors.remove(divideCandidate);

        WildcardPattern divideChild1 = null;
        WildcardPattern divideChild2 = null;
        try {
            divideChild1 = divideCandidate.clone().goDown(false);
            divideChild2 = divideCandidate.goDown(true);
        } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
            invalidWildCardValue.printStackTrace();
        }

        divideChild1.setWeight(maxWeight / 2);
        divideChild2.setWeight(maxWeight / 2);
        weights.add(divideChild1);
        weights.add(divideChild2);

        siblings.add(divideChild1);
        siblings.add(divideChild2);

        monitors.put(divideChild1, divideChild1);
        monitors.put(divideChild2, divideChild2);
    }

    protected void dfsBreak(WildcardPattern node, int maxLevel, int currentLevel) throws WildcardPattern.InvalidWildCardValue {
        WildcardPattern lClone = node.clone();
        lClone.goDown(false);
        node.goDown(true);
        if (currentLevel < maxLevel) {
            dfsBreak(lClone, maxLevel, currentLevel + 1);
            dfsBreak(node, maxLevel, currentLevel + 1);
        } else {
            monitors.put(lClone, lClone);
            monitors.put(node, node);
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

    protected class MinSiblingsFinder {
        protected final TreeSet<WildcardPattern> weights;
        protected WildcardPattern toMerge1;
        protected WildcardPattern toMerge2;
        protected double benefit;

        public MinSiblingsFinder(TreeSet<WildcardPattern> weights, double benefit) {
            this.weights = weights;
            this.benefit = benefit;
        }

        public WildcardPattern getToMerge1() {
            return toMerge1;
        }

        public WildcardPattern getToMerge2() {
            return toMerge2;
        }

        public MinSiblingsFinder invoke() {
            //find min node that has sibling
            toMerge1 = null;
            toMerge2 = null;
            {
                Map<WildcardPattern, WildcardPattern> seenSiblings = new HashMap<>();
                double minSiblingWeight = -1;
                for (WildcardPattern minCandidate : weights.descendingSet()) {//TODO: can be optimized by tracking the last min
                    if (minCandidate.getWeight() >= benefit || (minSiblingWeight >= 0 && (minCandidate.getWeight() > minSiblingWeight))) {
                        break;//no chance to see a better sibling pair
                    }
                    if (siblings.contains(minCandidate)) {
                        WildcardPattern sibling = monitors.get(minCandidate.getSibling());
                        double siblingWeight = sibling.getWeight() + minCandidate.getWeight();
                        if (seenSiblings.containsKey(sibling) &&
                                siblingWeight < benefit) {//then check benefit
                            //found it! but the sum may not be min
                            if (minSiblingWeight < 0 || minSiblingWeight > siblingWeight) {
                                toMerge1 = minCandidate;
                                toMerge2 = sibling;
                                minSiblingWeight = siblingWeight;
                            }
                        } else {
                            seenSiblings.put(minCandidate, minCandidate); //wait for the larger candidate
                        }
                    }
                }
            }
            return this;
        }
    }
}

package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.FlowHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.singleswitch.FalseHHHFinder;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.floodlight.TCAMAlgorithm;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;
import edu.usc.enl.dynamicmeasurement.util.profile.LatencyProfiler;
import edu.usc.enl.dynamicmeasurement.util.profile.Profilable;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/22/13
 * Time: 10:45 PM  <br/>
 * The main class to implement TCAM-based hierarchical heavy hitter detection on multiple switches
 */
public class MultiSwitch2 extends FlowHHHAlgorithm implements MultiSwitchTask.MultiSwitchTaskImplementation, Profilable, TCAMAlgorithm {
    public final static Set<MonitorPoint> EMPTY_SET = new HashSet<MonitorPoint>() {
        @Override
        public boolean add(MonitorPoint monitorPoint) {
            System.err.println("Modify set!");
            return false;
        }

        @Override
        public boolean remove(Object o) {
            System.err.println("Remove set!");
            return false;
        }
    };
    /**
     * Limit the number of divide and merge to limit the control loop delay
     */
    protected static final int MAX_DIVIDE_MERGE = 4000;
    private final SubSetCoverSolver<MonitorPoint> monitorPointSubSetCoverSolver;

    /**
     * How far we should go into the nodes that are smaller than the threshold? threshold/weight>maxSplitThreshold
     */
    public double maxSplitThreshold = 8;

    /**
     * Monitored prefixes and their corresponding nodes in the tree.
     * It is sorted bottom-up.
     */
    protected TreeMap<WildcardPattern, MultiSwitchWildcardPattern> monitors;

    /**
     * Keep the internal nodes in bottom-up order
     */
    protected Map<WildcardPattern, MultiSwitchWildcardPattern> internalNodes;

    /**
     * Keep additional information for each switch
     */
    protected Map<MonitorPoint, MonitorPointData> monitorPointDatas;

    /**
     * If there is not any node with smaller cost for merging
     */
    protected boolean noMoreFreeUp;
    protected PrintWriter accuracyWriter;
    protected double lastAccuracy;

    /**
     * Keep monitored prefixes in order of their weight descendingly
     */
    private TreeSet<WildcardPattern> weights = new TreeSet<>(WildcardPattern.WEIGHT_COMPARATOR);
    private FalseHHHFinder falseHHHFinder;

    /**
     * Temporary data structures for subset cover solving to not create them again and again
     */
    private final Set<MonitorPoint> toFreeMonitorPoints;
    private final List<WildcardPattern> solution;
    private final Map<WildcardPattern, Set<MonitorPoint>> sets;
    private final Map<WildcardPattern, Double> costs;
    /**
     * A temporary data structure for subset cover problem. For each unique set of switches, we only need to
     * keep the one with minimum cost to feed to the subset cover solver
     */
    private final Map<Set<MonitorPoint>, MultiSwitchWildcardPattern> setMinCost;

    /**
     * Data structures for joint TCAM configuration and allocation
     */
    private MultiTaskMultiSwitchResourceNegotiator resourceNegotiator = null;

    /**
     * Keeps track of the node that wanted free entries on a set of switches but could not find any.
     * So that other nodes with smaller cost does not try solving the min-set cover problem.
     */
    private Map<Set<MonitorPoint>, Double> impossibleSets = new HashMap<>();
    private LatencyProfiler profiler;

    /**
     * The folder for this task
     */
    private String folder = null;

    public MultiSwitch2(Element element) {
        super(element);
        monitors = new TreeMap<>(WildcardPattern.WILDCARDNUM_COMPARATOR);
        monitorPointDatas = new HashMap<>();
        internalNodes = new TreeMap<>(WildcardPattern.WILDCARDNUM_COMPARATOR);

        //just add the root
        //fill monitors and sibling
        Set<MonitorPoint> allMonitorPoints = Util.getNetwork().getMonitorPoints();
        //fill used capacity
        Set<MonitorPoint> taskMonitorPoints = Util.cloneSet(allMonitorPoints);
        taskMonitorPoints.clear();
        for (MonitorPoint monitorPoint : allMonitorPoints) {
            if (monitorPoint.hasDataFrom(taskWildcardPattern)) {
                MonitorPointData data = new MonitorPointData(monitorPoint);
                monitorPointDatas.put(monitorPoint, data);
                data.setUsedCapacity(1);
                taskMonitorPoints.add(monitorPoint);
            }
        }
        toFreeMonitorPoints = Util.cloneSet(taskMonitorPoints);
        toFreeMonitorPoints.clear();
        solution = new ArrayList<>(10);

        MultiSwitchWildcardPattern rootPattern = new MultiSwitchWildcardPattern(taskWildcardPattern.clone(), taskMonitorPoints);
        monitors.put(rootPattern.getWildcardPattern(), rootPattern);


        this.falseHHHFinder = new FalseHHHFinder(false, wildcardNum);
        monitorPointSubSetCoverSolver = new SubSetCoverSolver<>();
        sets = new HashMap<>(internalNodes.size(), 1);
        costs = new HashMap<>(internalNodes.size(), 1);
        setMinCost = new HashMap<>();
    }

    public static Set<MonitorPoint> hasDataFrom(WildcardPattern wildcardPattern, Set<MonitorPoint> monitorPoints) {
        Set<MonitorPoint> output = Util.cloneSet(monitorPoints);
        for (Iterator<MonitorPoint> iterator = output.iterator(); iterator.hasNext(); ) {
            MonitorPoint monitorPoint = iterator.next();
            if (!monitorPoint.hasDataFrom(wildcardPattern)) {
                iterator.remove();
            }
        }
        return output;
    }

    public Collection<MonitorPoint> getWhichSwitch(WildcardPattern monitor) {
        return monitors.get(monitor).getMonitorPoints();
    }

    @Override
    public void setFolder(String folder) {
        super.setFolder(folder);
        try {
            accuracyWriter =
//                    new VoidPrintWriter("/acc.csv");
                    new PrintWriter(folder + "/acc.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.folder = folder;
//        createProfiler();
    }

    public MultiTaskMultiSwitchResourceNegotiator getResourceNegotiator() {
        return resourceNegotiator;
    }

    public void setResourceNegotiator(MultiTaskMultiSwitchResourceNegotiator resourceNegotiator) {
        this.resourceNegotiator = resourceNegotiator;
    }

    /**
     * relies on the fact that internalNodes is sorted bottomup
     * go bottom up in the tree and update the weights of the internal nodes
     */
    protected void updateInternalNodesWeightsPreOrder() {
        if (internalNodes.size() == 0) {
            return;
        }
        try {
            for (MultiSwitchWildcardPattern mswp : internalNodes.values()) {
                //find children
                WildcardPattern left = mswp.getWildcardPattern().clone().goDown(false);
                WildcardPattern right = mswp.getWildcardPattern().clone().goDown(true);
                MultiSwitchWildcardPattern leftMswp = monitors.get(left);
                if (leftMswp == null) {
                    leftMswp = internalNodes.get(left);
                }
                MultiSwitchWildcardPattern rightMswp = monitors.get(right);
                if (rightMswp == null) {
                    rightMswp = internalNodes.get(right);
                }

                //add their weight
                mswp.setWeight(leftMswp.getWeight() + rightMswp.getWeight());
            }
        } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
            //as it is only internal nodes this exception should not happen
            invalidWildCardValue.printStackTrace();
        }
    }

    private boolean canGoUp(WildcardPattern wildcardPattern) {
        return (taskWildcardPattern.match(wildcardPattern) && wildcardPattern.getWildcardNum() < taskWildcardPattern.getWildcardNum());
    }

    @Override
    public void finish() {
        super.finish();
        accuracyWriter.close();
        finishProfiler();
    }

    @Override
    public Collection<WildcardPattern> findHHH() {
        Collection<WildcardPattern> hhhList = super.findHHH();
        if (hhhList.size() == 0) {
            lastAccuracy = 1;
            for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
                monitorPointData.setLastAccuracy(1);
            }
        } else {
            Collection<WildcardPattern> monitors1 = getMonitors();
            Map<WildcardPattern, Double> HHHPrecision = getHHHPrecision(hhhList, monitors1);
            computeAccuracy(monitors1, HHHPrecision);
        }
        accuracyWriter.println(getStep() + "," + lastAccuracy);
        return hhhList;
    }

    protected Map<WildcardPattern, Double> getHHHPrecision(Collection<WildcardPattern> hhhList, Collection<WildcardPattern> monitors1) {
        return falseHHHFinder.findFalseHHHs(hhhList, monitors1, threshold);
    }

    private void computeAccuracy(Collection<WildcardPattern> monitors1, Map<WildcardPattern, Double> HHHPrecision) {
        lastAccuracy = 0;
        for (Map.Entry<WildcardPattern, Double> entry : HHHPrecision.entrySet()) {
            Double precision = entry.getValue();
            WildcardPattern hhh = entry.getKey();
            lastAccuracy += precision;
            //find per monitorpoint accuracy
            MultiSwitchWildcardPattern mswp = monitors.get(hhh);
            if (mswp == null) {
                mswp = internalNodes.get(hhh);
                if (mswp == null) {
                    mswp = debugNullMSWP(monitors1, hhh);
                }
            }

            //distribute accuracy among switches
            Set<MonitorPoint> nodeMonitorPoints = mswp.getMonitorPoints();
            boolean bottleneck = contributingMonitorsHaveBottleneck(nodeMonitorPoints);
            for (MonitorPoint monitorPoint : nodeMonitorPoints) {
                MonitorPointData monitorPointData = monitorPointDatas.get(monitorPoint);
                if (monitorPointData.isFull() || !bottleneck) {//can be no bottleneck and I'm not full because of optimization
                    monitorPointData.addPrecision(precision);
                } else { //if there is a bottleneck and I'm not the one put precision 1 for me
                    monitorPointData.addPrecision(1);
                }
            }
        }
        lastAccuracy /= HHHPrecision.size();
        for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
            monitorPointData.computeAccuracy();
        }
    }

    private MultiSwitchWildcardPattern debugNullMSWP(Collection<WildcardPattern> monitors1, WildcardPattern hhh) {
        System.err.println("monitors");
        for (WildcardPattern wildcardPattern : monitors1) {
            System.err.println(wildcardPattern);
        }
        System.err.println("internals");
        for (WildcardPattern wildcardPattern : internalNodes.keySet()) {
            System.err.println(wildcardPattern);
        }
        throw new RuntimeException("data for " + hhh + " not found in monitors nor internalNodes");
    }

    protected boolean contributingMonitorsHaveBottleneck(Set<MonitorPoint> nodeMonitorPoints) {
        boolean bottleneck = false;
        for (MonitorPoint monitorPoint : nodeMonitorPoints) {
            //is any switch here a bottleneck
            MonitorPointData monitorPointData = monitorPointDatas.get(monitorPoint);
            if (monitorPointData.isFull()) {
                bottleneck = true;
                break;
            }
        }
        return bottleneck;
    }

    /**
     * tries multiple nodes until one division would be possible.
     * If the divide needs to free entries on some nodes, it also merges.
     * Todo: this method needs some refactoring to make its optimization logic clear
     *
     * @return false if there is no need for further division:
     * the entry to divide is very small comparing to the threshold.
     */
    public boolean updateStep() {
        boolean doneStep = false;
        while (weights.size() > 0 && !doneStep) {
            MultiSwitchWildcardPattern divideCandidate = findDivideCandidate(weights);
            if (divideCandidate == null || divideCandidate.getWeight() < threshold / maxSplitThreshold) {
                return false;
            }

            // have an estimate of benefit of going down: just its weight
            double benefit = divideCandidate.getBenefit();

            // find numbers that we need free capacity
            MultiSwitchWildcardPattern divideChildLeft = null;
            MultiSwitchWildcardPattern divideChildRight = null;
            try {
                divideChildLeft = divideCandidate.getLeft();
                divideChildRight = divideCandidate.getRight();
            } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
                invalidWildCardValue.printStackTrace();
            }
            Set<MonitorPoint> toFreeMonitorPoints2 = getToFreeMonitorPoints(divideChildLeft, divideChildRight);

            if (toFreeMonitorPoints2.size() > 0) {
                // find nodes that can free the required capacity: That's a minset cover problem from the set of siblings
                // sets are common ids for siblings
                // cost for each pair of sibling is sum of their weight
                if (resourceNegotiator == null) {
                    if (!impossibleSets.containsKey(toFreeMonitorPoints2)) {
                        double cost = findCandidateSiblings(solution, toFreeMonitorPoints2, benefit);
                        if (cost >= 0 && cost < benefit) { //if solution found and it is worth
                            //commit mins and max
                            commitDivide(divideCandidate);
                            commitMerge(solution);
                            doneStep = true;
                        } else {
                            impossibleSets.put(toFreeMonitorPoints2, benefit);
                        }
                    }
                } else {
                    double allocationResult = resourceNegotiator.findCandidateSiblings(this, toFreeMonitorPoints2, benefit, divideCandidate);
//                    if (allocationResult < 0) {
//                        noMoreFreeUp = true; //cannot do this as resourceNegotiator also checks the similarity of internal nodes
//                    } else
                    if (allocationResult > 0) {
                        doneStep = true;
                    }
                }

            } else {
                //commit max
                if (lastAccuracy == 1) {
                    return false; //don't expand unnecessarily --> optimize resource usage and speed
                    //this is especially useful for tasks that have not traffic in the beginning
                }
                commitDivide(divideCandidate);
                doneStep = true;
            }
        }
        return doneStep;
    }

    /**
     * find a merge solution that covers toFreeMonitorPoints with maximum cost of benefit
     *
     * @param solution
     * @param toFreeMonitorPoints
     * @param benefit             can be -1 so that ignore benefit
     * @return
     */
    private double findCandidateSiblings(List<WildcardPattern> solution, Set<MonitorPoint> toFreeMonitorPoints, double benefit) {
        if (noMoreFreeUp) {
            return -1;
        }
//        System.out.println(Thread.currentThread() + ": to free " + toFreeMonitorPoints.hashCode() + " benefit " + benefit);
        sets.clear();
        costs.clear();
        setMinCost.clear();
        solution.clear();

        //note internal nodes cannot be sorted based on mergecost as it can change during divide/merge
        for (Map.Entry<WildcardPattern, MultiSwitchWildcardPattern> entry : internalNodes.entrySet()) {
            MultiSwitchWildcardPattern mswp = entry.getValue();
            double mergeCost = mswp.getCost();
            if (benefit < 0 || mergeCost < benefit) {
                Set<MonitorPoint> providingSet = mswp.getInternalCommonSet();
                if (mergeCost == 0 && providingSet.equals(toFreeMonitorPoints)) {
                    //found best solution just return it
                    solution.add(mswp.getWildcardPattern());
                    return 0;
                }
                MultiSwitchWildcardPattern minMSWP = setMinCost.get(providingSet);
                if (minMSWP == null || minMSWP.getCost() > mergeCost) {
                    setMinCost.put(providingSet, mswp);
                }
            }
        }


        if (setMinCost.isEmpty()) {
            //as maxes are sorted we cannot find any candidates from now on because benefit is decreasing
            noMoreFreeUp = true;
            return -1;
        }
        for (Map.Entry<Set<MonitorPoint>, MultiSwitchWildcardPattern> entry : setMinCost.entrySet()) {
            sets.put(entry.getValue().getWildcardPattern(), Util.cloneSet(entry.getKey()));
            costs.put(entry.getValue().getWildcardPattern(), entry.getValue().getCost());
        }
        double solve = monitorPointSubSetCoverSolver.solve(toFreeMonitorPoints, sets, costs, solution);

        if (benefit < 0 && solve < 0) {
            //oh oh! wanted to forcefully merge but could not find a solution. This should be a bug in the algoritm.
            //lets write some debug information
            debugUnsuccessfulMerge(toFreeMonitorPoints);
        }
        return solve;
    }

    private void debugUnsuccessfulMerge(Set<MonitorPoint> toFreeMonitorPoints) {

        System.out.println(taskWildcardPattern + " hey I wanted to merge to capacity but not found anything to merge!");
        System.out.println("to free");
        for (MonitorPoint toFreeMonitorPoint : toFreeMonitorPoints) {
            System.out.println(toFreeMonitorPoint);
        }
        System.out.println("monitorpoints: ");
        for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
            System.out.println(monitorPointData);
        }
        System.out.println("Monitors: ");
        for (MultiSwitchWildcardPattern multiSwitchWildcardPattern : monitors.values()) {
            System.out.println(multiSwitchWildcardPattern);
        }
        throw new RuntimeException();
    }

    @Override
    public void reset() {
        super.reset();
        lastAccuracy = 0;
        for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
            monitorPointData.reset();
        }
    }

    protected void commitDivide(MultiSwitchWildcardPattern divideCandidate) {
        try {
            divideCandidate.computeCommonSet();
            {
                monitors.remove(divideCandidate.getWildcardPattern());
                //weights.remove(divideCandidate); is always removed before
            }

            MultiSwitchWildcardPattern divideChild1 = divideCandidate.getLeft();
            MultiSwitchWildcardPattern divideChild2 = divideCandidate.getRight();
            divideChild1.setWeight(divideCandidate.getWeight() / 2);
            divideChild2.setWeight(divideCandidate.getWeight() / 2);
            divideCandidate.computeInternalCommonSet();//do this after setting weight for children

            updateForDivide(divideCandidate, divideChild1, divideChild2);

        } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
            invalidWildCardValue.printStackTrace();
        }
    }

    protected void updateForDivide(MultiSwitchWildcardPattern divideCandidate, MultiSwitchWildcardPattern divideChild1, MultiSwitchWildcardPattern divideChild2) throws WildcardPattern.InvalidWildCardValue {
        internalNodes.put(divideCandidate.getWildcardPattern(), divideCandidate);
        //go through ancestors and update internal common set
        updateInternalNodes(divideCandidate.getWildcardPattern());
        weights.add(divideChild1.getWildcardPattern());
        weights.add(divideChild2.getWildcardPattern());
        monitors.put(divideChild1.getWildcardPattern(), divideChild1);
        monitors.put(divideChild2.getWildcardPattern(), divideChild2);


        //update used capacity
        for (MonitorPoint monitorPoint : divideCandidate.getCommonSet()) {
            MonitorPointData monitorPointData = monitorPointDatas.get(monitorPoint);
            monitorPointData.setUsedCapacity(monitorPointData.getUsedCapacity() + 1);
        }
    }

    /**
     * Commit a merge plan. The algorithm must take care of solutions that can have overlapping nodes,
     * and solutions that may be an ancestor of current monitored prefixes
     *
     * @param solutions the list of internal nodes that must collapse all their children. They may be overlapping!
     */
    protected void commitMerge(List<WildcardPattern> solutions) {
        Collections.sort(solutions, WildcardPattern.WILDCARDNUM_COMPARATOR);
        Set<WildcardPattern> ranBefore = new HashSet<>();
        try {
            while (solutions.size() > 0) {
                //the overall algorithm is to merge bottom-up. If the tomerge one is not the direct parent of
                //currently monitored prefixes, add its children to the end of solution list and do not remove
                // the current one from the list (we always pick from the end of list)
                // here there can be duplicate items in the solutions list. Just keep a set to ignore them.
                // There may be a better approach by making solution a sorted set bottom-up

                WildcardPattern toMergeSiblingsParent = solutions.get(solutions.size() - 1);
                if (ranBefore.contains(toMergeSiblingsParent)) {
                    solutions.remove(solutions.size() - 1);
                    continue;
                }
                MultiSwitchWildcardPattern toMerge = internalNodes.get(toMergeSiblingsParent);
                MultiSwitchWildcardPattern foundNode1 = toMerge.getLeft();
                MultiSwitchWildcardPattern foundNode2 = toMerge.getRight();

                {
                    //recursive merge, if I want to merge a top internal node and accepted the cost
                    //this means that I can also merge all its descendants
                    //so add the left and/or right to the solutions to address them firs in the next iteration and
                    //then come back to this node again
                    if (!monitors.containsKey(foundNode1.getWildcardPattern())) {
                        solutions.add(foundNode1.getWildcardPattern());
                        continue;
                    }
                    if (!monitors.containsKey(foundNode2.getWildcardPattern())) {
                        solutions.add(foundNode2.getWildcardPattern());
                        continue;
                    }
                }
                ranBefore.add(toMergeSiblingsParent);

                solutions.remove(solutions.size() - 1);
                //toMerge.setWeight(toMerge1.getWeight() + toMerge2.getWeight()); the weight is already correct, don't set it again
                updateForMerge(toMergeSiblingsParent, toMerge, foundNode1, foundNode2);
            }
        } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
            invalidWildCardValue.printStackTrace();
        }
    }

    /**
     * Update the data structures for merge
     *
     * @param toMergeSiblingsParent
     * @param toMerge
     * @param foundNode1
     * @param foundNode2
     * @throws WildcardPattern.InvalidWildCardValue
     */
    protected void updateForMerge(WildcardPattern toMergeSiblingsParent, MultiSwitchWildcardPattern toMerge, MultiSwitchWildcardPattern foundNode1, MultiSwitchWildcardPattern foundNode2) throws WildcardPattern.InvalidWildCardValue {
        {
            internalNodes.remove(toMergeSiblingsParent);
            //update ancestors' internal commonset
            toMerge.resetInternalCommonSet();
            updateInternalNodes(toMergeSiblingsParent);

            weights.remove(foundNode1.getWildcardPattern());
            weights.remove(foundNode2.getWildcardPattern());

            monitors.remove(foundNode1.getWildcardPattern());
            monitors.remove(foundNode2.getWildcardPattern());
        }

        {
            weights.add(toMerge.getWildcardPattern());
            monitors.put(toMerge.getWildcardPattern(), toMerge);

            //update usedCapacity
            for (MonitorPoint monitorPoint : toMerge.getCommonSet()) {
                MonitorPointData monitorPointData = monitorPointDatas.get(monitorPoint);
                monitorPointData.setUsedCapacity(monitorPointData.getUsedCapacity() - 1);
            }
        }
    }

    /**
     * Update the sets for all ancestors of the prefix pattern
     *
     * @param wp
     * @throws WildcardPattern.InvalidWildCardValue
     */
    private void updateInternalNodes(WildcardPattern wp) throws WildcardPattern.InvalidWildCardValue {
        wp = wp.clone();
        while (canGoUp(wp)) {
            wp.goUp();
            internalNodes.get(wp).computeInternalCommonSet();
        }
    }

    /**
     * Find which switches need additional TCAM entries if we want to monitor left and right
     *
     * @param maxEntryLeft
     * @param maxEntryRight
     * @return
     */
    private Set<MonitorPoint> getToFreeMonitorPoints(MultiSwitchWildcardPattern maxEntryLeft, MultiSwitchWildcardPattern maxEntryRight) {
        Set<MonitorPoint> toFreeMonitorPoints;
        {
            Set<MonitorPoint> commonMonitorPoints = Util.cloneSet(maxEntryRight.getMonitorPoints());
            commonMonitorPoints.retainAll(maxEntryLeft.getMonitorPoints());
            //check if we have capacity for them
            for (Iterator<MonitorPoint> iterator = commonMonitorPoints.iterator(); iterator.hasNext(); ) {
                MonitorPoint commonMonitorPoint = iterator.next();
                //remove those that have enough capacity
                MonitorPointData monitorPointData = monitorPointDatas.get(commonMonitorPoint);
                if (!monitorPointData.isFull()) {
                    iterator.remove();
                }
            }
            toFreeMonitorPoints = commonMonitorPoints;
        }
        return toFreeMonitorPoints;
    }

    /**
     * Find the divide candidate. It should not be bellow wildcardNum level of the tree
     *
     * @param weights
     * @return
     */
    protected MultiSwitchWildcardPattern findDivideCandidate(TreeSet<WildcardPattern> weights) {
        MultiSwitchWildcardPattern multiSwitchWildcardPattern = null;
        while (weights.size() > 0) {
            WildcardPattern maxEntry2 = weights.pollFirst();
            multiSwitchWildcardPattern = monitors.get(maxEntry2);
            if (maxEntry2.getWildcardNum() > wildcardNum && multiSwitchWildcardPattern.getMonitorPoints().size() > 0) {
                break;
            } else {
                multiSwitchWildcardPattern = null;
            }
        }
        return multiSwitchWildcardPattern;
    }

    public Collection<WildcardPattern> getMonitors() {
        return monitors.keySet();
    }

    @Override
    public void update() {
        profile("Prepare");
        updatePrepare();
        updateMonitors();
    }

    /**
     * The main method for updating monitors.
     * It goes through all nodes and tries to divide them.
     */
    protected void updateMonitors() {
        profile("Merge");
        mergeToCapacity();
        profile("Update " + weights.size());
        boolean canUpdate = true;
//        int step = 0;
        int doneStepNum = 0;
        while (weights.size() > 0 && canUpdate && doneStepNum < MAX_DIVIDE_MERGE) {
            //profile(weights.size() + "");
            canUpdate = updateStep();
            doneStepNum++;
//            displayHook(null, step++);
//            for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
//                if (monitorPointData.getUsedCapacity() > monitorPointData.getCapacity()) {
//                    throw new RuntimeException("Capacity violated");
//                }
//            }
        }
        profile(null);
    }

    /**
     * fill the data structures from monitored prefixes
     */
    protected void updatePrepare() {
        impossibleSets.clear();
        weights.clear();
        weights.addAll(monitors.keySet());
        noMoreFreeUp = false;
        updateInternalNodesWeightsPreOrder();
    }

    protected void profile(String s) {
        if (profiler != null) {
            profiler.sequentialRecord(s);
        }
    }

    /**
     * need to merge nodes if the capacity of some has been decreased
     */
    private void mergeToCapacity() {
        while (true) {
            toFreeMonitorPoints.clear();
            solution.clear();
            for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
                if (monitorPointData.getUsedCapacity() > monitorPointData.getCapacity()) {
                    toFreeMonitorPoints.add(monitorPointData.getMonitorPoint());
                }
            }
            if (toFreeMonitorPoints.size() > 0) {
                findCandidateSiblings(solution, toFreeMonitorPoints, -1);
                commitMerge(solution);
            } else {
                break;
            }
        }
    }

    private void compress() {
        for (MultiSwitchWildcardPattern multiSwitchWildcardPattern : monitors.values()) {
            multiSwitchWildcardPattern.compress();
        }
    }

    @Override
    public void setCapacityShare(Map<MonitorPoint, Integer> resource) {
        for (Map.Entry<MonitorPoint, Integer> entry : resource.entrySet()) {
            monitorPointDatas.get(entry.getKey()).setCapacity(entry.getValue());
        }
    }

    @Override
    public void estimateAccuracy(Map<MonitorPoint, Double> accuracy) {
        double min = Double.MAX_VALUE;
        for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
            min = Math.min(monitorPointData.getLastAccuracy(), min);
        }

        for (Map.Entry<MonitorPoint, Double> entry : accuracy.entrySet()) {
            double acc = monitorPointDatas.get(entry.getKey()).getLastAccuracy();
            if (min > lastAccuracy) {
                acc = (1 - (1 - acc) * min / lastAccuracy);
            }
            entry.setValue(Math.max(lastAccuracy, acc));
        }
    }

    @Override
    public double getGlobalAccuracy() {
        return lastAccuracy;
    }

    @Override
    public void getUsedResources(Map<MonitorPoint, Integer> resource) {
        for (MonitorPointData monitorPointData : monitorPointDatas.values()) {
            resource.put(monitorPointData.getMonitorPoint(), monitorPointData.getUsedCapacity());
        }
    }

    public Map<MonitorPoint, Integer> getUsedCapacities() {
        Map<MonitorPoint, Integer> usedCapacities = new HashMap<>();
        for (Map.Entry<MonitorPoint, MonitorPointData> entry : monitorPointDatas.entrySet()) {
            usedCapacities.put(entry.getKey(), entry.getValue().getUsedCapacity());
        }
        return usedCapacities;
    }

    public LatencyProfiler getProfiler() {
        return profiler;
    }

    @Override
    public void writeProfiles() {
        if (profiler != null) {
            profiler.write();
        }
    }

    @Override
    public void createProfiler() {
        if (profiler == null) {
            profiler = new LatencyProfiler(folder + "/profile.csv");
        }
    }

    @Override
    public void finishProfiler() {
        if (profiler != null) {
            profiler.finish();
            profiler = null;
        }
    }

}

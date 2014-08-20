package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.HHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.NeedInitHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.ui.GraphVizOutput;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 3/3/13
 * Time: 7:52 AM
 */
public abstract class FlowHHHAlgorithm extends HHHAlgorithm {
    public FlowHHHAlgorithm(Element element) {
        super(element);
    }

    @Override
    public void match(long srcIP, double size) {

    }

    /**
     * Find HHs in a post-order traversal algorithm
     *
     * @param weightThreshold
     * @param monitors
     * @param root
     * @return
     */
    public static Collection<WildcardPattern> findHHHPostOrder(double weightThreshold, Collection<WildcardPattern> monitors,
                                                               WildcardPattern root) {
        Set<WildcardPattern> output = new HashSet<>();
        LinkedList<WildcardPattern> unresolved = new LinkedList<>();
        if (monitors.size() == 0) {
            return output;
        }
        TreeMap<WildcardPattern, WildcardPattern> weightMap = new TreeMap<>(WildcardPattern.WILDCARDNUM_COMPARATOR);
        for (WildcardPattern monitor : monitors) {
            weightMap.put(monitor, monitor);
        }


        while (weightMap.size() > 0) {
            WildcardPattern wildcardPattern = weightMap.pollFirstEntry().getKey();
            boolean isHH = false;
            if (wildcardPattern.getWeight() >= weightThreshold) {
                output.add(wildcardPattern);
                //just set its weight to zero
                isHH = true;
            }
            if (!canGoUp(wildcardPattern, root)) {//this is the root
                break;
            }
            if (wildcardPattern.isLeft()) {
                if (isHH) {
                    WildcardPattern clone = wildcardPattern.clone();
                    clone.setWeight(0);
                    wildcardPattern = clone;
                }
                //check for right
                WildcardPattern parent = wildcardPattern.clone().goUp();
                SortedMap<WildcardPattern, WildcardPattern> lowerNodes = weightMap.headMap(parent, false);
                boolean siblingDescendantFound = false;
                for (WildcardPattern lowerNode : lowerNodes.keySet()) {
                    if (parent.match(lowerNode)) {
                        siblingDescendantFound = true;
                        break;
                    }
                }
                if (siblingDescendantFound) {
                    //keep me for sibling to retrieve
                    unresolved.push(wildcardPattern);
                } else {
                    //don't wait for sibling add parent
                    parent.setWeight(wildcardPattern.getWeight());
                    putParent(weightMap, parent);
                }

            } else {
                WildcardPattern sibling = null;
                if (unresolved.size() > 0) {
                    sibling = unresolved.pop();
                    if (!wildcardPattern.isSibling(sibling)) {
                        //push it back
                        unresolved.push(sibling);
                        sibling = null;
                    }
                }
                //create parent
                WildcardPattern parent = wildcardPattern.clone().goUp();
                parent.setWeight((sibling == null ? 0 : sibling.getWeight()) + (isHH ? 0 : wildcardPattern.getWeight()));
                putParent(weightMap, parent);
            }
        }

        return output;
    }

    private static void putParent(TreeMap<WildcardPattern, WildcardPattern> weightMap, WildcardPattern parent) {
        WildcardPattern currentParent = weightMap.get(parent);
        if (currentParent != null) {
            parent.setWeight(currentParent.getWeight() + parent.getWeight());
            weightMap.remove(parent);
            weightMap.put(parent, parent); //don't mess with the monitors
        } else {
            weightMap.put(parent, parent);
        }
    }

    /**
     * Fill the algorithm with "capacity" number of prefixes that cover all leaves under "rootWildcardPattern"
     *
     * @param capacity
     * @param algorithm
     * @param rootWildcardPattern
     */
    public static void initMonitors(int capacity, NeedInitHHHAlgorithm algorithm, WildcardPattern rootWildcardPattern) {
        int startLevel = (int) (Math.log(capacity) / Math.log(2));
        int unusedCapacity;
        if (startLevel < rootWildcardPattern.getWildcardNum()) {
            unusedCapacity = capacity - (1 << startLevel);
        } else {
            startLevel = rootWildcardPattern.getWildcardNum();
            unusedCapacity = 0;
        }
        try {
            //create init list
            int preliminaryLevel = rootWildcardPattern.getWildcardNum() - startLevel;
            long baseData = rootWildcardPattern.getData() << startLevel;
            for (int i = 0; i < 1 << startLevel; i++) {
                WildcardPattern wildcardPattern = new WildcardPattern(baseData + i, preliminaryLevel, 0);
                algorithm.addMonitor(wildcardPattern);
            }

            //now use the unused space
            fillUnusedCapacity(unusedCapacity, algorithm);
        } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
            invalidWildCardValue.printStackTrace();
        }
    }

    private static void fillUnusedCapacity(int unusedCapacity, NeedInitHHHAlgorithm algorithm) throws WildcardPattern.InvalidWildCardValue {
//        Random random = new Random(125623);
        for (int i = 0; i < unusedCapacity; i++) {
            //pick a random node
            WildcardPattern wildcardPattern = algorithm.pollAMonitor();
            if (wildcardPattern.canGoDown()) {
                WildcardPattern lClone = wildcardPattern.clone();
                lClone.goDown(false);
                algorithm.addMonitor(lClone);
                wildcardPattern.goDown(true);
                algorithm.addMonitor(wildcardPattern);
            } else {
                algorithm.addMonitor(wildcardPattern);
            }
        }
    }

    public static boolean canGoUp(WildcardPattern wp, WildcardPattern root) {
        return (wp.canGoUp() && root.getWildcardNum() > wp.getWildcardNum());
    }

    public Collection<WildcardPattern> findHHH() {
        return findHHHPostOrder(threshold, getMonitors(), taskWildcardPattern);
    }

    @Override
    public void reset() {
        for (WildcardPattern wildcardPattern : getMonitors()) {
            wildcardPattern.setWeight(0);
        }
    }

    /**
     * @return is always sorted
     */
    public abstract Collection<WildcardPattern> getMonitors();

    public void printMonitors() {
        for (WildcardPattern monitor : getMonitors()) {
            System.out.println(monitor);
        }
    }

    public void displayHook(Collection<WildcardPattern> hhh, int step) {
        if (hhh == null) { //don't print intermediate steps
//            return;
        }
        if (graphFolder != null) {
            new GraphVizOutput().print(getMonitors(), graphFolder + "/" + getStep() + "_" + (step) + ".jpg", hhh);
        }
    }

    private Collection<WildcardPattern> findHHHInOrder(double weightThreshold) {
        Set<WildcardPattern> output = new HashSet<>();
        Collection<WildcardPattern> monitors = getMonitors();
        if (monitors.size() == 0) {
            return output;
        }
        Map<WildcardPattern, Double> weightMap = new HashMap<WildcardPattern, Double>();
        for (WildcardPattern monitor : monitors) {
            weightMap.put(monitor, monitor.getWeight());
        }

        Iterator<WildcardPattern> iterator = monitors.iterator();
        LinkedList<WildcardPattern> unResolved = new LinkedList<WildcardPattern>();
        WildcardPattern wildcardPattern = iterator.next();
        while (wildcardPattern != null) {
            if (weightMap.get(wildcardPattern) >= weightThreshold) {
                output.add(wildcardPattern);
                //just set its weight to zero
                weightMap.put(wildcardPattern, 0d);
            }
            if (!wildcardPattern.canGoUp()) {//this is the root
                break;
            }

            WildcardPattern sibling;
            if (wildcardPattern.isLeft()) {
                //next is sibling, handle it next
                unResolved.add(wildcardPattern);
                wildcardPattern = iterator.hasNext() ? iterator.next() : null;
            } else {
                //my sibling must be head of unresolved
                sibling = unResolved.pollLast();
                if (!wildcardPattern.isSibling(sibling)) {
                    System.err.println("Not found the sibling");
                    System.exit(1);
                }
                //create parent
                WildcardPattern parent = wildcardPattern.clone().goUp();
                parent.setWeight(weightMap.get(wildcardPattern) + weightMap.get(sibling));
                weightMap.put(parent, parent.getWeight());
                wildcardPattern = parent;
            }
        }
        if (unResolved.size() > 0) {
            System.err.println("Some points are unresolved");
            for (WildcardPattern pattern : unResolved) {
                System.err.println(pattern);
            }
        }
        return output;
    }
}

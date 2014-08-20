package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.overlappingsingleswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.FlowHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.SingleSwitchTask;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/28/13
 * Time: 1:42 PM <br/>
 * A single switch TCAM divide & merge algorithm that can handle overlapping monitored prefixes.
 * The general algorithm works like this:
 * pick the maximum node to divide
 * pick the two nodes with minimum total weight to merge. Merge them to their common ancestor.
 * This algorithm is only implemented to test this idea and does not work in the resource allocator framework
 */
public class OverlappingSingleSwitch extends FlowHHHAlgorithm implements SingleSwitchTask.SingleSwitchTaskImplementation {
    private TreeMap<WildcardPattern, OverlappingWildcardPattern> monitors;
    private TreeMap<WildcardPattern, WildcardPattern> weights;

    public OverlappingSingleSwitch(Element element) {
        super(element);
        int capacity = 1024;//Util.getNetwork().getFirstMonitorPoints().getCapacity();
        monitors = new TreeMap<>(WildcardPattern.WILDCARDNUM_COMPARATOR);
        weights = new TreeMap<>(WildcardPattern.WEIGHT_COMPARATOR);
        int unusedCapacity;
        int startLevel = (int) (Math.log(capacity) / Math.log(2));
        if (startLevel < taskWildcardPattern.getWildcardNum()) {
            unusedCapacity = capacity - (1 << startLevel);
        } else {
            startLevel = taskWildcardPattern.getWildcardNum();
            unusedCapacity = 0;
        }
//        int unusedCapacity = capacity - (1 << startLevel);
        int preliminaryLevel = taskWildcardPattern.getWildcardNum() - startLevel;
        long baseData = taskWildcardPattern.getData() << startLevel;
        for (int i = 0; i < 1 << startLevel; i++) {
            WildcardPattern wildcardPattern = new WildcardPattern(baseData + i, preliminaryLevel, 0);
            monitors.put(wildcardPattern, new OverlappingWildcardPattern(wildcardPattern));
        }
//        System.out.println(monitors.firstKey().getCommonParent2(monitors.lastKey()));
        for (int i = 0; i < unusedCapacity; i++) {
            monitors.firstEntry().getValue().breakNode();
        }
    }

    @Override
    public Collection<WildcardPattern> getMonitors() {
        return monitors.keySet();
    }

    @Override
    public void update() {
//        printMonitors();
        weights.clear();
        for (WildcardPattern wildcardPattern : monitors.keySet()) {
            weights.put(wildcardPattern, wildcardPattern);
        }
        //give weight to orphans
        for (OverlappingWildcardPattern overlappingWildcardPattern : monitors.values()) {
            overlappingWildcardPattern.giveOrphansShare();
        }
        WildcardPattern[] maxMins = new WildcardPattern[3];
        boolean canUpdate = true;
        int step = 0;
        while (canUpdate) {
            canUpdate = findMaxAndTwoMin(maxMins);
            if (canUpdate) {
                commitMins(maxMins);
            }
//            printMonitors();
            displayHook(null, step++);
        }
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

   /* private boolean findMaxAndTwoMin2(WildcardPattern[] toFill) {
        WildcardPattern first = null;
        WildcardPattern second = null;
        WildcardPattern max = null;
        for (WildcardPattern wildcardPattern : monitors.keySet()) {
            if (wildcardPattern.canGoUp()) {
                if (first == null || wildcardPattern.getWeight() < first.getWeight()) {
                    second = first;
                    first = wildcardPattern;
                } else {
                    if (second == null || wildcardPattern.getWeight() < second.getWeight()) {
                        second = wildcardPattern;
                    }
                }
            }
            if (max == null || wildcardPattern.getWeight() > max.getWeight()) {
                max = wildcardPattern;
            }
        }
        toFill[0] = first;
        toFill[1] = second;
        toFill[2] = max;
        return !(max == null || first == null || second == null || max.getWeight() < first.getWeight() + second.getWeight());
    }*/

    private boolean findMaxAndTwoMin(WildcardPattern[] toFill) {
        if (weights.size() != monitors.size()) {
            System.out.println();
        }

        WildcardPattern first = null;
        WildcardPattern second = null;
        double minWeight = -1;
        WildcardPattern max = null;
        do {
            if (max == null) {
                max = weights.firstKey();
            } else {
                max = weights.lowerKey(max);
            }
        } while (max != null && !max.canGoDown());

//        System.out.println("++++++++++++++++");
//        for (WildcardPattern wildcardPattern : weights.keySet()) {
//            System.out.println(wildcardPattern);
//        }
//        TreeMap<WildcardPattern, WildcardPattern> a = new TreeMap<>(new WildcardPattern.WeightComparator());
//        a.putAll(weights);
//        System.out.println("-------------------");
        if (max != null) {
            WildcardPattern[] newNodes = commitMax(max);

            for (WildcardPattern min : weights.descendingKeySet()) {
                if (min.equals(newNodes[0]) || min.equals(newNodes[1])) {
                    continue;
                }
                for (WildcardPattern larger : weights.descendingMap().tailMap(min, false).keySet()) {
                    if (larger.equals(newNodes[0]) || larger.equals(newNodes[1])) {
                        continue;
                    }
                    //check end condition
                    if (min.getWeight() + larger.getWeight() >= max.getWeight()) {
                        //this min object is useless to check more
                        break;
                    }
                    //check if larger can be paired with min
                    double newNodeWeight = canPair(min, larger);
                    if (newNodeWeight >= 0) {
                        //now check with current first and second
                        if ((first == null && newNodeWeight < max.getWeight()) || minWeight > newNodeWeight) {
                            first = min;
                            second = larger;
                            minWeight = newNodeWeight;
                        }
                        break;
                    }
                }
            }
            if (first == null) {
                commitMins(newNodes);
            }
        }

        toFill[0] = first;
        toFill[1] = second;
        toFill[2] = max;
        return max != null && first != null;
    }

    private double canPair(WildcardPattern min0, WildcardPattern min1) {
        WildcardPattern commonParent = min0.getCommonParent(min1);
        if (commonParent.equals(min0)) {
            if (getAncestor(min1).equals(commonParent)) { //is it nearest
                return min0.getWeight() + min1.getWeight();
            }
            return -1;
        } else if (commonParent.equals(min1)) {
            try {
                if (getAncestor(min0).equals(commonParent)) {//is it nearest
                    return min0.getWeight() + min1.getWeight();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        } else {
            boolean okFor0;
            boolean okFor1;
            WildcardPattern ancestor0 = getAncestor(min0);
            WildcardPattern ancestor1 = getAncestor(min1);
            if (ancestor0 == null || (ancestor0.match(commonParent) && !ancestor0.equals(commonParent))) { //is null or above commonparent
                okFor0 = true;
            } else {
                okFor0 = ancestor0.equals(commonParent);
            }
            if (ancestor1 == null || (ancestor1.match(commonParent) && !ancestor1.equals(commonParent))) { //is null or above commonparent
                okFor1 = true;
            } else {
                okFor1 = ancestor1.equals(commonParent);
            }
            if (okFor0 && okFor1) {
                //get orphans from ancestor
                double weight = min0.getWeight() + min1.getWeight();
                WildcardPattern ancestor = getAncestor(commonParent);
                if (ancestor != null) {
                    OverlappingWildcardPattern ancestorOverlappingWildcardPattern = monitors.get(ancestor);
                    if (ancestorOverlappingWildcardPattern.representsOrphans()) {
                        weight += ancestorOverlappingWildcardPattern.getOrphanWildcardPatterns(commonParent, new ArrayList<WildcardPattern>());
                    } else {
                        throw new RuntimeException("Ancestor without orphans! " + ancestor);
                    }
                }
                return weight;
            } else {
                return -1;
            }
        }
    }

    private WildcardPattern[] commitMax(WildcardPattern max) {
        return monitors.get(max).breakNode();
    }

    private void addMonitor(WildcardPattern w, OverlappingWildcardPattern o) {
        monitors.put(w, o);
        weights.put(w, w);
    }

    private void removeMonitor(WildcardPattern w) {
        monitors.remove(w);
        weights.remove(w);
    }

    private void commitMins(WildcardPattern[] mins) {

        WildcardPattern commonParent = mins[0].getCommonParent(mins[1]);
        WildcardPattern ancestor0 = null;
        WildcardPattern ancestor1 = null;
        boolean goToAncestor0 = false;
        boolean goToAncestor1 = false;
        if (commonParent.equals(mins[0])) {
            ancestor1 = getAncestor(mins[1]);
            goToAncestor1 = true;
        } else if (commonParent.equals(mins[1])) {
            ancestor0 = getAncestor(mins[0]);
            goToAncestor0 = true;
        } else {
            ancestor0 = getAncestor(mins[0]);
            ancestor1 = getAncestor(mins[1]);
            if (ancestor0 == null || (ancestor0.match(commonParent) && !ancestor0.equals(commonParent))) { //is null or above commonparent
                goToAncestor0 = false;
            } else {
                goToAncestor0 = !ancestor0.equals(commonParent);
            }
            if (ancestor1 == null || (ancestor1.match(commonParent) && !ancestor1.equals(commonParent))) { //is null or above commonparent
                goToAncestor1 = false;
            } else {
                goToAncestor1 = !ancestor1.equals(commonParent);
            }
        }
        if (goToAncestor0 && goToAncestor1) {
            // check which has min sum
            if (mins[0].getWeight() + ancestor0.getWeight() > mins[1].getWeight() + ancestor1.getWeight()) {
                gotoAncestor(mins[1], ancestor0);
            } else {
                //if equal mins[0] was smaller
                gotoAncestor(mins[0], ancestor0);
            }
        } else if (goToAncestor0) {
            gotoAncestor(mins[0], ancestor0);
        } else if (goToAncestor1) {
            gotoAncestor(mins[1], ancestor1);
        } else {
            commonParent.setWeight(mins[0].getWeight() + mins[1].getWeight());
            OverlappingWildcardPattern commonParentOverlappingWildcardPattern = monitors.get(commonParent);
            if (commonParentOverlappingWildcardPattern != null) {
                //must divide the common parent
                //in the first step left and right are not full so give each a token
                commonParentOverlappingWildcardPattern.add(monitors.get(mins[0]));
                commonParentOverlappingWildcardPattern.add(monitors.get(mins[1]));
                commonParentOverlappingWildcardPattern.addWeight(commonParent.getWeight());
                commonParentOverlappingWildcardPattern.breakNode();
            } else {
                commonParentOverlappingWildcardPattern = new OverlappingWildcardPattern(commonParent);
                //common parent must be the monitor
                //but need to track orphans.
                // 1) query nearest ancestor for orphans
                WildcardPattern ancestor = getAncestor(commonParent);
                // 2) if there is ancestor
                if (ancestor != null) {
                    //3) if there is any orphan under me
                    OverlappingWildcardPattern ancestorOverlappingWildcardPattern = monitors.get(ancestor);
                    if (ancestorOverlappingWildcardPattern.representsOrphans()) {
                        ArrayList<WildcardPattern> orphans = new ArrayList<>();
                        ancestorOverlappingWildcardPattern.getOrphanWildcardPatterns(commonParent, orphans);
                        if (orphans.size() > 0) {
                            double weight = ancestorOverlappingWildcardPattern.removeOrphanWildcardPatterns(orphans);
                            commonParent.setWeight(commonParent.getWeight() + weight);
                            commonParentOverlappingWildcardPattern.addOrphans(orphans);
                            addMonitor(mins, commonParent, commonParentOverlappingWildcardPattern);
                            try {
                                ancestorOverlappingWildcardPattern.reArrange();
                            } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
                                System.out.println("Ancestor cannot go down!");
                                invalidWildCardValue.printStackTrace();
                            }
                        } else {
                            //OK no orphan under me from ancestor but these two may be orphan or has orphans
                            addMonitor(mins, commonParent, commonParentOverlappingWildcardPattern);
                        }
                    } else {
                        throw new RuntimeException("Ancestor without orphans! " + ancestor);
                    }

                } else { //no ancestor. No orphans
                    //OK no orphan under me from ancestor but these two may be orphan or has orphans
                    addMonitor(mins, commonParent, commonParentOverlappingWildcardPattern);
                }
            }
            removeMonitor(mins[0]);
            removeMonitor(mins[1]);
        }
    }

    private void gotoAncestor(WildcardPattern min, WildcardPattern ancestor0) {
        OverlappingWildcardPattern ancestor0OverlappingWildcardPattern = monitors.get(ancestor0);
        ancestor0OverlappingWildcardPattern.add(monitors.get(min));
        ancestor0OverlappingWildcardPattern.addWeight(min.getWeight());
        removeMonitor(min);
    }

    private void addMonitor(WildcardPattern[] mins, WildcardPattern commonParent, OverlappingWildcardPattern commonParentOverlappingWildcardPattern) {
        OverlappingWildcardPattern min0 = monitors.get(mins[0]);
        OverlappingWildcardPattern min1 = monitors.get(mins[1]);
        if (!min0.representsOrphans() && !min1.representsOrphans() && commonParent.isChild(mins[0]) && commonParent.isChild((mins[1]))) {
            // I will be just a leaf so no orphan is necessary
        } else {
            commonParentOverlappingWildcardPattern.add(min0);
            commonParentOverlappingWildcardPattern.add(min1);
        }
        addMonitor(commonParent, commonParentOverlappingWildcardPattern);
    }

    private WildcardPattern getAncestor(WildcardPattern commonParent) {
        WildcardPattern ancestor = null;
        commonParent = commonParent.clone().goUp();
        for (WildcardPattern wildcardPattern : monitors.descendingKeySet()) {//TOP TO BOTTOM
            if (wildcardPattern.getWildcardNum() < commonParent.getWildcardNum()) {
                break;
            }
            if (wildcardPattern.match(commonParent)) { //find lowest level ancester
                ancestor = wildcardPattern;
            }
        }
        return ancestor;
    }

    private class OverlappingWildcardPattern {
        private WildcardPattern wildcardPattern;
        private Set<WildcardPattern> orphanWildcardPatterns;

        private OverlappingWildcardPattern(WildcardPattern wildcardPattern) {
            this.wildcardPattern = wildcardPattern;
        }

        public void add(OverlappingWildcardPattern overlappingWildcardPattern) {
            if (orphanWildcardPatterns == null) {
                orphanWildcardPatterns = new HashSet<>();
            }
            if (overlappingWildcardPattern.representsOrphans()) {
                orphanWildcardPatterns.addAll(overlappingWildcardPattern.getOrphanWildcardPatterns()); //would be nice if merge mergable orphans
            } else {
                orphanWildcardPatterns.add(overlappingWildcardPattern.wildcardPattern); //would be nice if merge mergable orphans
            }
        }

        public void addOrphan(WildcardPattern orphan) {
            if (orphanWildcardPatterns == null) {
                orphanWildcardPatterns = new HashSet<>();
            }
            orphanWildcardPatterns.add(orphan);//would be nice if merge mergable orphans
        }

        public void addOrphans(Collection<WildcardPattern> orphans) {
            if (orphanWildcardPatterns == null) {
                orphanWildcardPatterns = new HashSet<>();
            }
            orphanWildcardPatterns.addAll(orphans);//would be nice if merge mergable orphans
        }

        public Collection<WildcardPattern> getOrphanWildcardPatterns() {
            return orphanWildcardPatterns;
        }

        private double getOrphanWildcardPatterns(WildcardPattern toMatch, Collection<WildcardPattern> orphans) {
            return getOrphanWildcardPatterns(toMatch, orphans, orphanWildcardPatterns);
        }

        private double getOrphanWildcardPatterns(WildcardPattern toMatch, Collection<WildcardPattern> toFill, Collection<WildcardPattern> toSearchIn) {
            double weight = 0;
            for (WildcardPattern orphanWildcardPattern : toSearchIn) {
                if (toMatch.match(orphanWildcardPattern)) {
                    toFill.add(orphanWildcardPattern);
                    weight += orphanWildcardPattern.getWeight();
                }
            }
            return weight;
        }

        private double removeOrphanWildcardPatterns(Collection<WildcardPattern> orphans) {
            double weight = 0;
            orphanWildcardPatterns.removeAll(orphans);
            for (WildcardPattern orphan : orphans) {
                orphanWildcardPatterns.remove(orphan);
                weight += orphan.getWeight();
            }
            weights.remove(wildcardPattern);
            wildcardPattern.setWeight(wildcardPattern.getWeight() - weight);
            weights.put(wildcardPattern, wildcardPattern);
            return weight;
        }

        private boolean representsOrphans() {
            return orphanWildcardPatterns != null && orphanWildcardPatterns.size() > 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OverlappingWildcardPattern)) return false;

            OverlappingWildcardPattern that = (OverlappingWildcardPattern) o;

            if (!wildcardPattern.equals(that.wildcardPattern)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return wildcardPattern.hashCode();
        }

        @Override
        public String toString() {
            return
                    "wildcardPattern=" + wildcardPattern +
                            ", orphanWildcardPatterns=" + orphanWildcardPatterns +
                            '}';
        }

        /**
         * both left and right must have orphans
         */
        private WildcardPattern[] breakNode() {
            WildcardPattern[] newNodes = new WildcardPattern[2];
            try {
                removeMonitor(wildcardPattern);
                if (this.representsOrphans()) {
                    //find common parent for right and left orphans
                    Collection<WildcardPattern> tempOrphanWildcardPatterns = new ArrayList<>(this.getOrphanWildcardPatterns().size());
                    boolean addLeft = false;
                    boolean addRight = false;
                    {
                        //left
                        WildcardPattern left = wildcardPattern.clone().goDown(false);
                        double leftWeight = this.getOrphanWildcardPatterns(left, tempOrphanWildcardPatterns);
                        left = getCommonOrphansParent(tempOrphanWildcardPatterns);
                        if (tempOrphanWildcardPatterns.size() > 0) {
                            left.setWeight(leftWeight);
                            OverlappingWildcardPattern value = new OverlappingWildcardPattern(left);
                            if (tempOrphanWildcardPatterns.size() > 1) {
                                value.addOrphans(tempOrphanWildcardPatterns);
                            }
                            addMonitor(left, value);
                            addLeft = true;
                            tempOrphanWildcardPatterns.clear();
                            newNodes[0] = left;
                        }
                    }
                    {
                        //right
                        WildcardPattern right = wildcardPattern.clone().goDown(true);
                        double rightWeight = this.getOrphanWildcardPatterns(right, tempOrphanWildcardPatterns);
                        right = getCommonOrphansParent(tempOrphanWildcardPatterns);
                        if (tempOrphanWildcardPatterns.size() > 0) {
                            right.setWeight(rightWeight);
                            OverlappingWildcardPattern value = new OverlappingWildcardPattern(right);
                            if (tempOrphanWildcardPatterns.size() > 1) {
                                value.addOrphans(tempOrphanWildcardPatterns);
                            }
                            addMonitor(right, value);
                            addRight = true;
                            newNodes[1] = right;
                        }
                    }
                    if (!addLeft || !addRight) {
                        throw new RuntimeException("No empty right or left" + this);
                    }
                } else {
                    // regular break
                    WildcardPattern left = wildcardPattern.clone().goDown(false);
                    left.setWeight(wildcardPattern.getWeight() / 2);
                    addMonitor(left, new OverlappingWildcardPattern(left));
                    newNodes[0] = left;
                    WildcardPattern right = wildcardPattern.clone().goDown(true);
                    right.setWeight(wildcardPattern.getWeight() / 2);
                    addMonitor(right, new OverlappingWildcardPattern(right));
                    newNodes[1] = right;
                }

            } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
                invalidWildCardValue.printStackTrace();
            }
            return newNodes;
        }

        public WildcardPattern getCommonOrphansParent(Collection<WildcardPattern> wildcardPatternsSet) {
            if (wildcardPatternsSet.size() == 0) {
                return null;
            }
            Iterator<WildcardPattern> itr = wildcardPatternsSet.iterator();
            WildcardPattern output = itr.next().clone();
            while (itr.hasNext()) {
                output = output.getCommonParent(itr.next());

            }
            output.setWeight(0);
            return output;
        }

        public void reArrange() throws WildcardPattern.InvalidWildCardValue {
            //check to have orphans from both left and right
            Collection<WildcardPattern> leftOrphanWildcardPatterns = new ArrayList<>(this.getOrphanWildcardPatterns().size());
            WildcardPattern left = wildcardPattern.clone().goDown(false);
            this.getOrphanWildcardPatterns(left, leftOrphanWildcardPatterns);
            boolean leftHasOrphans = leftOrphanWildcardPatterns.size() > 0;

            Collection<WildcardPattern> rightOrphanWildcardPatterns = new ArrayList<>(this.getOrphanWildcardPatterns().size());
            WildcardPattern right = wildcardPattern.clone().goDown(true);
            this.getOrphanWildcardPatterns(right, rightOrphanWildcardPatterns);
            boolean rightHasOrphans = rightOrphanWildcardPatterns.size() > 0;

            if (leftHasOrphans && rightHasOrphans) {
                //stay
            } else if (!leftHasOrphans && !rightHasOrphans) {
                throw new RuntimeException("reArrange " + this + " no orphans in right or left");
            } else if (!leftHasOrphans) {
                //go to right which has orphans. right cannot be in monitors
                handToChild(rightOrphanWildcardPatterns);
            } else {
                //go to left
                handToChild(leftOrphanWildcardPatterns);
            }
        }

        private void handToChild(Collection<WildcardPattern> childOrphanWildcardPatterns) {
            WildcardPattern child;
            child = getCommonOrphansParent(childOrphanWildcardPatterns);
            removeMonitor(wildcardPattern);
            OverlappingWildcardPattern value = new OverlappingWildcardPattern(child);
            if (childOrphanWildcardPatterns.size() > 1) {//this is not a leaf
                value.addOrphans(childOrphanWildcardPatterns);
            }
            child.setWeight(wildcardPattern.getWeight());
            //it cannot be there otherwise I did not have those orphans
            if (monitors.containsKey(child)) {
                throw new RuntimeException("Child should not be in monitors " + child);
            }
            addMonitor(child, value);
        }

        public void addWeight(double weight) {
            weights.remove(wildcardPattern);
            wildcardPattern.setWeight(wildcardPattern.getWeight() + weight);
            weights.put(wildcardPattern, wildcardPattern);
        }

        public void giveOrphansShare() {
            if (representsOrphans()) {
                try {
                    if (orphanWildcardPatterns.contains(wildcardPattern)) {
                        throw new RuntimeException("I am my orphan!" + this);
                    }
                    WildcardPattern mergedOrphan = giveOrphansShare(wildcardPattern, orphanWildcardPatterns, wildcardPattern.getWeight());
                    if (mergedOrphan != null && mergedOrphan.equals(wildcardPattern)) {
                        orphanWildcardPatterns.clear();
                    }
                } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
                    invalidWildCardValue.printStackTrace();
                }
            }
        }

        private WildcardPattern giveOrphansShare(WildcardPattern toMatch, Collection<WildcardPattern> consideringOrphans,
                                                 double share) throws WildcardPattern.InvalidWildCardValue {

            if (consideringOrphans.size() == 1) { //if it is leaf so at most it has one orphan
                //give all to this
                WildcardPattern onlyOrphan = consideringOrphans.iterator().next();
                onlyOrphan.setWeight(share);
                return onlyOrphan;
            }
            if (consideringOrphans.size() == 2) {
                //check for merge
                Iterator<WildcardPattern> iterator = consideringOrphans.iterator();
                WildcardPattern first = iterator.next();
                WildcardPattern second = iterator.next();
                if (first.isSibling(second)) {
                    return mergeOrphans(toMatch, share, first, second);
                }
            }
            ArrayList<WildcardPattern> leftOrphans = new ArrayList<>();
            WildcardPattern left = toMatch.clone().goDown(false);
            getOrphanWildcardPatterns(left, leftOrphans, consideringOrphans);
            ArrayList<WildcardPattern> rightOrphans = new ArrayList<>();
            WildcardPattern right = toMatch.clone().goDown(true);
            getOrphanWildcardPatterns(right, rightOrphans, consideringOrphans);
            if (leftOrphans.size() > 0 && rightOrphans.size() > 0) {
                //devide share by 2
                WildcardPattern mergedLeft = giveOrphansShare(left, leftOrphans, share / 2);
                WildcardPattern mergedRight = giveOrphansShare(right, rightOrphans, share / 2);
                if (mergedLeft != null && mergedRight != null && mergedLeft.isSibling(mergedRight)) {
                    return mergeOrphans(toMatch, share, mergedLeft, mergedRight);
                }
                return null;
            } else if (leftOrphans.size() > 0) {
                //run for left
                return giveOrphansShare(left, leftOrphans, share);
            } else {
                //run for left
                return giveOrphansShare(right, rightOrphans, share);
            }
        }

        private WildcardPattern mergeOrphans(WildcardPattern toMatch, double share, WildcardPattern first, WildcardPattern second) {
            orphanWildcardPatterns.remove(first);
            orphanWildcardPatterns.remove(second);
            WildcardPattern parent = first.goUp();
            orphanWildcardPatterns.add(parent);
            parent.setWeight(share);
            return parent;
        }
    }
}

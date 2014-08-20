package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/22/13
 * Time: 10:31 PM <br/>
 * Represents a node in the prefix tree
 */
public class MultiSwitchWildcardPattern implements Comparable<MultiSwitchWildcardPattern> {
    /**
     * prefix
     */
    private final WildcardPattern wildcardPattern;

    /**
     * The weight of node plus left subtree and right subtree
     */
    protected double internalWeight = 0;

    /**
     * All prefixes that have traffic on this prefix
     */
    private final Set<MonitorPoint> monitorPoints;

    /**
     * The set of common switches among left and right subtree
     */
    private Set<MonitorPoint> commonSet;

    /**
     * commonSet + common set of left and right.
     * This is equal to the any switch that we can save an entry on if we collapse all leaves to this internal node
     */
    private Set<MonitorPoint> internalCommonSet;

    private MultiSwitchWildcardPattern right = null;
    protected MultiSwitchWildcardPattern left = null;

    public MultiSwitchWildcardPattern(WildcardPattern wildcardPattern, Set<MonitorPoint> monitorPoints) {
        this.wildcardPattern = wildcardPattern;
        this.monitorPoints = monitorPoints;
    }

    public void compress() {
        right = null;
        left = null;
    }

    public WildcardPattern getWildcardPattern() {
        return wildcardPattern;
    }

    public double getBenefit() {
        return wildcardPattern.getWeight();
    }

    public double getCost() {
        return getInternalWeight();
    }

    public Set<MonitorPoint> getMonitorPoints() {
        return monitorPoints;
    }

    public double getInternalWeight() {
        return internalWeight;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(wildcardPattern.toString() + " (");
        boolean first = true;
        for (MonitorPoint monitorPoint : monitorPoints) {
            sb.append(first ? "" : ",").append(monitorPoint.getIntId());
            first = false;
        }
        return sb.append(")").toString();
    }

    @Override
    public int compareTo(MultiSwitchWildcardPattern o) {
        return wildcardPattern.compareTo(o.wildcardPattern);
    }

    public Set<MonitorPoint> getCommonSet() {
        return commonSet;
    }

    Set<MonitorPoint> getInternalCommonSet() {
        if (internalCommonSet == null) {
            internalCommonSet = Util.cloneSet(getMonitorPoints());
            internalCommonSet.clear();
            internalWeight = 0;
        }
        return internalCommonSet;
    }

    public MultiSwitchWildcardPattern getLeft() throws WildcardPattern.InvalidWildCardValue {
        if (left == null) {
            WildcardPattern wp = wildcardPattern.clone().goDown(false);
            left = new MultiSwitchWildcardPattern(wp, MultiSwitch2.hasDataFrom(wp, monitorPoints));
        }
        return left;
    }

    public MultiSwitchWildcardPattern getRight() throws WildcardPattern.InvalidWildCardValue {
        if (right == null) {
            WildcardPattern wp = wildcardPattern.clone().goDown(true);
            right = new MultiSwitchWildcardPattern(wp, MultiSwitch2.hasDataFrom(wp, monitorPoints));
        }
        return right;
    }

    public void computeCommonSet() throws WildcardPattern.InvalidWildCardValue {
        commonSet = Util.cloneSet(getLeft().getMonitorPoints());
        commonSet.retainAll(getRight().getMonitorPoints());
    }

    /**
     * re-populate the common set and internal common set of this internal node
     *
     * @throws WildcardPattern.InvalidWildCardValue
     */
    public void computeInternalCommonSet() throws WildcardPattern.InvalidWildCardValue {
        if (commonSet == null) {
            computeCommonSet();
        }
        if (internalCommonSet == MultiSwitch2.EMPTY_SET || internalCommonSet == null) {
            internalCommonSet = Util.cloneSet((getLeft().getInternalCommonSet()));
        } else {
            internalCommonSet.clear();
            internalCommonSet.addAll(getLeft().getInternalCommonSet());
        }
        internalCommonSet.addAll(getRight().getInternalCommonSet());
        internalCommonSet.addAll(commonSet);
        updateInternalWeight();
    }

    public double getWeight() {
        return wildcardPattern.getWeight();
    }

    /**
     * You should not call this on leaves
     *
     * @param weight
     */
    public void setWeight(double weight) {
        wildcardPattern.setWeight(weight);
        updateInternalWeight();
    }


    public void resetInternalCommonSet() {
        internalCommonSet.clear();
        internalWeight = 0;
    }

    /**
     * always make sure left and right is there
     *
     * @throws edu.usc.enl.dynamicmeasurement.model.WildcardPattern.InvalidWildCardValue
     */
    public void updateInternalWeight() {

        try {
            if (left != null) { //if it is really an internal node, its left should not be null by now
                internalWeight = getWeight() + getLeft().getInternalWeight() + getRight().getInternalWeight();
            }
        } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
        }
    }
}

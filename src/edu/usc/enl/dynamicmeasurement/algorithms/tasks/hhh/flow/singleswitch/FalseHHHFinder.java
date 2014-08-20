package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.singleswitch;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/19/13
 * Time: 6:34 PM  <br/>
 * For finding the precision of Hierarchical Heavy Hitters.
 * This class finds the probability of a HHH to be false or true and sums that up over all HHHs
 */
public class FalseHHHFinder {
    /**
     * For checking the descendant HHHs, using the threshold may be overkilling,
     * If true, lets refining accuracy using average weight of HHHs we think are true HHHs
     */
    private final boolean refine;
    private List<WildcardPattern> lastFalseHHHs;
    private final int wildcardNum;

    public FalseHHHFinder(boolean refine, int wildcardNum) {
        this.refine = refine;
        this.wildcardNum = wildcardNum;
    }

    public boolean isRefine() {
        return refine;
    }

    public List<WildcardPattern> getLastFalseHHHs() {
        return lastFalseHHHs;
    }

    public double findFalseHHHs2(Collection<WildcardPattern> reportedHHH, Collection<WildcardPattern> monitors,
                                 double threshold) {
        TreeMap<WildcardPattern, Boolean> bottomUpTrueHHHs = new TreeMap<>(WildcardPattern.WILDCARDNUM_COMPARATOR);
        for (WildcardPattern wildcardPattern : reportedHHH) {
            bottomUpTrueHHHs.put(wildcardPattern, Boolean.FALSE);
        }
        double accuracy = 0;
        double sum = 0;
        List<WildcardPattern> falseHHHs = new LinkedList<>();
        for (Map.Entry<WildcardPattern, Boolean> entry : bottomUpTrueHHHs.entrySet()) {
            WildcardPattern hhh = entry.getKey();
            boolean isExact = hhh.getWildcardNum() == wildcardNum;

            //find if there is any descendent hhh that is false
            //if (checkDescendantHHHs(falseHHHs, hhh)) {
            if (!isExact && checkForDescendantHHHs(falseHHHs, hhh, threshold, reportedHHH, monitors)) {
                falseHHHs.add(hhh);
                continue;
            }

            // now check monitors, if it is exact or there is a monitor under me it is OK
            boolean isTrue = isExact || hasDescendantMonitors(monitors, hhh);
            if (isTrue) {
                sum += hhh.getWeight();
                accuracy++;
            } else {
                // This is not an exact HHH but a fringe one without any descendant HHH or monitor.
                // It can only be a hope to be a true HHH if it sweight is <2*threshold
                if (hhh.getWeight() < 2 * threshold) {
                    accuracy += 0.5;
                }
                falseHHHs.add(hhh);
            }
        }
        if (refine) {
            // Using the average weight of HHHs, check again the descendant HHHs
            double averageEasyTrueHHHWeight = sum / (reportedHHH.size() - falseHHHs.size());
            for (Iterator<WildcardPattern> iterator = falseHHHs.iterator(); iterator.hasNext(); ) {
                WildcardPattern falseHHH = iterator.next();
                if (!checkForDescendantHHHs(falseHHHs, falseHHH, averageEasyTrueHHHWeight, reportedHHH, monitors)) {
                    iterator.remove();
                }
            }
        }

        lastFalseHHHs = falseHHHs;
        return accuracy;
    }

    public Map<WildcardPattern, Double> findFalseHHHs(Collection<WildcardPattern> reportedHHH, Collection<WildcardPattern> monitors,
                                                      double threshold) {
        TreeMap<WildcardPattern, Double> bottomUpHHHsReport = new TreeMap<>(WildcardPattern.WILDCARDNUM_COMPARATOR);
        for (WildcardPattern wildcardPattern : reportedHHH) {
            bottomUpHHHsReport.put(wildcardPattern, 0d);
        }
        double trueHHHSum = 0;
        List<WildcardPattern> falseHHHs = new LinkedList<>();
        for (Map.Entry<WildcardPattern, Double> entry : bottomUpHHHsReport.entrySet()) {
            WildcardPattern hhh = entry.getKey();
            if (hhh.getWildcardNum() == wildcardNum) { //exact hhhs are always true
                entry.setValue(1d);
                trueHHHSum += hhh.getWeight();
            } else {
                //find if there is any descendent hhh that is false
                //find descendant monitors
                if (!hasDescendantMonitors(monitors, hhh)) {
                    falseHHHs.add(hhh);
                    if (hhh.getWeight() < 2 * threshold) {
                        entry.setValue(0.5);
                    }
                } else {
                    //check descendant HHHs
                    if (checkForDescendantHHHs(falseHHHs, hhh, threshold, bottomUpHHHsReport.keySet(), monitors)) {
                        entry.setValue(1d);
                        trueHHHSum += hhh.getWeight();
                    } else {
                        falseHHHs.add(hhh);
                    }
                }
            }
        }
        if (refine) {
            double averageEasyTrueHHHWeight = trueHHHSum / (reportedHHH.size() - falseHHHs.size());
            for (Iterator<WildcardPattern> iterator = falseHHHs.iterator(); iterator.hasNext(); ) {
                WildcardPattern falseHHH = iterator.next();
                if (checkForDescendantHHHs(falseHHHs, falseHHH, averageEasyTrueHHHWeight, bottomUpHHHsReport.keySet(), monitors)) {
                    iterator.remove();
                    bottomUpHHHsReport.put(falseHHH, 1d);
                }
            }
        }
        lastFalseHHHs = falseHHHs;
        return bottomUpHHHsReport;
    }

    protected boolean hasDescendantMonitors(Collection<WildcardPattern> monitors, WildcardPattern hhh) {
        boolean hasDescendantMonitor = false;
        for (WildcardPattern monitor : monitors) {
            if (hhh.match(monitor) && !hhh.equals(monitor)) {
                hasDescendantMonitor = true;
                break;
            }
        }
        return hasDescendantMonitor;
    }

    protected boolean checkForDescendantHHHs(List<WildcardPattern> falseHHHs, WildcardPattern hhh, double threshold,
                                             Collection<WildcardPattern> bottomUPHHHs,
                                             Collection<WildcardPattern> monitors) {
        List<WildcardPattern> descendantFalseHHHs = new ArrayList<>();
        for (WildcardPattern falseHHH : falseHHHs) {
            if (hhh.match(falseHHH)) {
                if (falseHHH.getWeight() >= 2 * threshold) {
                    return false;
                } else {
                    //there is still hope that even if the descendant hhh is false, I am true
                    descendantFalseHHHs.add(falseHHH);
                }
            }
        }
        if (descendantFalseHHHs.size() <= 0) {
            return true;
        }
        if (descendantFalseHHHs.size() > 0) {
            //check if descendentHHHs become smaller down to threshold, the hhh will still remain hhh
            Collection<WildcardPattern> directMonitors = getDirectMonitors(hhh, bottomUPHHHs, monitors);
            //Reduce the size of descendant false hhhs to weight-threshold,
            //as the real descendant hhh will take at least that much and the remaining will be gathered here
            for (WildcardPattern descendantFalseHHH : descendantFalseHHHs) {
                directMonitors.add(new WildcardPattern(descendantFalseHHH.getData(), descendantFalseHHH.getWildcardNum(), descendantFalseHHH.getWeight() - threshold));
            }
            //now check and see if hhh is still an HHH based on monitors
            // the HHH is at least grandparent of descendant false hhhs so if
            //any of its children weight become>threshold, it is no longer an hhh
            try {
                WildcardPattern leftChild = hhh.clone().goDown(false);
                double rightSum = 0;
                double leftSum = 0;
                for (WildcardPattern newMonitor : directMonitors) {
                    if (leftChild.match(newMonitor)) {
                        leftSum += newMonitor.getWeight();
                    } else {
                        rightSum += newMonitor.getWeight();
                    }
                }
                return leftSum < threshold && rightSum < threshold;
            } catch (WildcardPattern.InvalidWildCardValue invalidWildCardValue) {
                invalidWildCardValue.printStackTrace();
            }
        }
        return false;
    }

    private Collection<WildcardPattern> getDirectMonitors(WildcardPattern hhh, Collection<WildcardPattern> bottomUpHHHs,
                                                          Collection<WildcardPattern> monitors) {
        List<WildcardPattern> directMonitors = new LinkedList<>(monitors);
        for (Iterator<WildcardPattern> iterator = directMonitors.iterator(); iterator.hasNext(); ) {
            WildcardPattern newMonitor = iterator.next();
            if (!hhh.match(newMonitor)) {
                iterator.remove();
            }
        }
        for (WildcardPattern bHHH : bottomUpHHHs) {
            if (bHHH.getWildcardNum() >= hhh.getWildcardNum()) { //equal condition here also breaks hhh itself
                break;
            }
            if (hhh.match(bHHH)) {
                for (Iterator<WildcardPattern> iterator = directMonitors.iterator(); iterator.hasNext(); ) {
                    WildcardPattern monitor = iterator.next();
                    if (bHHH.match(monitor)) {
                        iterator.remove();
                    }
                }
            }
        }
        return directMonitors;
    }
}

package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.tcammultitaskmultiswitch.TCAMMultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/22/13
 * Time: 5:11 PM <br/>
 * This is a special class for joint TCAM configuration and TCAM allocation among tasks.
 * I'm not sure even if this works or not
 */
public class MultiTaskMultiSwitchResourceNegotiator {
    private Map<TCAMMultiSwitchTask, AccuracyAggregator> tasks;
    private final MatrixSet.MatrixMapping<MonitorPoint> mapping;
    private final Map<MultiSwitch2, TCAMMultiSwitchTask> algorithmTaskMap;
    private final Element accuracyAggregatorElement;

    public MultiTaskMultiSwitchResourceNegotiator(Element element) {
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        accuracyAggregatorElement = properties.get("AccuracyAggregator");
        mapping = new MatrixSet.MatrixMapping<>();
        mapping.addAll(Util.getNetwork().getMonitorPoints());
        algorithmTaskMap = new HashMap<>();
    }

    public Collection<MonitorPoint> getMonitorPoints() {
        return mapping;
    }

    public double findCandidateSiblings(MultiSwitch2 multiSwitch, Set<MonitorPoint> toFreeMonitorPoints, double benefit,
                                        MultiSwitchWildcardPattern maxEntry) {
        TCAMMultiSwitchTask task = algorithmTaskMap.get(multiSwitch);
        List<SetIdentifier> solution = new ArrayList<>();
        //first find tasks that used resources on the toFreeMonitorPoints
        MatrixSet<MonitorPoint> toFreeMonitorPoints2 = ((MatrixSet<MonitorPoint>) toFreeMonitorPoints).clone();
        List<TCAMMultiSwitchTask> candidateTasks;
        if (tasks.get(task).getAccuracy() == 0) {
            candidateTasks = new LinkedList<>();
            candidateTasks.add(task);
        } else {
            candidateTasks = getCandidateTasks(toFreeMonitorPoints2);
        }

        //now for each task find set of siblings that match criteria
        Map<SetIdentifier, Set<MonitorPoint>> sets = new HashMap<>();
        Map<SetIdentifier, Double> costs = new HashMap<>();
        prepare(task, benefit, toFreeMonitorPoints2, candidateTasks, sets, costs);
        if (sets.isEmpty()) {
            //as maxes are sorted we cannot find any candidates from now on
            return -1;
        }
        solution.clear();
        double cost = new SubSetCoverSolver<MonitorPoint>().solve(toFreeMonitorPoints, sets, costs, solution);
        if (cost >= 0 && cost < benefit) {
            return commit(task, solution, cost, maxEntry, toFreeMonitorPoints2); //use toFreeMonitorPoints2 as the other should be empty by now
        }
        return 0;
    }

    private void prepare(TCAMMultiSwitchTask task, double benefit, MatrixSet<MonitorPoint> toFreeMonitorPoints2,
                         List<TCAMMultiSwitchTask> candidateTasks, Map<SetIdentifier, Set<MonitorPoint>> sets, Map<SetIdentifier, Double> costs) {
        for (TCAMMultiSwitchTask candidateTask : candidateTasks) {
            double coef;
            if (task.equals(candidateTask)) {
                coef = 1;
                //to skip the case that accuracy is 0 but it is the same task.
                // Note the cases with 0 accuracy and different tasks has been filtered before in the candidatetasks list
            } else {
                coef = task.getThreshold() * tasks.get(task).getAccuracy() / candidateTask.getThreshold() / tasks.get(candidateTask).getAccuracy();
            }
            Map<WildcardPattern, MultiSwitchWildcardPattern> internalNodes = candidateTask.getMultiSwitch().internalNodes;
            for (Map.Entry<WildcardPattern, MultiSwitchWildcardPattern> entry : internalNodes.entrySet()) {
                MultiSwitchWildcardPattern mswp = entry.getValue();

                //criteria should also depend on threshold and precision difference!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if (((MatrixSet<MonitorPoint>) mswp.getMonitorPoints()).getSimilarity(toFreeMonitorPoints2) > 0) {

                    if (mswp.getCost() * coef < benefit) {
                        SetIdentifier key = new SetIdentifier(entry.getKey(), candidateTask);
                        sets.put(key, Util.cloneSet(mswp.getInternalCommonSet()));
                        costs.put(key, mswp.getCost());
                    }
                }
            }
        }
    }

    private double commit(TCAMMultiSwitchTask task, List<SetIdentifier> solution, double cost, MultiSwitchWildcardPattern maxEntry, Set<MonitorPoint> toFreeMonitorPoints) {
        Map<TCAMMultiSwitchTask, List<WildcardPattern>> taskMergedWildcardPatterns = new HashMap<>();
        for (SetIdentifier setIdentifier : solution) {
            TCAMMultiSwitchTask helpfulTask = setIdentifier.getTask();
//            if (helpfulTask!=task){
//                System.out.println();
//            }
            List<WildcardPattern> list = taskMergedWildcardPatterns.get(helpfulTask);
            if (list == null) {
                list = new LinkedList<>();
                taskMergedWildcardPatterns.put(helpfulTask, list);
            }
            list.add(setIdentifier.getWildcardPattern());
            //update capacity
            Set<MonitorPoint> useFulMonitorPoints = setIdentifier.getUsefulMonitorPoints();
            for (MonitorPoint useFulMonitorPoint : useFulMonitorPoints) {
                useFulMonitorPoint.decCapacity();
            }
        }
        for (MonitorPoint toFreeMonitorPoint : toFreeMonitorPoints) {
            toFreeMonitorPoint.incCapacity();
        }

        //commit max
        task.getMultiSwitch().commitDivide(maxEntry);
        //commit merging //may be in multiple tasks
        for (Map.Entry<TCAMMultiSwitchTask, List<WildcardPattern>> entry : taskMergedWildcardPatterns.entrySet()) {
            entry.getKey().getMultiSwitch().commitMerge(entry.getValue());
        }
        return cost;
    }


    private List<TCAMMultiSwitchTask> getCandidateTasks(MatrixSet<MonitorPoint> toFreeMonitorPoints2) {
        List<TCAMMultiSwitchTask> candidateTasks = new LinkedList<>();
        for (Map.Entry<TCAMMultiSwitchTask, AccuracyAggregator> entry : tasks.entrySet()) {
            if (entry.getKey().usedResourceOn(toFreeMonitorPoints2) && entry.getValue().getAccuracy() > 0) {
                candidateTasks.add(entry.getKey());
            }
        }
        return candidateTasks;
    }

    private MatrixSet<MonitorPoint> getMatrixSet(Set<MonitorPoint> toFreeMonitorPoints) {
        MatrixSet<MonitorPoint> toFreeMonitorPoints2;
        if (toFreeMonitorPoints instanceof MatrixSet) {
            toFreeMonitorPoints2 = (MatrixSet<MonitorPoint>) toFreeMonitorPoints;
        } else {
            toFreeMonitorPoints2 = new MatrixSet<>(mapping);
            toFreeMonitorPoints2.addAll(toFreeMonitorPoints);
        }
        return toFreeMonitorPoints2;
    }

    public void initialize(Set<TCAMMultiSwitchTask> TCAMMultiSwitchTasks) {
        try {
            algorithmTaskMap.clear();
            for (TCAMMultiSwitchTask task : TCAMMultiSwitchTasks) {
                AccuracyAggregator accuracyAggregator = (AccuracyAggregator) Class.forName(accuracyAggregatorElement.getAttribute(ConfigReader.PROPERTY_VALUE)).
                        getConstructor(Element.class).newInstance(accuracyAggregatorElement);
                tasks.put(task, accuracyAggregator);
                algorithmTaskMap.put(task.getMultiSwitch(), task);
                task.getMultiSwitch().setResourceNegotiator(this);
            }
            //Assign capacity of monitorpoints for each task, do equal share
            //find the number of tasks per monitor point
            Map<MonitorPoint, Integer> monitorPointTaskNumMap = new HashMap<>();
            Map<MonitorPoint, Integer> capacities = new HashMap<>();
            for (MonitorPoint monitorPoint : mapping) {
                monitorPointTaskNumMap.put(monitorPoint, 0);
                capacities.put(monitorPoint, monitorPoint.getCapacity());
            }

            for (TCAMMultiSwitchTask task : tasks.keySet()) {
                for (MonitorPoint monitorPoint : task.getMonitorPoints()) {
                    monitorPointTaskNumMap.put(monitorPoint, monitorPointTaskNumMap.get(monitorPoint) + 1);
                }
            }
            for (TCAMMultiSwitchTask task : tasks.keySet()) {
                for (MonitorPoint monitorPoint : task.getMonitorPoints()) {
                    monitorPoint.setCapacity(capacities.get(monitorPoint) / monitorPointTaskNumMap.get(monitorPoint));
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void allocate(Set<TCAMMultiSwitchTask> TCAMMultiSwitchTasks) {
        //nothing to do as tasks take resources during the update step
    }

    private class SetIdentifier implements SubSetCoverSolver.DetailSolution {
        private final WildcardPattern wildcardPattern;
        private final TCAMMultiSwitchTask task;
        private Set<MonitorPoint> usefulMonitorPoints;

        private SetIdentifier(WildcardPattern wildcardPattern, TCAMMultiSwitchTask task) {
            this.wildcardPattern = wildcardPattern;
            this.task = task;
        }

        private Set<MonitorPoint> getUsefulMonitorPoints() {
            return usefulMonitorPoints;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SetIdentifier that = (SetIdentifier) o;

            if (!task.equals(that.task)) return false;
            if (!wildcardPattern.equals(that.wildcardPattern)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = wildcardPattern.hashCode();
            result = 31 * result + task.hashCode();
            return result;
        }

        private WildcardPattern getWildcardPattern() {
            return wildcardPattern;
        }

        private TCAMMultiSwitchTask getTask() {
            return task;
        }

        @Override
        public void setUsefulFor(Set monitorPoints, double avgCost) {
            this.usefulMonitorPoints = ((Set<MonitorPoint>) monitorPoints);
        }
    }

    @Override
    public String toString() {
        return "Multi_Multi_TCAM";
    }
}

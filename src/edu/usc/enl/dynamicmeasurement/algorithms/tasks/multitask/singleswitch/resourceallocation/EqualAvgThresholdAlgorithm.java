package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 7/30/13
 * Time: 12:15 PM
 */
public class EqualAvgThresholdAlgorithm extends MultiTaskResourceControl {
    private final int minResource;
    private final int maxResource;
    private final Random random;
    private double lowThreshold;
    private double highThreshold;
    private int resourceChangeStep;
    private List<TaskRecord2> tasks = new LinkedList<>();

    public EqualAvgThresholdAlgorithm(Element element, MonitorPoint monitorPoint) {
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        this.resourceChangeStep = Integer.parseInt(properties.get("ResourceChangeStep").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.lowThreshold = Double.parseDouble(properties.get("LowThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.highThreshold = Double.parseDouble(properties.get("HighThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.random = new Random(Long.parseLong(properties.get("Random").getAttribute(ConfigReader.PROPERTY_VALUE)));
        this.minResource = 1;
        maxResource = monitorPoint.getCapacity();
    }

    public EqualAvgThresholdAlgorithm(int resourceChangeStep, double lowThreshold, double highThreshold,
                                      int minResource, int maxResource, Random random) {
        this.resourceChangeStep = resourceChangeStep;
        this.lowThreshold = lowThreshold;
        this.highThreshold = highThreshold;
        this.minResource = minResource;
        this.maxResource = maxResource;
        this.random = random;
    }

//    public static void main(String[] args) {
//        EqualAvgThresholdAlgorithm thresholdAlgorithm = new EqualAvgThresholdAlgorithm(0.1, 0.05, 0.05, 0, 1, new Random(1324234));
//        TestTask task1 = new TestTask(4, new EWMAAccuracyAggregatorImpl(0));
//        TestTask task2 = new TestTask(4, new EWMAAccuracyAggregatorImpl(0));
//        TestTask task3 = new TestTask(2, new EWMAAccuracyAggregatorImpl(0));
//        ArrayList<SingleSwitchTask> tasks = new ArrayList<>();
//        tasks.add(task1);
//        tasks.add(task2);
//        tasks.add(task3);
//        task1.setResourceShare(1);
//        task2.setResourceShare(0);
//        task3.setResourceShare(0);
//        Random random1 = new Random(34309432);
//        for (int i = 0; i < 50; i++) {
//            System.out.print(i);
//            for (Task task : tasks) {
//                ((TestTask) task).updateAccuracyEstimate();
//                System.out.print(", " + task);
//            }
//            System.out.println();
//            thresholdAlgorithm.allocate(tasks);
//            if (i % 5 == 4) {
//                ((TestTask) task1).setCoefficient(random1.nextInt(10) + 1);
//            }
//        }
//    }

    @Override
    public void allocate() {
        double averageAccuracy = 0;
        for (TaskRecord2 task : tasks) {
            averageAccuracy += task.getTask().getAggregatedAccuracy();
        }
        averageAccuracy /= tasks.size();
        List<AllocationTaskView> toRemoveTask = new ArrayList<>();
        List<AllocationTaskView> toAddTask = new ArrayList<>();
        for (TaskRecord2 task1 : tasks) {
            AllocationTaskView task = (AllocationTaskView) task1.getTask();
            double accuracy = task.getAggregatedAccuracy();
            if (accuracy > averageAccuracy + highThreshold &&
                    task.getResourceShare() > resourceChangeStep + minResource) {
                toRemoveTask.add(task);
            } else if (accuracy < averageAccuracy + lowThreshold &&
                    task.getResourceShare() < maxResource - resourceChangeStep) {
                toAddTask.add(task);
            }
        }
        if (toRemoveTask.size() > 0 && toAddTask.size() > 0) {
            if (toRemoveTask.size() > toAddTask.size()) {
                Collections.shuffle(toRemoveTask, random);
                toRemoveTask = toRemoveTask.subList(0, toAddTask.size());
            } else if (toRemoveTask.size() < toAddTask.size()) {
                Collections.shuffle(toAddTask, random);
                toAddTask = toAddTask.subList(0, toRemoveTask.size());
            }

            for (AllocationTaskView task : toRemoveTask) {
                task.setResourceShare(task.getResourceShare() - resourceChangeStep);
            }
            for (AllocationTaskView task : toAddTask) {
                task.setResourceShare(task.getResourceShare() + resourceChangeStep);
            }
        }
    }

    @Override
    public boolean addTask(AllocationTaskView task) {
        //assume the new task accuracy is 0, take resource from highest accuracy guy,
        //if no other task give all
        if (tasks.size() == 0) {
            task.setResourceShare(1);
            return true;
        }
        //if there
        double maxAccuracy = 0;
        AllocationTaskView maxTask = null;
        for (TaskRecord2 oldTask : tasks) {
            double accuracy = oldTask.getTask().getAggregatedAccuracy();
            if (maxTask == null || maxAccuracy < accuracy) {
                maxAccuracy = accuracy;
                maxTask = oldTask.getTask();
            }
        }
        if (maxTask.getResourceShare() < minResource + resourceChangeStep) {
            return false;
        }
        tasks.add(new TaskRecord2(task, -tasks.size()));
        maxTask.setResourceShare(maxTask.getResourceShare() - resourceChangeStep);
        task.setResourceShare(resourceChangeStep);
        return true;
    }

    @Override
    public void removeTask(AllocationTaskView task) {
        //give its share to the task with minimum resource
        if (tasks.size() > 0) {
            for (Iterator<TaskRecord2> iterator = tasks.iterator(); iterator.hasNext(); ) {
                TaskRecord2 next = iterator.next();
                if (next.getTask().equals(task)) {
                    iterator.remove();
                    break;
                }
            }

            //find min accuracy task
            double minAccuracy = 0;
            AllocationTaskView minTask = null;
            for (TaskRecord2 taskRecord2 : tasks) {
                double accuracy = taskRecord2.getTask().getAggregatedAccuracy();
                if (minTask == null || minAccuracy > accuracy) {
                    minAccuracy = accuracy;
                    minTask = taskRecord2.getTask();
                }
            }
            minTask.setResourceShare(minTask.getResourceShare() + task.getResourceShare());
        }
        task.setResourceShare(0);

//        double resourceShare = task.getResourceShare() / newTasks.size();
//        for (SingleSwitchTask singleSwitchTask : newTasks.keySet()) {
//            singleSwitchTask.setResourceShare(singleSwitchTask.getResourceShare() + resourceShare);
//        }
    }

//    private static class TestTask extends SingleSwitchTask {
//        private double coefficient;
//
//        private TestTask(double coefficient, AccuracyAggregator accuracyAggregator) {
//            super("", accuracyAggregator, 1);
//            this.coefficient = coefficient;
//        }
//
//        private void setCoefficient(double coefficient) {
//            this.coefficient = coefficient;
//        }
//
//        public void updateAccuracyEstimate() {
//            setAccuracy(1 - Math.exp(-coefficient * getResourceShare()));
//        }
//
//        @Override
//        public String toString() {
//            return coefficient + "," + getResourceShare() + "," + getAccuracy();
//        }
//
//    }
}

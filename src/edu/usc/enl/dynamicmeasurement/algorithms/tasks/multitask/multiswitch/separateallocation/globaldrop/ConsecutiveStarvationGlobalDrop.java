package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.globaldrop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.SeparateMultiTaskMultiSwitchTaskHandler;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/27/13
 * Time: 6:44 AM <br/>
 * drops a task if for "dropEpochs" of consecutive epochs its accuracy is below "LowThreshold"
 * If the accuracy increases by at least 10% the task will not be penalized.
 * <p>The XML constructor requires the following Property children tags: <ul>
 * <li> name attribute as "DropEpochs" </li>
 * <li> name attribute as "LowThreshold", </li>
 * </ul></p>
 */
public class ConsecutiveStarvationGlobalDrop implements GlobalDrop {
    private final List<MultiSwitchTask> toDrop;
    private final double lowThreshold;
    public int dropEpochs;
    /**
     * to take track of the those tasks that are having consecutive low satisfaction
     */
    Map<MultiSwitchTask, Integer> notReceiving = new HashMap<>();
    /**
     * Keep track of the latest accuracy to make sure if the task increase accuracy in enough large steps
     */
    Map<MultiSwitchTask, Double> lastAccuracy = new HashMap<>();
    private SeparateMultiTaskMultiSwitchTaskHandler taskHandler;

    public ConsecutiveStarvationGlobalDrop(Element element, SeparateMultiTaskMultiSwitchTaskHandler taskHandler) throws Exception {
        toDrop = new ArrayList<>();
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        dropEpochs = Integer.parseInt(properties.get("DropEpochs").getAttribute(ConfigReader.PROPERTY_VALUE));
        lowThreshold = Double.parseDouble(properties.get("LowThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));

        this.taskHandler = taskHandler;
    }

    @Override
    public void update() {

    }

    public void globalDrop() {
        toDrop.clear();
        for (Task2 task2 : taskHandler.getAcceptedTasks()) {
            MultiSwitchTask task = (MultiSwitchTask) task2;
            double globalAccuracy = task.getGlobalAccuracy();
//            System.out.println(task.getName() + "," + globalAccuracy);
            if (!notReceiving.containsKey(task)) {
                notReceiving.put(task, 0);
            } else {
                Double lAccuracy = lastAccuracy.get(task);
                if (globalAccuracy < lowThreshold
                        && lAccuracy < lowThreshold
                        ) {//am poor
                    if (globalAccuracy < lAccuracy + 0.1) {
                        taskHandler.getLogWriter().println(task + " has small progress " + lAccuracy + " to " + globalAccuracy);
                        notReceiving.put(task, notReceiving.get(task) + 1);
                    }
                } else {
                    notReceiving.put(task, 0);
                }
            }
            lastAccuracy.put(task, globalAccuracy);
            if (notReceiving.get(task) > dropEpochs) {
                //drop the task
                toDrop.add(task);
            }
        }
        for (MultiSwitchTask task : toDrop) {
            notReceiving.remove(task);
            lastAccuracy.remove(task);
            taskHandler.drop(task);
        }
    }

    @Override
    public void doRemove(MultiSwitchTask multiSwitchTask) {
        lastAccuracy.remove(multiSwitchTask);
        notReceiving.remove(multiSwitchTask);
    }
}

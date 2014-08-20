package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.globaldrop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.aggregator.AccuracyAggregator;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.SeparateMultiTaskMultiSwitchTaskHandler;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/27/13
 * Time: 6:56 AM <br/>
 * Drops a task if is not saisficed X percent of its  lifetime, lets drop it.
 */
public class AverageSatisfactionGlobalDrop implements GlobalDrop {
    private final double lowThreshold;
    private final double satisfactionThreshold;
    private final Element satisfactionAggregatorElement;
    private Map<MultiSwitchTask, AccuracyAggregator> taskAccuracyAggregatorMap;
    private SeparateMultiTaskMultiSwitchTaskHandler taskHandler;
    private List<MultiSwitchTask> toDrop;

    public AverageSatisfactionGlobalDrop(Element element, SeparateMultiTaskMultiSwitchTaskHandler taskHandler) {
        taskAccuracyAggregatorMap = new HashMap<>();
        this.taskHandler = taskHandler;
        Map<String, Element> properties = Util.getChildrenProperties(element, "Property");
        satisfactionThreshold = Double.parseDouble(properties.get("SatisfactionThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        lowThreshold = Double.parseDouble(properties.get("LowThreshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        satisfactionAggregatorElement = properties.get("SatisfactionAggregator");
        toDrop = new ArrayList<>();
    }

    @Override
    public void doRemove(MultiSwitchTask multiSwitchTask) {
        taskAccuracyAggregatorMap.remove(multiSwitchTask);
    }

    @Override
    public void globalDrop() {
        for (Map.Entry<MultiSwitchTask, AccuracyAggregator> entry : taskAccuracyAggregatorMap.entrySet()) {
            if (entry.getValue().getAccuracy() < satisfactionThreshold) {
                taskHandler.getLogWriter().println("Drop " + entry.getKey() + " satisfaction " + entry.getValue().getAccuracy());
                toDrop.add(entry.getKey());
            }
        }
        for (MultiSwitchTask multiSwitchTask : toDrop) {
            taskHandler.drop(multiSwitchTask);
        }
    }

    @Override
    public void update() {
        for (MultiSwitchTask multiSwitchTask : taskHandler.getAcceptedTasks()) {
            AccuracyAggregator accuracyAggregator = taskAccuracyAggregatorMap.get(multiSwitchTask);
            int satisfaction = multiSwitchTask.getGlobalAccuracy() >= lowThreshold ? 1 : 0;
            if (accuracyAggregator != null) {
                accuracyAggregator.update(satisfaction);
//                System.out.println(multiSwitchTask + ":" + satisfaction);
            } else {
                try {
                    AccuracyAggregator satisfactionAggregator = (AccuracyAggregator) Class.forName(satisfactionAggregatorElement.getAttribute(ConfigReader.PROPERTY_VALUE)).
                            getConstructor(Element.class).newInstance(satisfactionAggregatorElement);
                    taskAccuracyAggregatorMap.put(multiSwitchTask, satisfactionAggregator);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

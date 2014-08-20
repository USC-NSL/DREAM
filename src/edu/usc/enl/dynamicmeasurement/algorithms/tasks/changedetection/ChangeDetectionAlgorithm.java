package edu.usc.enl.dynamicmeasurement.algorithms.tasks.changedetection;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/10/14
 * Time: 2:55 PM <br/>
 * <p>
 * <p>The XML constructor requires the following Property children tags: <ul>
 * <li> name attribute as "Filter", value attribute as a prefix pattern</li>
 * <li> name attribute as "Wildcard", value attribute as the number of wildcard bits to ignore before using the input</li>
 * <li> name attribute as "Threshold", value attribute as a positive number based on # bytes</li>
 * <li> name attribute as "Alpha", value attribute a double in [0,1] </li>
 * </ul></p>
 */
public abstract class ChangeDetectionAlgorithm implements Task2.TaskImplementation {

    protected final WildcardPattern taskWildcardPattern;
    protected final int wildcardNum;
    protected final double threshold;
    protected double ewmaAlpha = 0.8;

    protected ChangeDetectionAlgorithm(Element element) {
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        this.taskWildcardPattern = new WildcardPattern(childrenProperties.get("Filter").getAttribute(ConfigReader.PROPERTY_VALUE), 0);
        this.wildcardNum = Integer.parseInt(childrenProperties.get("WildcardNum").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.threshold = Double.parseDouble(childrenProperties.get("Threshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.ewmaAlpha = Double.parseDouble(childrenProperties.get("Alpha").getAttribute(ConfigReader.PROPERTY_VALUE));
        //Todo validation of input configuration
    }

    public WildcardPattern getTaskWildcardPattern() {
        return taskWildcardPattern;
    }

    public abstract void match(long item, double diff);

    public abstract Collection<WildcardPattern> findBigChanges(int step);

    public void reset() {

    }

    public String toString() {
        return getClass().getSimpleName();
    }

    public void finish(FinishPacket p) {

    }

    public void setFolder(String folder) {

    }

    public void update(int step) {

    }

    /**
     * The exception for when there is not enough history of an item to detect if it is a large change or not
     */
    protected static class NotEnoughDataException extends Exception {
    }
}
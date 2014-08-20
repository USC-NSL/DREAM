package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hh;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/16/13
 * Time: 8:56 AM <br/>
 * The implementation of algorithms that find heavy hitters.
 * <p>The XML constructor requires the following Property children tags: <ul>
 * <li> name attribute as "Filter", value attribute as a prefix pattern</li>
 * <li> name attribute as "Wildcard", value attribute as the number of wildcard bits to ignore before using the input</li>
 * <li> name attribute as "Threshold", value attribute as a positive number based on # bytes</li>
 * </ul></p>
 */
public abstract class HHAlgorithm implements Task2.TaskImplementation {
    protected final double threshold;
    protected final int wildcardNum;
    protected final WildcardPattern taskWildcardPattern;
    private int step;

    public HHAlgorithm(Element element) {
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        this.threshold = Double.parseDouble(childrenProperties.get("Threshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.taskWildcardPattern = new WildcardPattern(childrenProperties.get("Filter").getAttribute(ConfigReader.PROPERTY_VALUE), 0);
        this.wildcardNum = Integer.parseInt(childrenProperties.get("WildcardNum").getAttribute(ConfigReader.PROPERTY_VALUE));
        step = 0;
    }

    public HHAlgorithm(double threshold, int wildcardNum, WildcardPattern taskWildcardPattern) {
        this.threshold = threshold;
        this.wildcardNum = wildcardNum;
        this.taskWildcardPattern = taskWildcardPattern;
        step = 0;
    }

    public abstract Collection<WildcardPattern> findHH();

    public void reset() {

    }

    public void finish() {

    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void update(int step) {
        this.step = step;
    }

    public abstract void setSum(double sum);

    public double getThreshold() {
        return threshold;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public void setFolder(String folder) {

    }

    public abstract void match(long srcIP, double size);
}

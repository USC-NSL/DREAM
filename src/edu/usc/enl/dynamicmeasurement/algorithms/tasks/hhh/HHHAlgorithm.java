package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/23/13
 * Time: 10:07 PM   <br/>
 * The implementation of algorithms that find heavy hitters.
 * <p>The XML constructor requires the following Property children tags: <ul>
 * <li> name attribute as "Filter", value attribute as a prefix pattern</li>
 * <li> name attribute as "Wildcard", value attribute as the number of wildcard bits to ignore before using the input</li>
 * <li> name attribute as "Threshold", value attribute as a positive number based on # bytes</li>
 * </ul></p>
 */
public abstract class HHHAlgorithm implements Task2.TaskImplementation {
    protected final int wildcardNum;
    protected final double threshold;
    protected final WildcardPattern taskWildcardPattern;
    protected String graphFolder;
    private int step = 0;


    public HHHAlgorithm(Element element) {
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        this.threshold = Double.parseDouble(childrenProperties.get("Threshold").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.taskWildcardPattern = new WildcardPattern(childrenProperties.get("Filter").getAttribute(ConfigReader.PROPERTY_VALUE), 0);
        this.wildcardNum = Integer.parseInt(childrenProperties.get("WildcardNum").getAttribute(ConfigReader.PROPERTY_VALUE));
    }


    public HHHAlgorithm(double threshold, WildcardPattern taskWildcardPattern) {
        this.threshold = threshold;
        wildcardNum = 0;
        if (taskWildcardPattern == null) {
            taskWildcardPattern = new WildcardPattern(0, WildcardPattern.TOTAL_LENGTH, 0);
        }
        this.taskWildcardPattern = taskWildcardPattern;
    }

    public void setGraphFolder(String graphFolder) {
        this.graphFolder = graphFolder;
    }

    public final void doUpdate() {
        update();
    }

    public double getThreshold() {
        return threshold;
    }

    protected abstract void update();

    /**
     * @return hierarchical heavy hitters
     */
    public abstract Collection<WildcardPattern> findHHH();

    public void finish() {

    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setSum(double sum) {

    }

    public abstract void reset();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public void setFolder(String folder) {

    }

    public abstract void match(long srcIP, double size);
}

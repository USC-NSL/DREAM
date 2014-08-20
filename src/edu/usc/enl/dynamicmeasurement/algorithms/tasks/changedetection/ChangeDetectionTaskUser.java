package edu.usc.enl.dynamicmeasurement.algorithms.tasks.changedetection;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.TaskUser;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/10/14
 * Time: 7:13 PM <br/>
 * This class implements change detection task and uses a ChangeDetectionAlgorithm.
 * <p>
 * <p>The XML constructor requires a Property tag with name attribute as "Algorithm"
 * and with value attribute pointing to a class that implements ChangeDetectionAlgorithm.</p>
 *
 * @see ChangeDetectionAlgorithm
 */
public class ChangeDetectionTaskUser extends TaskUser {
    private ChangeDetectionAlgorithm algorithm;
    /**
     * The writer for the reports
     */
    private ControlledBufferWriter reportPrintWriter;

    public ChangeDetectionTaskUser(Element element) throws Exception {
        super(element);
        Element algorithmElement = Util.getChildrenProperties(element, "Property").get("Algorithm");
        this.algorithm = (ChangeDetectionAlgorithm) Class.forName(algorithmElement.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class).newInstance(algorithmElement);
    }

    @Override
    public void setFolder(String folder) {
        algorithm.setFolder(folder);
        try {
            if (reportPrintWriter != null) {
                reportPrintWriter.close();
            }
            reportPrintWriter = Util.getNewWriter(folder + "/hhh.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Task2.TaskImplementation getImplementation() {
        return algorithm;
    }

    @Override
    public void report(int step) {
//        Collection<WildcardPattern> bigChanges = algorithm.findBigChanges();
//        for (WildcardPattern bigChange : bigChanges) {
//            reportPrintWriter.println(step + "," + bigChange);
//        }

        Collection<WildcardPattern> bigChanges = algorithm.findBigChanges(step);
        for (WildcardPattern bigChange : bigChanges) {
            reportPrintWriter.println(step + "," + bigChange.toStringNoWeight() + "," + bigChange.getWeight());
        }
    }

    @Override
    public void update(int step) {
        algorithm.update(step);
        algorithm.reset();
    }

    @Override
    public void process2(DataPacket p) {
        algorithm.match(p.getSrcIP(), p.getSize());
    }

    @Override
    public void finish(FinishPacket p) {
        algorithm.finish(p);
        reportPrintWriter.close();
    }

}

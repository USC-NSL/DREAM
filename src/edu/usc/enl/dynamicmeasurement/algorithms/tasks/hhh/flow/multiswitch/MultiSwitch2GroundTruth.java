package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/23/13
 * Time: 2:29 PM <br/>
 * HHH algorithm that estimates accuracy using the groundtruth.
 */
public class MultiSwitch2GroundTruth extends MultiSwitch2 {
    private final String groundTruthFolder;
    private Map<Integer, Set<WildcardPattern>> groundTruth = new HashMap<>();

    public MultiSwitch2GroundTruth(Element element) {
        super(element);
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        groundTruthFolder = childrenProperties.get("GroundTruthFolder").getAttribute(ConfigReader.PROPERTY_VALUE);
    }

    @Override
    public void setFolder(String folder) {
        super.setFolder(folder);
        String name = new File(folder).getName();
        try (BufferedReader br = new BufferedReader(new FileReader(groundTruthFolder + "/" + name + "/hhh.csv"))) {
            while (br.ready()) {
                String line = br.readLine();
                String[] split = line.split(",");
                WildcardPattern hhh = new WildcardPattern(split[1], Double.parseDouble(split[2]));
                int step = Integer.parseInt(split[0]);
                Set<WildcardPattern> wildcardPatterns = groundTruth.get(step);
                if (wildcardPatterns == null) {
                    wildcardPatterns = new HashSet<>();
                    groundTruth.put(step, wildcardPatterns);
                }
                wildcardPatterns.add(hhh);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Map<WildcardPattern, Double> getHHHPrecision(Collection<WildcardPattern> hhhList, Collection<WildcardPattern> monitors1) {
        Set<WildcardPattern> wildcardPatterns = groundTruth.get(getStep());
        Map<WildcardPattern, Double> output = new HashMap<>();
        for (WildcardPattern hhh : hhhList) {
            double precision;
            if (wildcardPatterns != null && wildcardPatterns.contains(hhh)) {
                precision = 1;
            } else {
                precision = 0;
            }
            output.put(hhh, precision);
        }
        return output;
    }
}

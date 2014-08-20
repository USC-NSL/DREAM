package edu.usc.enl.dynamicmeasurement.data.trace;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.NumberAwareComparator;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/22/13
 * Time: 8:39 PM <br/>
 * Represents the information for a packet trace file. This may cover multiple tasks.
 * <p>The XML constructor requires the following Property children tags: <ul>
 * <li> name attribute as "Filter", value attribute as a prefix pattern</li>
 * <li> name attribute as "Folder0", the first folder of traces</li>
 * <li> name attribute as "Folder1", the second folder of traces</li>
 * </ul>
 * If the trace just has one folder of traces it can have a property named "Folder"</p>
 */
public class InputTrace {
    private String[] packetsFile;
    private WildcardPattern wildcardPattern;

    public InputTrace(Element e) {
        Map<String, Element> childrenProperties = Util.getChildrenProperties(e, "Property");
        wildcardPattern = new WildcardPattern(childrenProperties.get("Filter").getAttribute(ConfigReader.PROPERTY_VALUE), 0);
        String[] folders = new String[childrenProperties.size()];
        for (Map.Entry<String, Element> entry : childrenProperties.entrySet()) {
            String folder = entry.getValue().getAttribute(ConfigReader.PROPERTY_VALUE);
            if (entry.getKey().matches("Folder\\d+")) {
                folders[Integer.parseInt(entry.getKey().replaceAll("Folder", ""))] = folder;
            } else if (entry.getKey().equals("Folder")) {
                folders[0] = folder;
            }

        }

        try {
            List<File> files2 = new ArrayList<>();
            for (String folder : folders) {
                if (folder != null) {
                    File folderFile = new File(folder);
                    File[] files = folderFile.listFiles();
                    if (files == null) {
                        throw new Exception("Folder " + folder + " not found");
                    }
                    Arrays.sort(files, new NumberAwareComparator());
                    files2.addAll(Arrays.asList(files));
                }
                for (Iterator<File> iterator = files2.iterator(); iterator.hasNext(); ) {
                    File next = iterator.next();
                    if (next.getName().contains("summary")) {
                        iterator.remove();
                    }
                }
            }
            packetsFile = new String[files2.size()];
            int i = 0;
            for (File file : files2) {
                packetsFile[i++] = file.getAbsolutePath();
            }
        } catch (Exception e1) {
            System.err.println(e1.getMessage());
            System.exit(1);
        }
    }

    public WildcardPattern getWildcardPattern() {
        return wildcardPattern;
    }

    public TaskTraceReader getTaskTraceReader(WildcardPattern wildcardPattern, int taskStart, boolean cache) throws IOException {
        return new TaskTraceReader(taskStart, wildcardPattern, packetsFile, this.wildcardPattern, cache);
    }
}

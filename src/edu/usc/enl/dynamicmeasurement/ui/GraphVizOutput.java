package edu.usc.enl.dynamicmeasurement.ui;

import com.github.jabbalaci.graphviz.GraphViz;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/26/13
 * Time: 8:28 AM  <br/>
 * Make output for Graphviz based on monitored nodes and heavy hitters.
 * This is heavily used in debugging the divide and merge algorithms
 */
public class GraphVizOutput {


    public void print(Collection<WildcardPattern> monitors, String outputFile, Collection<WildcardPattern> hhh) {
        Set<WildcardPattern> output = gatherNodes(monitors);
        GraphViz gv = printGraph(monitors, hhh, output);
        File out = new File(outputFile);
        out.getParentFile().mkdirs();
//        System.out.println(gv.getDotSource());
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), "jpg"), out);

    }

    private GraphViz printGraph(Collection<WildcardPattern> monitors, Collection<WildcardPattern> hhh, Set<WildcardPattern> output) {
        List<WildcardPattern> sortedQ = new ArrayList<>(output);
        Collections.sort(sortedQ);
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());
        gv.addln("graph [ordering=\"out\"];");
        printNodes(monitors, hhh, sortedQ, gv);
        gv.addln(gv.end_graph());
        return gv;
    }

    private void printNodes(Collection<WildcardPattern> monitors, Collection<WildcardPattern> hhh, List<WildcardPattern> sortedQ, GraphViz gv) {
        for (WildcardPattern wildcardPattern : sortedQ) {
            printNode(wildcardPattern, monitors.contains(wildcardPattern), hhh != null && hhh.contains(wildcardPattern), gv);
            if (wildcardPattern.canGoUp()) {
                gv.addln("n" + wildcardPattern.clone().goUp().toStringNoWeight() + " -> n" + wildcardPattern.toStringNoWeight() +
                        "[label=\"" + String.format("%.1f", wildcardPattern.getWeight() / 1000) + "\"];");
            } else {
                gv.addln("{rank=source; n" + wildcardPattern.toStringNoWeight() + "}");
            }
        }
    }

    private Set<WildcardPattern> gatherNodes(Collection<WildcardPattern> monitors) {
        Set<WildcardPattern> output = new HashSet<>();
        LinkedList<WildcardPattern> q = new LinkedList<>();
        q.addAll(monitors);

        while (q.size() > 0) {
            WildcardPattern wildcardPattern = q.poll();

            output.add(wildcardPattern);
            if (wildcardPattern.canGoUp()) {
                WildcardPattern parent = wildcardPattern.clone().goUp();
                if (!monitors.contains(parent)) {
                    parent.setWeight(0);
                }
                if (!output.contains(parent)) {
                    q.add(parent);
                }
            }
        }
        return output;
    }

    private void printNode(WildcardPattern wildcardPattern, boolean isOriginal, boolean ishhh, GraphViz gv) {
        gv.addln("n" + wildcardPattern.toStringNoWeight() + "[label=\"" + wildcardPattern.getLabel() +
                "\" " + (isOriginal ? " style=filled" : "") + (ishhh ? " fontcolor=red" : "") + "];");
    }
}

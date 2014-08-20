package edu.usc.enl.dynamicmeasurement.process.scripts;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/10/13
 * Time: 5:36 PM
 */
public class TrimmedTrafficReport2 extends TrafficReport {
    public TrimmedTrafficReport2(boolean resetOnStep, Set<WildcardPattern> wildcards, String outputFile, int maxWildcards) throws IOException {
        super(resetOnStep, trim(wildcards, maxWildcards), outputFile);
    }

    private static Set<WildcardPattern> trim(Set<WildcardPattern> wildcards, int levels) {
        for (Iterator<WildcardPattern> iterator = wildcards.iterator(); iterator.hasNext(); ) {
            WildcardPattern wildcard = iterator.next();
            if (wildcard.getWildcardNum() < levels) {
                iterator.remove();
            }
        }
        return wildcards;
    }

}

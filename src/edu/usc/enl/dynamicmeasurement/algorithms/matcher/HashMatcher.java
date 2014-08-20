package edu.usc.enl.dynamicmeasurement.algorithms.matcher;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 3/5/13
 * Time: 5:34 PM <br/>
 * The hash based version of matcher that keeps a hashmap of wildcardpatterns for each prefix length.
 * This is usually faster than linear implementation
 */
public class HashMatcher extends Matcher {
    private List<Map<Long, WildcardPattern>> data;

    public HashMatcher() {
        data = new ArrayList<>(33);
        for (int i = 0; i < 33; i++) {
            data.add(new HashMap<Long, WildcardPattern>());
        }
    }

    @Override
    public void setMonitors(Collection<WildcardPattern> monitors) {
        for (Map<Long, WildcardPattern> d : data) {
            d.clear();
        }
        for (WildcardPattern monitor : monitors) {
            Map<Long, WildcardPattern> sameLengthPrefix = data.get(monitor.getWildcardNum());
            sameLengthPrefix.put(monitor.getData(), monitor);
        }
    }

    public WildcardPattern match(long item) {
        for (Map<Long, WildcardPattern> hashMap : data) {
            WildcardPattern wildcardPattern = hashMap.get(item);
            if (wildcardPattern != null) {
                return wildcardPattern;
            }
            item >>>= 1;
        }
        return null;
    }
}

package edu.usc.enl.dynamicmeasurement.util;

import java.math.BigInteger;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/24/13
 * Time: 12:19 AM <br/> Can compare objects recognizing a hierarchy of numbers in their toString
 */
public class NumberAwareComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        String o1s = o1.toString();
        String o2s = o2.toString();
        if (o1s.replaceAll("\\d", "").equals(o2s.replaceAll("\\d", ""))) { //to handle integer
            return new BigInteger(o1s.replaceAll("\\D", "")).compareTo(new BigInteger(o2s.replaceAll("\\D", "")));
        }
        return o1s.compareTo(o2s);
    }
}

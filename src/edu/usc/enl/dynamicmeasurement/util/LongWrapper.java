package edu.usc.enl.dynamicmeasurement.util;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/8/13
 * Time: 5:31 PM
 */
public class LongWrapper {
    private long value;

    public LongWrapper(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongWrapper that = (LongWrapper) o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }
}

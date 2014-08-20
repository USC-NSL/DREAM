package edu.usc.enl.dynamicmeasurement.util;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/26/13
 * Time: 10:47 AM
 */
public class IntegerWrapper {
    private int value = 0;

    public IntegerWrapper(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value + "";
    }

    public void add(int a) {
        this.value += a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntegerWrapper that = (IntegerWrapper) o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value;
    }
}

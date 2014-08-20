package edu.usc.enl.dynamicmeasurement.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/13/13
 * Time: 6:53 PM <Br/>
 * A printwriter without any overhead or output
 */
public class VoidPrintWriter extends PrintWriter {
    public VoidPrintWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    @Override
    public void println() {
    }

    @Override
    public void println(boolean x) {
    }

    @Override
    public void println(char x) {
    }

    @Override
    public void println(int x) {
    }

    @Override
    public void println(long x) {
    }

    @Override
    public void println(float x) {
    }

    @Override
    public void println(double x) {
    }

    @Override
    public void println(char[] x) {
    }

    @Override
    public void println(String x) {
    }

    @Override
    public void println(Object x) {
    }
}

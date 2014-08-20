package edu.usc.enl.dynamicmeasurement.util;

import java.io.FileNotFoundException;
import java.io.Flushable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 11/18/13
 * Time: 1:32 PM  <br/>
 * A kind of writer that only writes into output if told so.
 * This is an essential feature as we don't want to add any disk delay to the control loop
 */
public class ControlledBufferWriter implements AutoCloseable, Flushable {
    private PrintWriter pw;
    private List<String> buffer;
    private boolean currentIsFull = false;
    private boolean close = false;
    private boolean dummy;

    ControlledBufferWriter(String fileName, boolean dummy) throws FileNotFoundException {
        if (!dummy) {
            pw = new PrintWriter(fileName);
            buffer = new ArrayList<>();
        }
        this.dummy = dummy;
    }

    public void println() {
        println("");
    }

    public void println(int s) {
        println(String.valueOf(s));
    }

    public void println(double s) {
        println(String.valueOf(s));
    }

    public synchronized void println(String s) {
        if (dummy) {
            return;
        }
        if (currentIsFull) {
            buffer.set(buffer.size() - 1, buffer.get(buffer.size() - 1) + s);
            currentIsFull = false;
        } else {
            buffer.add(s);
        }
    }

    /**
     * flush the buffer into the output.
     * Just to be safe, the name of the method has been changed
     */
    public synchronized void flush2() {
        if (dummy) {
            return;
        }
        for (String s : buffer) {
            pw.println(s);
        }
        buffer.clear();
    }

    @Override
    public void close() {
        if (dummy) {
            return;
        }
        flush2();
        pw.close();
        close = true;
    }

    @Override
    public void flush() {
        // even if the user of this wants to flush, don't do this
    }

    public synchronized void print(String s) {
        if (dummy) {
            return;
        }
        if (currentIsFull) {
            buffer.set(buffer.size() - 1, buffer.get(buffer.size() - 1) + s);
        } else {
            buffer.add(s);
        }
        currentIsFull = true;
    }

    public boolean isClose() {
        return close;
    }
}

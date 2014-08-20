package edu.usc.enl.dynamicmeasurement.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/14/13
 * Time: 10:15 AM  <br/>
 * The trace reader that can read multiple trace files.
 * It is like concatenating those trace files
 */
public class MultiFileTraceReader implements PacketLoader.TraceReader {
    private final String[] files;
    private int currentFileIndex = 0;
    private BufferedReader currentReader;

    public MultiFileTraceReader(String[] files) throws FileNotFoundException {
        this.files = files;
        if (files.length > 0) {
            currentFileIndex = 0;
            currentReader = new BufferedReader(new FileReader(files[0]));
        }
    }

    @Override
    public void close() throws IOException {
        if (currentReader != null) {
            currentReader.close();
        }
    }

    @Override
    public String readLine() throws IOException {
        if (currentReader == null) {
            return null;
        }
        String out = currentReader.readLine();
        if (out != null) {
            return out;
        }
        //check next file
        if (currentFileIndex < files.length - 1) {
            currentFileIndex++;
            currentReader.close();
            currentReader = new BufferedReader(new FileReader(files[currentFileIndex]));
            return readLine();
        }

        return null;
    }

    public String[] getFiles() {
        return files;
    }
}

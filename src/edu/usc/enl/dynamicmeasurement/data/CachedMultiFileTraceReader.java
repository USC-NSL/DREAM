package edu.usc.enl.dynamicmeasurement.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/12/13
 * Time: 5:30 PM <br/>
 * This is a kind of tracereader that reads files but also caches the data to not re-read them
 */
public class CachedMultiFileTraceReader implements PacketLoader.TraceReader {
    private MultiFileTraceReader reader;
    private List<String> cache = new LinkedList<>();
    private Iterator<String> iterator;

    public CachedMultiFileTraceReader(MultiFileTraceReader reader) throws IOException {
        this.reader = reader;
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            cache.add(line);
        }
        iterator = cache.iterator();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public String readLine() throws IOException {
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public void reset() {
        iterator = cache.iterator();
    }
}

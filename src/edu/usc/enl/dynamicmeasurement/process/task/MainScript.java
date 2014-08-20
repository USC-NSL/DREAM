package edu.usc.enl.dynamicmeasurement.process.task;

import edu.usc.enl.dynamicmeasurement.algorithms.transform.EpochPacer;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.TransformHandler;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.TransformHandlerInterface;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.FileListParser;
import edu.usc.enl.dynamicmeasurement.data.MultiFileTraceReader;
import edu.usc.enl.dynamicmeasurement.data.PacketLoader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.event.*;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.process.Simulator;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.apache.commons.cli.Options;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 12:07 AM <br/>
 * A very simple configuration runner that uses Simulator class
 */
public class MainScript {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        for (String arg : args) {
            System.out.println(arg);
        }

        WildcardPattern.TOTAL_LENGTH = 32;
        for (String filename : args) {
            run(filename);
        }
    }

    private static void run(String filename) throws Exception {
        File file = new File(filename);
        if (file.isDirectory()) {
            filename = file.getAbsolutePath() + "/config.xml";
        }
        ConfigReader configReader = new ConfigReader();
        configReader.read(filename);


        // get end user //this should be able to add/remove tasks
        PacketUser user = configReader.getTaskHandler();
        // wrap it by a user for traffic transform scenarios handler
        user = new TransformHandler(user);
        //if event needs task handler set task handler
        //if event needs transform handler set transform handler
        // set event runner task handler and traffic transformer handler to allow events to run
        for (Event event : configReader.getEvents()) {
            if (event instanceof TaskEvent) {
                ((TaskEvent) event).setHandler(configReader.getTaskHandler());
            } else if (event instanceof TransformEvent) {
                ((TransformEvent) event).setHandler((TransformHandlerInterface) user);
            } else if (event instanceof AddTrafficEvent) {
                throw new RuntimeException("This does not support trace files");
            }
        }

        // wrap it by event runner
        user = new EventRunner(user, configReader.getEvents());
        // wrap it by epoch pacer
        user = new EpochPacer(user, Util.getSimulationConfiguration().getEpoch());

        //create loader
        FileListParser fileListParser = new FileListParser();
        try {
            fileListParser.parse(Util.getSimulationConfiguration().get("Input"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        PacketLoader loader = new PacketLoader(user.getQueue(), new MultiFileTraceReader(fileListParser.getPacketsFile()), 0);
        //pass loader and user to the simulation
        new Simulator(0).run(user, loader);
    }
}

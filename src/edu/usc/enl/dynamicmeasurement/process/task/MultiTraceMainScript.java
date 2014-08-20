package edu.usc.enl.dynamicmeasurement.process.task;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.data.trace.FilterTraceMapping;
import edu.usc.enl.dynamicmeasurement.model.event.*;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.SimulationConfiguration;
import edu.usc.enl.dynamicmeasurement.util.Util;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 12:07 AM   <br/>
 * The main script for running multiple configurations.
 * This can be given the address/addresses of config files or folder that contains config files with name config.xml.
 * If the first argument is a number it will be used as the maximum number of concurrent threads to be used.
 */
public class MultiTraceMainScript {
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
        try {
            SimulationConfiguration.threadsNum = Integer.parseInt(args[0]);
            for (int i = 1; i < args.length; i++) {
                try {
                    run(args[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (NumberFormatException e) {
            // if the first argument is not a number
            for (String filename : args) {
                try {
                    run(filename);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }


    }

    private static void run(String filename) throws Exception {
        //read the configuration file
        File file = new File(filename);
        if (file.isDirectory()) {
            filename = file.getAbsolutePath() + "/config.xml";
        }
        ConfigReader configReader = new ConfigReader();
        configReader.read(filename);

        FilterTraceMapping filterTraceMapping = new FilterTraceMapping();

        SimulationConfiguration simulationConfiguration = Util.getSimulationConfiguration();
        int epochSize = (int) simulationConfiguration.getEpoch();
        // set event runner task handler and traffic transformer handler to allow events to run
        TaskHandler taskHandler = configReader.getTaskHandler();
        for (Event event : configReader.getEvents()) {
            event.setEpoch(event.getEpoch() / epochSize);

            if (event instanceof TaskEvent) {
                ((TaskEvent) event).setHandler(taskHandler);
                if (event instanceof AddTraceTaskEvent) {
                    ((AddTraceTaskEvent) event).setMapping(filterTraceMapping);
                }
            } else if (event instanceof TransformEvent) {
                ((TransformEvent) event).setHandler(taskHandler);
            } else if (event instanceof AddTrafficEvent) {
                ((AddTrafficEvent) event).setHandler(filterTraceMapping);
            }
        }

        taskHandler.createProfiler();

        // wrap it by an event runner
        EventRunner eventRunner = new EventRunner(taskHandler, configReader.getEvents());


        //run events
        int step = 0;
        long startTime = simulationConfiguration.getStartTime();
        while (!eventRunner.isEmpty()) {
            eventRunner.forceStep(new EpochPacket(step * epochSize + startTime, step++));

            //write reports
            taskHandler.writeLog(step);
//            System.gc();
            taskHandler.writeProfiles();
            Util.flushAllControlledWriters();
        }

        //finish
        taskHandler.finishProfiler();
        taskHandler.finish(new FinishPacket(step * epochSize + startTime));
    }
}

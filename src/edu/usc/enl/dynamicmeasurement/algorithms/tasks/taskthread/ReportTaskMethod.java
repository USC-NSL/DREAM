package edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/21/13
 * Time: 7:42 AM  <br/>
 * Asks the task to report to the user
 */
public class ReportTaskMethod implements Runnable {
    private Task2 task;

    public ReportTaskMethod(Task2 task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.report();
    }

    @Override
    public String toString() {
        return "Report " + task;
    }
}

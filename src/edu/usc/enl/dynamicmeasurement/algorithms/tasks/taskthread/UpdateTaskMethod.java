package edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/21/13
 * Time: 7:42 AM <br/>
 * Asks the task to update whatever internal data structure it has
 */
public class UpdateTaskMethod implements Runnable {
    private Task2 task;

    public UpdateTaskMethod(Task2 task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.update();
    }

    @Override
    public String toString() {
        return "Update " + task;
    }
}

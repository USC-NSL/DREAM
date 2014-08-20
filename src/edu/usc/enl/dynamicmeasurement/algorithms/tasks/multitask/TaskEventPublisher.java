package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.singleswitch.resourceallocation.MultiTaskResourceControl;

import java.util.Observable;
import java.util.Observer;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/30/13
 * Time: 3:35 PM <br/>
 * It was used as a broadcaster of events for tasks, especially if they can be dropped independently from
 * each of the per-switch resource allocators to inform others and task handler.
 * This is not used in the final design
 */
public class TaskEventPublisher extends Observable {
    public static enum EventType {Add, Drop}

    public void subscribe(Observer observer) {
        this.addObserver(observer);
    }

    public void publish(Task2 task, EventType event, MultiTaskResourceControl resourceControl) {
        this.setChanged();
        System.out.println("Event: " + event + ": " + task);
        this.notifyObservers(new EventWrapper(task, event, resourceControl));
    }

    public static class EventWrapper {
        private final Task2 task;
        private final EventType event;
        private final MultiTaskResourceControl resourceControl;

        public EventWrapper(Task2 task, EventType event, MultiTaskResourceControl resourceControl) {
            this.task = task;
            this.event = event;
            this.resourceControl = resourceControl;
        }

        public MultiTaskResourceControl getResourceControl() {
            return resourceControl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EventWrapper that = (EventWrapper) o;

            if (event != that.event) return false;
            if (task != null ? !task.equals(that.task) : that.task != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = task != null ? task.hashCode() : 0;
            result = 31 * result + (event != null ? event.hashCode() : 0);
            return result;
        }

        public Task2 getTask() {
            return task;
        }

        public EventType getEvent() {
            return event;
        }
    }
}

package edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.globaldrop;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.MultiSwitchTask;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.multitask.multiswitch.separateallocation.SeparateMultiTaskMultiSwitchTaskHandler;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 12/27/13
 * Time: 6:55 AM
 */
public class NoGlobalDrop implements GlobalDrop {
    public NoGlobalDrop(Element e, SeparateMultiTaskMultiSwitchTaskHandler taskHandler) {
    }

    @Override
    public void doRemove(MultiSwitchTask multiSwitchTask) {

    }

    @Override
    public void globalDrop() {

    }

    @Override
    public void update() {

    }
}

package edu.usc.enl.dynamicmeasurement.util.profile;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 11/5/13
 * Time: 8:40 PM <br/>
 * If you want to profile the delay of the methods in a class, implement this method
 */
public interface Profilable {
    /**
     * This signals the profilable to write its numbers to the log. The profilable must not write any profile data
     * outside of this method. The motivation is to not add any (disk) delay during the control loop
     */
    public void writeProfiles();

    /**
     * creates a profiler and may create the log file
     */
    public void createProfiler();

    /**
     * close any log file openned
     */
    public void finishProfiler();
}

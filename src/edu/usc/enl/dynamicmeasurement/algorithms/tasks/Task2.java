package edu.usc.enl.dynamicmeasurement.algorithms.tasks;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread.LoadTrafficTaskMethod;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread.ReportTaskMethod;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.taskthread.UpdateTaskMethod;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.TrafficTransformer;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.data.FinishPacket;
import edu.usc.enl.dynamicmeasurement.data.trace.TaskTraceReaderInterface;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.ControlledBufferWriter;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 9:58 AM <br/>
 * Represents the tasks in the system
 */
public abstract class Task2 {
    /**
     * Keep it as only numbers for simplicity of analysis scripts. The name should be unique among tasks
     */
    protected final String name;
    /**
     * will only process the packets that match this filter
     */
    protected final WildcardPattern filter;
    /**
     * The one that knows how to use packets
     */
    protected final TaskUser user;
    /**
     * any output created by this task must be written in this folder
     */
    protected final String outputFolder;
    /**
     * keeps the configuration of this task
     */
    protected Element element;
    /**
     * Just log start once only in the first step.
     */
    boolean wroteStart = false;
    /**
     * Management logs for this task
     */
    private ControlledBufferWriter logWriter;
    private int step;
    private UpdateTaskMethod updateTaskMethod;
    private ReportTaskMethod reportTaskMethod;
    private LoadTrafficTaskMethod processTaskMethod;
    private TaskTraceReaderInterface traceReader;

    public Task2(Element e) throws Exception {
        this.element = e;
        name = e.getAttribute(ConfigReader.PROPERTY_NAME);
        Map<String, Element> childrenProperties = Util.getChildrenProperties(e, "Property");
        filter = new WildcardPattern(childrenProperties.get("Filter").getAttribute(ConfigReader.PROPERTY_VALUE), 0);
        outputFolder = Util.getRootFolder() + "/" + name;
        new File(outputFolder).mkdirs();
        //instantiate user
        Element userElement = childrenProperties.get("User");
        user = (TaskUser) Class.forName(userElement.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class).newInstance(userElement);
        user.setFolder(outputFolder);
        logWriter = Util.getNewWriter(outputFolder + "/task.log");

        reportTaskMethod = new ReportTaskMethod(this);
        updateTaskMethod = new UpdateTaskMethod(this);
    }

    public WildcardPattern getFilter() {
        return filter;
    }

    public void addTransform(TrafficTransformer transformer) {
        processTaskMethod.addTransform(transformer);
    }

    public void removeTransform(String name) {
        processTaskMethod.removeTransform(name);
    }

    public TaskTraceReaderInterface getTraceReader() {
        return traceReader;
    }

    public void setTraceReader(TaskTraceReaderInterface traceReader) {
        this.traceReader = traceReader;
        processTaskMethod = new LoadTrafficTaskMethod(this);
    }

    public abstract void updateStats();

    public void setStep(int step) {
        this.step = step;
        if (!wroteStart) {
            logWriter.println("Start," + step);
            wroteStart = true;
        }
    }

    public void process(DataPacket p) {
        if (filter.match(p.getSrcIP())) {
            synchronized (this) {
                filter.setWeight(filter.getWeight() + p.getSize());
                user.process2(p);
            }
        }
        if (!wroteStart) {
            logWriter.println("Start," + step);
            wroteStart = true;
        }
    }

    public void drop() {
        logWriter.println("Drop," + step);
        cleanup(null);
    }

    public void finish(FinishPacket p) {
//        System.out.println(filter);
        logWriter.println("Finish," + step);
        cleanup(p);
    }

    private void cleanup(FinishPacket p) {
        user.finish(p);
        writeElement();
        logWriter.close();
    }

    protected void writeElement() {
        try {
//            Element e = (Element) element.cloneNode(true);
//            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//            Document doc = docBuilder.newDocument();
//            e = (Element) doc.importNode(e, true);
//            {
//                Element property = doc.createElement("Property");
//                property.setAttribute(ConfigReader.PROPERTY_NAME, "Start");
//                property.setAttribute("value", start + "");
//                e.appendChild(property);
//                property = doc.createElement("Property");
//                property.setAttribute(ConfigReader.PROPERTY_NAME, "Finish");
//                property.setAttribute("value", taskHandler.getStep() + "");
//                e.appendChild(property);
//            }
            ConfigReader.writeElement(element, outputFolder + "/config.xml");
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task2 task = (Task2) o;

        if (name != null ? !name.equals(task.name) : task.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String getName() {
        return name;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void report() {
        report(step);
    }

    /**
     * report the output to the enduser.
     *
     * @param step
     */
    public void report(int step) {
        this.step = step;
        user.report(step);
    }

    public UpdateTaskMethod getUpdateTaskMethod() {
        return updateTaskMethod;
    }

    public void setUpdateTaskMethod(UpdateTaskMethod updateTaskMethod) {
        this.updateTaskMethod = updateTaskMethod;
    }

    public ReportTaskMethod getReportTaskMethod() {
        return reportTaskMethod;
    }

    public void setReportTaskMethod(ReportTaskMethod reportTaskMethod) {
        this.reportTaskMethod = reportTaskMethod;
    }

    public void update() {
//        System.out.println("Su " + getName());
        update(step);
    }

    /**
     * update any internal data structure e.g., reconfigure counters
     *
     * @param step
     */
    public void update(int step) {
        this.step = step;
        user.update(step);
    }

    public TaskUser getUser() {
        return user;
    }

    public LoadTrafficTaskMethod getProcessTaskMethod() {
        return processTaskMethod;
    }


    public interface TaskImplementation {

    }
}

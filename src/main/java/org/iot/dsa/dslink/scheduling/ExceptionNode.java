package org.iot.dsa.dslink.scheduling;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class ExceptionNode extends ExceptionEventNode implements EventCollection {
        
    public ExceptionNode() {
        super();
    }
    
    public ExceptionNode(ScheduleNode parent, DSMap parameters) {
        super(parent, null, parameters);
    }
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Add Event", makeAddEventAction());
    }

    private DSAction makeAddEventAction() {
        DSAction act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation request) {
                ((ExceptionNode) target.get()).addEvent(request.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSValueType.STRING, null);
        act.addParameter("Value", DSValueType.STRING, null);
        act.addParameter("Start Time", DSValueType.STRING, null);
        act.addParameter("End Time", DSValueType.STRING, null);
        return act;
    }
    
    private void addEvent(DSMap parameters) {
        String name = parameters.getString("Name");
        put(name, new ExceptionEventNode(getScheduleNode(), this, parameters));
    }
    
    public LocalDate getDate() {
        String str = parameters.getString("Date");
        return LocalDate.parse(str);
    }
    
    public int getDays() {
        return parameters.getInt("Duration");
    }
    
    protected ScheduleNode getScheduleNode() {
        return (ScheduleNode) getParent();
    }

    @Override
    public Collection<EventNode> getEvents() {
        Set<EventNode> events = new HashSet<EventNode>();
        events.add(this);
        for (DSInfo info: this) {
            DSIObject obj = info.get();
            if (obj instanceof EventNode) {
                EventNode event = (EventNode) obj;
                events.add(event);
            }
        }
        return events;
    }
    
    @Override
    public DSElement getValue() {
        return getScheduleNode().getFallbackValue();
    }
    
    @Override
    public Integer priority() {
        return 2;
    }
    
    @Override
    public LocalTime getStartTime() {
        return LocalTime.MIDNIGHT;
    }
    
    @Override
    public LocalTime getEndTime() {
        return LocalTime.MIDNIGHT;
    }
    
    @Override
    protected ExceptionNode getExceptionNode() {
        return this;
    }
    
    @Override
    public boolean isEnabled() {
        return parameters.getBoolean("Override Whole Day");
    }
    

}

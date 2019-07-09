package org.iot.dsa.dslink.scheduling;

import java.util.PriorityQueue;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.DSRuntime.Timer;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

public class ScheduleNode extends DSNode implements Runnable {
    
    private PriorityQueue<TimestampedEvent> eventQueue = new PriorityQueue<TimestampedEvent>();
    private PriorityQueue<EventNode> activeEvents = new PriorityQueue<EventNode>();
    private Timer nextUpdate;
//    private long nextUpdateTime;
    private DSInfo valueInfo = getInfo("Value");
    private DSInfo fallbackInfo = getInfo("Fallback Value");
    
    public ScheduleNode() {
        
    }
    
    public ScheduleNode(DSValueType type) {
        
    }
    
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Fallback Value", DSString.valueOf("null"));
        declareDefault("Value", DSString.valueOf("null")).setReadOnly(true);
        declareDefault("Add Weekly Event", makeAddWeeklyEventAction());
        declareDefault("Add Exception", makeAddExceptionAction());
    }

    private DSAction makeAddWeeklyEventAction() {
        DSAction act = new DSAction.Parameterless() {
            
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation request) {
                ((ScheduleNode) target.get()).addWeeklyEvent(request.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSValueType.STRING, null);
        act.addParameter("Value", DSValueType.STRING, null);
        act.addParameter("Start Time", DSValueType.STRING, null);
        act.addParameter("End Time", DSValueType.STRING, null);
        act.addParameter("Monday", DSValueType.BOOL, null);
        act.addParameter("Tuesday", DSValueType.BOOL, null);
        act.addParameter("Wednesday", DSValueType.BOOL, null);
        act.addParameter("Thursday", DSValueType.BOOL, null);
        act.addParameter("Friday", DSValueType.BOOL, null);
        act.addParameter("Saturday", DSValueType.BOOL, null);
        act.addParameter("Sunday", DSValueType.BOOL, null);
        return act;
    }
    
    private void addWeeklyEvent(DSMap parameters) {
        String name = parameters.getString("Name");
        put(name, new WeeklyEventNode(this, parameters));
    }
    
    private DSAction makeAddExceptionAction() {
        DSAction act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation request) {
                ((ScheduleNode) target.get()).addException(request.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSValueType.STRING, null);
        act.addParameter("Date", DSValueType.STRING, null);
        act.addDefaultParameter("Duration", DSInt.valueOf(1), "How many days the exception is active for");
        act.addParameter("Override Whole Day", DSValueType.BOOL, null);
        return act;
    }
    
    private void addException(DSMap parameters) {
        String name = parameters.getString("Name");
        put(name, new ExceptionNode(this, parameters));
    }
    
    public void registerEvent(EventNode event) {
        insertIntoQueue(event);
        if (event.isActive()) {
            markEventActive(event);
        }
        refresh();
    }
    
    private void insertIntoQueue(EventNode event) {
        if (event.isEnabled()) {
            long nextChange = event.nextChange();
            info("next change for " + event.getValue() + " at " + nextChange);
            if (nextChange > 0) {
                eventQueue.add(new TimestampedEvent(nextChange, event));
            }
        }
    }
    
    private void markEventActive(EventNode event) {
        if (!activeEvents.contains(event)) {
            activeEvents.add(event);
        }
    }
    
    @Override
    protected void onStable() {
        super.onStable();
        init();
    }
    
    @Override
    protected void onChildChanged(DSInfo info) {
        super.onChildChanged(info);
        if (info == fallbackInfo && activeEvents.isEmpty()) {
            updateValue(getFallbackValue());
        }
    }
    
    @Override
    protected void onChildRemoved(DSInfo info) {
        super.onChildRemoved(info);
        DSIObject obj = info.get();
        if (obj instanceof EventNode) {
            EventNode event = (EventNode) obj;
            event.disable();
            activeEvents.remove(event);
            refresh();
        }
    }
    
    private void cancelNextUpdate() {
        if (nextUpdate != null) {
            nextUpdate.cancel();
            nextUpdate = null;
        }
    }
    
    private void refresh() {
        cancelNextUpdate();
        DSRuntime.run(this);
    }
    
    private void init() {
        for (DSInfo info: this) {
            DSIObject obj = info.get();
            if (obj instanceof EventCollection) {
                EventCollection events = (EventCollection) obj;
                for (EventNode event: events.getEvents()) {
                    insertIntoQueue(event);
                    if (event.isActive()) {
                        markEventActive(event);
                    }
                }
            }
        }
        refresh();
    }
    
    private void updateValue(DSElement newValue) {
        put(valueInfo, newValue);
    }
    
    public DSElement getFallbackValue() {
        return fallbackInfo.getElement();
    }

    @Override
    public void run() {
        info("Running");
        TimestampedEvent nextEvent = eventQueue.peek();
        while (nextEvent != null && nextEvent.getTs() <= System.currentTimeMillis()) {
            EventNode eventNode = nextEvent.getEvent();
            if (eventNode.isActive()) {
                markEventActive(eventNode);
            } else {
                activeEvents.remove(eventNode);
            }
            eventQueue.remove();
            insertIntoQueue(eventNode);
            nextEvent = eventQueue.peek();
        }
        EventNode activeEvent = activeEvents.peek();
        if (activeEvent != null) {
            updateValue(activeEvent.getValue());
        } else {
            updateValue(getFallbackValue());
        }
        
        nextEvent = eventQueue.peek();
        if (nextEvent != null) {
            nextUpdate = DSRuntime.runAt(this, nextEvent.getTs());
        }
    }
    
    

}

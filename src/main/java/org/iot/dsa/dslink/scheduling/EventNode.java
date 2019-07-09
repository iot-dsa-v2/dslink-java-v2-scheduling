package org.iot.dsa.dslink.scheduling;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSNode;

public abstract class EventNode extends DSNode implements Comparable<EventNode> {
    
    private ScheduleNode schedule;
    private boolean enabled = true;
    
    public EventNode() {
        
    }
    
    public EventNode(ScheduleNode parent) {
        this.schedule = parent;
    }
    
    @Override
    protected void onStable() {
        super.onStable();
        if (schedule != null) {
            schedule.registerEvent(this);
        }
    }
    
    @Override
    protected void onStarted() {
        super.onStarted();
        
    }
    
    public void enable() {
        enabled = true;
    }
    
    public void disable() {
        enabled = false;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isActive() {
        return isEnabled() && isActiveTime();
    }
    
    protected abstract boolean isActiveTime();
    
    public abstract DSElement getValue();
    
    public abstract long nextChange();
    
    public abstract Integer priority();

    @Override
    public int compareTo(EventNode o) {
        return priority().compareTo(o.priority());
    }
}

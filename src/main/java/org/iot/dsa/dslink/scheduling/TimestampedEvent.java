package org.iot.dsa.dslink.scheduling;

public class TimestampedEvent implements Comparable<TimestampedEvent> {
    private final Long ts;
    private final EventNode event;
    
    public TimestampedEvent(long ts, EventNode event) {
        this.ts = ts;
        this.event = event;
    }
    
    public Long getTs() {
        return ts;
    }
    
    public EventNode getEvent() {
        return event;
    }

    @Override
    public int compareTo(TimestampedEvent o) {
        return ts.compareTo(o.getTs());
    }
    

}

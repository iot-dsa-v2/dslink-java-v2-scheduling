package org.iot.dsa.dslink.scheduling;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSMap;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class ExceptionEventNode extends EventNode {
    
    protected DSMap parameters;
    private ExceptionNode parent;
    
    public ExceptionEventNode() {
        super();
    }
    
    public ExceptionEventNode(ScheduleNode schedule, ExceptionNode parent, DSMap parameters) {
        super(schedule);
        this.parent = parent;
        this.parameters = parameters;
    }
    
    @Override
    protected void onStarted() {
        super.onStarted();
        DSIObject obj = get("Parameters");
        if (obj instanceof DSMap) {
            parameters = (DSMap) obj;
        } else {
            put("Parameters", parameters.copy());
        }
    }

    @Override
    protected boolean isActiveTime() {
        DateTime nowDt = DateTime.now();
        LocalDate date = nowDt.toLocalDate();
        LocalDate startDate = getExceptionNode().getDate();
        LocalDate endDate = startDate.plusDays(getExceptionNode().getDays());
        LocalTime now = nowDt.toLocalTime();
        LocalTime start = getStartTime();
        LocalTime end = getEndTime();
        return !date.isBefore(startDate) && date.isBefore(endDate) && !now.isBefore(start) && now.isBefore(end);
    }

    @Override
    public DSElement getValue() {
        return parameters.get("Value");
    }

    @Override
    public long nextChange() {
        DateTime nowDt = DateTime.now();
        LocalDate date = nowDt.toLocalDate();
        LocalDate startDate = getExceptionNode().getDate();
        LocalDate endDate = startDate.plusDays(getExceptionNode().getDays());
        
        if (date.isBefore(endDate)) {
            if (date.isBefore(startDate)) {
                return startDate.toDateTime(getStartTime()).getMillis();
            } else {
                LocalTime now = nowDt.toLocalTime();
                LocalTime start = getStartTime();
                LocalTime end = getEndTime();
                if (now.isBefore(end)) {
                    if (now.isBefore(start)) {
                        return date.toDateTime(start).getMillis();
                    } else {
                        return date.toDateTime(end).getMillis();
                    }
                } else if (date.plusDays(1).isBefore(endDate)) {
                    return date.plusDays(1).toDateTime(start).getMillis();
                } else {
                    return -1;
                }
            }
        } else {
            return -1;
        }
    }

    @Override
    public Integer priority() {
        return 1;
    }
    
    public LocalTime getStartTime() {
        String str = parameters.getString("Start Time");
        return LocalTime.parse(str);
    }
    
    public LocalTime getEndTime() {
        String str = parameters.getString("End Time");
        return LocalTime.parse(str);
    }
    
    protected ExceptionNode getExceptionNode() {
        if (parent == null) {
            parent = (ExceptionNode) getParent();
        }
        return parent;
    }

}

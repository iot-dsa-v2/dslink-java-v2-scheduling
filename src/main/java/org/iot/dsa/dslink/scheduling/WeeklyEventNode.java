package org.iot.dsa.dslink.scheduling;

import java.util.Collection;
import java.util.Collections;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSMap;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

public class WeeklyEventNode extends EventNode implements EventCollection {
    
    private DSMap parameters;
    
    public WeeklyEventNode() {
        super();
    }
    
    public WeeklyEventNode(ScheduleNode parent, DSMap parameters) {
        super(parent);
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
    public DSElement getValue() {
        return parameters.get("Value");
    }

    @Override
    public long nextChange() {
        DateTime nowDt = DateTime.now();
        LocalTime now = nowDt.toLocalTime();
        LocalTime start = getStartTime();
        LocalTime end = getEndTime();
        int dow = nowDt.dayOfWeek().get();
        if (isActiveOn(dow) && now.isBefore(end)) {
            if (now.isBefore(start)) {
                return nowDt.withTime(start).getMillis();
            } else {
                return nowDt.withTime(end).getMillis();
            }
        } else {
            int i = 1;
            dow = incrementDayOfWeek(dow);
            while (!isActiveOn(dow)) {
                i++;
                dow = incrementDayOfWeek(dow);
                if (i > 7) {
                    throw new RuntimeException("event never active");
                }
            }
            return nowDt.plusDays(i).withTime(start).getMillis();
        }
    }

    @Override
    protected boolean isActiveTime() {
        DateTime nowDt = DateTime.now();
        if (isActiveOn(nowDt.dayOfWeek().get())) {
            LocalTime now = nowDt.toLocalTime();
            LocalTime start = getStartTime();
            LocalTime end = getEndTime();
            return !now.isBefore(start) && now.isBefore(end);
        } else {
            return false;
        }
    }

    @Override
    public Integer priority() {
        return 5;
    }
    
    private int incrementDayOfWeek(int dayOfWeek) {
        if (dayOfWeek >= 7) {
            return 1;
        } else {
            return dayOfWeek + 1;
        }
    }
    
    public LocalTime getStartTime() {
        String str = parameters.getString("Start Time");
        return LocalTime.parse(str);
    }
    
    public LocalTime getEndTime() {
        String str = parameters.getString("End Time");
        return LocalTime.parse(str);
    }
    
    public boolean isActiveOn(int dayOfWeek) {
        switch(dayOfWeek) {
            case 1: return isMonday();
            case 2: return isTuesday();
            case 3: return isWednesday();
            case 4: return isThursday();
            case 5: return isFriday();
            case 6: return isSaturday();
            case 7: return isSunday();
            default: throw new RuntimeException("invalid day of week");
        }
    }
    
    public boolean isMonday() {
        return parameters.getBoolean("Monday");
    }
    
    public boolean isTuesday() {
        return parameters.getBoolean("Tuesday");
    }
    
    public boolean isWednesday() {
        return parameters.getBoolean("Wednesday");
    }
    
    public boolean isThursday() {
        return parameters.getBoolean("Thursday");
    }
    
    public boolean isFriday() {
        return parameters.getBoolean("Friday");
    }
    
    public boolean isSaturday() {
        return parameters.getBoolean("Saturday");
    }
    
    public boolean isSunday() {
        return parameters.getBoolean("Sunday");
    }

    @Override
    public Collection<EventNode> getEvents() {
        return Collections.singleton(this);
    }

}

package org.iot.dsa.dslink.scheduling;

import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSJavaEnum;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * The main and only node of this link.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode {


    private static DSIRequester requester;

    public static DSIRequester getRequester() {
        return requester;
    }

    public static void setRequester(DSIRequester requester) {
        MainNode.requester = requester;
    }

    public MainNode() {
    }

    /**
     * Defines the permanent children of this node type, their existence is guaranteed in all
     * instances.  This is only ever called once per, type per process.
     */
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Add Schedule", makeAddScheduleAction());
        declareDefault("Help",
                       DSString.valueOf("https://github.com/iot-dsa-v2/dslink-java-v2-example"))
                .setTransient(true)
                .setReadOnly(true);
    }
    
    private DSAction makeAddScheduleAction() {
        DSAction act = new DSAction.Parameterless() { 
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation request) {
                ((MainNode) target.get()).addSchedule(request.getParameters());
                return null;
            }
        };
        act.addParameter("Name", DSValueType.STRING, null);
        act.addParameter("Type", DSJavaEnum.valueOf(DSValueType.STRING), null);
        return act;
    }
    
    private void addSchedule(DSMap parameters) {
        String name = parameters.getString("Name");
        DSValueType type = DSValueType.valueOf(parameters.getString("Type"));
        put(name, new ScheduleNode(type));
    }

    @Override
    protected void onStarted() {
        getLink().getConnection().subscribe(((event, node, child, data) -> {
            if (event.equals(DSLinkConnection.CONNECTED_EVENT)) {
                MainNode.setRequester(getLink().getConnection().getRequester());
            }
        }));
    }
    
    @Override
    protected void onStable() {
        super.onStable();
    }
}

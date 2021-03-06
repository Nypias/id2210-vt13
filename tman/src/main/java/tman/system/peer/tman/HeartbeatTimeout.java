package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout to send a heartbeat to the leader.
 */
public class HeartbeatTimeout extends Timeout
{
    /**
     * Create a new HeartbeatTimeout specifying the SchedulePeriodicTimeout.
     *
     * @param request The SchedulePeriodicTimeout.
     */
    public HeartbeatTimeout(SchedulePeriodicTimeout request) {
        super(request);
    }

    /**
     * Create a new HeartbeatTimeout specifying the ScheduleTimeout.
     *
     * @param request The ScheduleTimeout.
     */
    public HeartbeatTimeout(ScheduleTimeout request) {
        super(request);
    }
}

package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout to detect a dead leader and start an election.
 */
public class HeartbeatLeaderTimeout extends Timeout
{
    /**
     * Create a new HeartbeatLeaderTimeout specifying the SchedulePeriodicTimeout.
     *
     * @param request The SchedulePeriodicTimeout.
     */
    public HeartbeatLeaderTimeout(SchedulePeriodicTimeout request) {
        super(request);
    }

    /**
     * Create a new HeartbeatLeaderTimeout specifying the ScheduleTimeout.
     *
     * @param request The ScheduleTimeout.
     */
    public HeartbeatLeaderTimeout(ScheduleTimeout request) {
        super(request);
    }
}

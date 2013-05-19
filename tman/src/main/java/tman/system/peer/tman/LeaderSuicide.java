package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout to make the leader kill itself.
 */
public class LeaderSuicide extends Timeout
{
    /**
     * Create a new LeaderSuicide specifying the SchedulePeriodicTimeout.
     *
     * @param request The SchedulePeriodicTimeout.
     */
    public LeaderSuicide(SchedulePeriodicTimeout request) {
        super(request);
    }

    /**
     * Create a new LeaderSuicide specifying the ScheduleTimeout.
     *
     * @param request The ScheduleTimeout.
     */
    public LeaderSuicide(ScheduleTimeout request) {
        super(request);
    }
}

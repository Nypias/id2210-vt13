package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout to publish gradient partners to other components.
 */
public class TManSchedule extends Timeout
{
    /**
     * Create a new TManSchedule specifying the SchedulePeriodicTimeout.
     *
     * @param request The SchedulePeriodicTimeout.
     */
    public TManSchedule(SchedulePeriodicTimeout request) {
        super(request);
    }

    /**
     * Create a new TManSchedule specifying the ScheduleTimeout.
     *
     * @param request The ScheduleTimeout.
     */
    public TManSchedule(ScheduleTimeout request) {
        super(request);
    }
}
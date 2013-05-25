package search.system.peer.search;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout event to initiate the pulling of index information from neighbooring
 * nodes.
 */
public class UpdateIndexTimeout extends Timeout
{
    /**
     * Create a new UpdateIndexTimeout specifying the SchedulePeriodicTimeout.
     *
     * @param request The SchedulePeriodicTimeout.
     */
    public UpdateIndexTimeout(SchedulePeriodicTimeout request) {
        super(request);
    }

    /**
     * Create a new UpdateIndexTimeout specifying the ScheduleTimeout.
     *
     * @param request The ScheduleTimeout.
     */
    public UpdateIndexTimeout(ScheduleTimeout request) {
        super(request);
    }
}

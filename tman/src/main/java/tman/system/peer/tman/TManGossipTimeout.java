package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout event the TMan component will use to initiate gossip with other
 * gradient partners to exchange similarity lists.
 */
public class TManGossipTimeout extends Timeout
{
    /**
     * Create a new TManGossipTimeout specifying the SchedulePeriodicTimeout.
     *
     * @param request The SchedulePeriodicTimeout.
     */
    public TManGossipTimeout(SchedulePeriodicTimeout request) {
        super(request);
    }

    /**
     * Create a new TManGossipTimeout specifying the ScheduleTimeout.
     *
     * @param request The ScheduleTimeout.
     */
    public TManGossipTimeout(ScheduleTimeout request) {
        super(request);
    }
}

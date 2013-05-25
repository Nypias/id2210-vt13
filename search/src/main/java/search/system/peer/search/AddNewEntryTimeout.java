package search.system.peer.search;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout that a leader uses to stop waiting for replication ACKs from its
 * neighbors.
 */
public class AddNewEntryTimeout extends Timeout
{
    /**
     * The entry ID for which we timed out.
     */
    private String entryID;

    /**
     * Create a new AddNewEntryTimeout specifying the SchedulePeriodicTimeout.
     *
     * @param request The SchedulePeriodicTimeout.
     */
    public AddNewEntryTimeout(SchedulePeriodicTimeout request, String entryID) {
        super(request);
        this.entryID = entryID;
    }

    /**
     * Create a new AddNewEntryTimeout specifying the ScheduleTimeout.
     *
     * @param request The ScheduleTimeout.
     */
    public AddNewEntryTimeout(ScheduleTimeout request, String entryID) {
        super(request);
        this.entryID = entryID;
    }

    /**
     * Get the entry ID.
     *
     * @return The entry ID.
     */
    public String getEntryID() {
        return entryID;
    }
}

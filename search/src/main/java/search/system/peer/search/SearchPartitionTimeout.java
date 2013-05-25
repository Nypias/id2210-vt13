package search.system.peer.search;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout to detect a failed search in different partitions.
 */
public class SearchPartitionTimeout extends Timeout
{
    private Integer pendingSearchID;

    /**
     * Create a new SearchPartitionTimeout specifying the
     * SchedulePeriodicTimeout.
     *
     * @param request The SchedulePeriodicTimeout.
     */
    public SearchPartitionTimeout(SchedulePeriodicTimeout request, Integer pendingSearchID) {
        super(request);
        this.pendingSearchID = pendingSearchID;
    }

    /**
     * Create a new SearchPartitionTimeout specifying the ScheduleTimeout.
     *
     * @param request The ScheduleTimeout.
     */
    public SearchPartitionTimeout(ScheduleTimeout request, Integer pendingSearchID) {
        super(request);
        this.pendingSearchID = pendingSearchID;
    }

    /**
     * Get the ID of the pending search sent by the node performing the
     * search operation
     * 
     * @return An Integer representing the ID of the search operation.
     */
    public Integer getPendingSearchID() {
        return pendingSearchID;
    }
}

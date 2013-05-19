package tman.system.peer.tman;

import common.peer.PeerAddress;
import java.util.ArrayList;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout for a peer to declare itself the leader in a Bully election.
 */
public class ElectionTimeout extends Timeout
{
    /**
     * The list of peers that are part of the election
     */
    private ArrayList<PeerAddress> electionGroup;

    /**
     * Create a new ElectionTimeout specifying the SchedulePeriodicTimeout and
     * the election group.
     *
     * @param request The SchedulePeriodicTimeout.
     * @param electionGroup The list of peers that are part of the election.
     */
    public ElectionTimeout(SchedulePeriodicTimeout request, ArrayList<PeerAddress> electionGroup) {
        super(request);
        this.electionGroup = electionGroup;
    }

    /**
     * Create a new ElectionTimeout specifying the ScheduleTimeout and the
     * election group.
     *
     * @param request The ScheduleTimeout.
     * @param electionGroup The list of peers that are part of the election.
     */
    public ElectionTimeout(ScheduleTimeout request, ArrayList<PeerAddress> electionGroup) {
        super(request);
        this.electionGroup = electionGroup;
    }

    /**
     * Get the list of peers that are part of the election.
     *
     * @return The list of peers that are part of the election.
     */
    public ArrayList<PeerAddress> getElectionGroup() {
        return electionGroup;
    }
}

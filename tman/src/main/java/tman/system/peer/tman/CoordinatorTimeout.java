package tman.system.peer.tman;

import common.peer.PeerAddress;
import java.util.ArrayList;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Timeout for a peer to restart an election in case it doesn't receive a
 * CoordinatorMessage in time after receiving an OKMessage.
 */
public class CoordinatorTimeout extends Timeout
{
    /**
     * The list of peers that are part of the election
     */
    private ArrayList<PeerAddress> electionGroup;

    /**
     * Create a new CoordinatorTimeout specifying the SchedulePeriodicTimeout
     * and the election group.
     *
     * @param request The SchedulePeriodicTimeout.
     * @param electionGroup The list of peers that are part of the election.
     */
    public CoordinatorTimeout(SchedulePeriodicTimeout request, ArrayList<PeerAddress> electionGroup) {
        super(request);
        this.electionGroup = electionGroup;
    }

    /**
     * Create a new CoordinatorTimeout specifying the ScheduleTimeout and the
     * election group.
     *
     * @param request The ScheduleTimeout.
     * @param electionGroup The list of peers that are part of the election.
     */
    public CoordinatorTimeout(ScheduleTimeout request, ArrayList<PeerAddress> electionGroup) {
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

package tman.system.peer.tman;

import common.peer.PeerAddress;
import java.util.ArrayList;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


public class CoordinatorTimeout extends Timeout
{
    private ArrayList<PeerAddress> electionGroup;

    public CoordinatorTimeout(SchedulePeriodicTimeout request, ArrayList<PeerAddress> electionGroup) {
        super(request);
        this.electionGroup = electionGroup;
    }

    public CoordinatorTimeout(ScheduleTimeout request, ArrayList<PeerAddress> electionGroup) {
        super(request);
        this.electionGroup = electionGroup;
    }

    public ArrayList<PeerAddress> getElectionGroup() {
        return electionGroup;
    }
}

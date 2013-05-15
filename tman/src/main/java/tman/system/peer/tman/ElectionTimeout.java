package tman.system.peer.tman;

import common.peer.PeerAddress;
import java.util.ArrayList;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


public class ElectionTimeout extends Timeout
{
    private ArrayList<PeerAddress> electionGroup;

    public ElectionTimeout(SchedulePeriodicTimeout request, ArrayList<PeerAddress> electionGroup) {
        super(request);
        this.electionGroup = electionGroup;
    }

    public ElectionTimeout(ScheduleTimeout request, ArrayList<PeerAddress> electionGroup) {
        super(request);
        this.electionGroup = electionGroup;
    }

    public ArrayList<PeerAddress> getElectionGroup() {
        return electionGroup;
    }
}

package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


public class LeaderSuicide extends Timeout
{
    public LeaderSuicide(SchedulePeriodicTimeout request) {
        super(request);
    }

//-------------------------------------------------------------------
    public LeaderSuicide(ScheduleTimeout request) {
        super(request);
    }
}

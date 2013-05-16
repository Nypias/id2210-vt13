package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

public class TManGossipTimeout extends Timeout {

	public TManGossipTimeout(SchedulePeriodicTimeout request) {
		super(request);
	}

//-------------------------------------------------------------------
	public TManGossipTimeout(ScheduleTimeout request) {
		super(request);
	}
}

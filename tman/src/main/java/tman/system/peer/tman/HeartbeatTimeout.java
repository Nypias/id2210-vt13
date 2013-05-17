package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

public class HeartbeatTimeout extends Timeout {

	public HeartbeatTimeout(SchedulePeriodicTimeout request) {
		super(request);
	}

//-------------------------------------------------------------------
	public HeartbeatTimeout(ScheduleTimeout request) {
		super(request);
	}
}

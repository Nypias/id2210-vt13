package tman.system.peer.tman;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

public class HeartbeatLeaderTimeout extends Timeout {

	public HeartbeatLeaderTimeout(SchedulePeriodicTimeout request) {
		super(request);
	}

//-------------------------------------------------------------------
	public HeartbeatLeaderTimeout(ScheduleTimeout request) {
		super(request);
	}
}

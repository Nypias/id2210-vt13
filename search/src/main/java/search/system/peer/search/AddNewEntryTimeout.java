package search.system.peer.search;

import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


public class AddNewEntryTimeout extends Timeout
{
    private String entryID;

    public AddNewEntryTimeout(SchedulePeriodicTimeout request, String entryID) {
        super(request);
        this.entryID = entryID;
    }

    public AddNewEntryTimeout(ScheduleTimeout request, String entryID) {
        super(request);
        this.entryID = entryID;
    }

    public String getEntryID() {
        return entryID;
    }
}

package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import cyclon.system.peer.cyclon.DescriptorBuffer;
import java.util.ArrayList;
import java.util.UUID;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


public class ExchangeMsg
{
    public static class Request extends PeerMessage
    {
        private static final long serialVersionUID = 8493601671018888143L;
        private final UUID requestId;
        private final ArrayList<PeerAddress> randomBuffer;

//-------------------------------------------------------------------
        public Request(UUID requestId, ArrayList<PeerAddress> randomBuffer, PeerAddress source, PeerAddress destination) {
            super(source, destination);
            this.requestId = requestId;
            this.randomBuffer = randomBuffer;
        }

//-------------------------------------------------------------------
        public UUID getRequestId() {
            return requestId;
        }

        //-------------------------------------------------------------------
        public ArrayList<PeerAddress> getSimilaritySet() {
            return randomBuffer;
        }

//-------------------------------------------------------------------
        public int getSize() {
            return 0;
        }
    }

    public static class Response extends PeerMessage
    {
        private static final long serialVersionUID = -5022051054665787770L;
        private final UUID requestId;
        private final ArrayList<PeerAddress> selectedBuffer;

//-------------------------------------------------------------------
        public Response(UUID requestId, ArrayList<PeerAddress> selectedBuffer, PeerAddress source, PeerAddress destination) {
            super(source, destination);
            this.requestId = requestId;
            this.selectedBuffer = selectedBuffer;
        }

//-------------------------------------------------------------------
        public UUID getRequestId() {
            return requestId;
        }

//-------------------------------------------------------------------
        public ArrayList<PeerAddress> getSimilaritySet() {
            return selectedBuffer;
        }

//-------------------------------------------------------------------
        public int getSize() {
            return 0;
        }
    }

    public static class RequestTimeout extends Timeout
    {
        private final PeerAddress peer;

//-------------------------------------------------------------------
        public RequestTimeout(ScheduleTimeout request, PeerAddress peer) {
            super(request);
            this.peer = peer;
        }

//-------------------------------------------------------------------
        public PeerAddress getPeer() {
            return peer;
        }
    }

}
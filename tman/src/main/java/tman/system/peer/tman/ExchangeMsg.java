package tman.system.peer.tman;

import common.peer.PeerAddress;
import common.peer.PeerMessage;
import java.util.ArrayList;
import java.util.UUID;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;


/**
 * Request and response messages exchanged by the gradient peers in order to
 * find better neighbors for them and converge.
 */
public class ExchangeMsg
{
    /**
     * Request message exchanged by the gradient peers to send out their
     * similarity lists and ask for the list of the other peer.
     */
    public static class Request extends PeerMessage
    {
        private static final long serialVersionUID = 8493601671018888143L;
        /**
         * An unique ID for the request being made
         */
        private final UUID requestId;
        /**
         * The list of peers that the peer is sending out for exchange.
         */
        private final ArrayList<PeerAddress> randomBuffer;

        /**
         * Create a Request specifying its unique ID, the list of peers to send
         * as well as the sender and receiver peers of the message.
         *
         * @param requestId The unique ID that marks the exchange.
         * @param randomBuffer The list of peers sent to the other peer.
         * @param source The peer that sends the message.
         * @param destination The peer that receives the message.
         */
        public Request(UUID requestId, ArrayList<PeerAddress> randomBuffer, PeerAddress source, PeerAddress destination) {
            super(source, destination);
            this.requestId = requestId;
            this.randomBuffer = randomBuffer;
        }

        /**
         * Get the unique ID of the specific request.
         *
         * @return The unique ID of the request.
         */
        public UUID getRequestId() {
            return requestId;
        }

        /**
         * Get the list of peers sent by the initiating peer.
         *
         * @return The list of peer in the sending node`s similarity list.
         */
        public ArrayList<PeerAddress> getSimilaritySet() {
            return randomBuffer;
        }
    }

    /**
     * Response message exchanged by the gradient peers as a response to a
     * Request message (see above) in order to send back their similarity list.
     */
    public static class Response extends PeerMessage
    {
        private static final long serialVersionUID = -5022051054665787770L;
        /**
         * An unique ID for the response we are sending out (to a specific
         * request).
         */
        private final UUID requestId;
        /**
         * The list of peers that the peer is sending as a response to the
         * exchange.
         */
        private final ArrayList<PeerAddress> selectedBuffer;

        /**
         * Create a Response specifying its unique ID, the list of peers to send
         * back as well as the sender and receiver peers of the message.
         *
         * @param requestId The unique ID that marks the exchange.
         * @param randomBuffer The list of peers sent as a response.
         * @param source The peer that sends the message.
         * @param destination The peer that receives the message.
         */
        public Response(UUID requestId, ArrayList<PeerAddress> selectedBuffer, PeerAddress source, PeerAddress destination) {
            super(source, destination);
            this.requestId = requestId;
            this.selectedBuffer = selectedBuffer;
        }

        /**
         * Get the unique ID of the specific request.
         *
         * @return The unique ID of the request.
         */
        public UUID getRequestId() {
            return requestId;
        }

        /**
         * Get the list of peers sent by the initiating peer.
         *
         * @return The list of peer in the sending node`s similarity list.
         */
        public ArrayList<PeerAddress> getSimilaritySet() {
            return selectedBuffer;
        }
    }

    /**
     * Timeout to detect a similarity list exchange failure.
     */
    public static class RequestTimeout extends Timeout
    {
        /**
         * The address of the peer that failed to respond.
         */
        private final PeerAddress peer;

        /**
         * Create a new RequestTimeout specifying the ScheduleTimeout and the
         * peer that we are waiting a response from.
         *
         * @param request The SchedulePeriodicTimeout.
         */
        public RequestTimeout(ScheduleTimeout request, PeerAddress peer) {
            super(request);
            this.peer = peer;
        }

        /**
         * Get the peer that we are waiting a response from (which timed out).
         *
         * @return The peer from whom we are waiting a response from.
         */
        public PeerAddress getPeer() {
            return peer;
        }
    }

}
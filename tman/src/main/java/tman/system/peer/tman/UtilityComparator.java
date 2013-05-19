package tman.system.peer.tman;

import common.peer.PeerAddress;
import java.math.BigInteger;
import java.util.Comparator;


/**
 * Class to provide comparison based on preference between peers.
 *
 * The compare method implements the Gradient function using the peer ID as the
 * utility value.
 *
 */
public class UtilityComparator implements Comparator<PeerAddress>
{
    /**
     * The ID of the peer based on whose preference we are ordering the other
     * peers
     */
    private BigInteger utilityBN;

    /**
     * Create a new UtilityComparator for a specific base node ID.
     *
     * The ID of the base node is used as a reference for the comparisons
     *
     * @param baseNode
     */
    public UtilityComparator(PeerAddress baseNode) {
        this.utilityBN = baseNode.getPeerId();
    }

    /**
     * Compare utility values (IDs) of two nodes based on the base node.
     *
     * If both nodes have higher (or lower) utility than the base node,
     * we keep the one that is closest to us.
     * 
     * If one node has higher utility than the base node and the other has
     * lower utility, then we keep the one with the higher utility.
     *
     * @param a First peer to compare
     * @param b Second peer to compare
     * @return -1 if b is more preferred, 1 if a is more preferred and 0 if
     *         nodes have the same utility value.
     */
    @Override
    public int compare(PeerAddress a, PeerAddress b) {
        BigInteger utilityA = a.getPeerId();
        BigInteger utilityB = b.getPeerId();

        // First gradient condition
        if (utilityA.compareTo(utilityBN) == 1 && utilityB.compareTo(utilityBN) == -1) {
            return 1;
        }

        if (utilityA.compareTo(utilityBN) == -1 && utilityB.compareTo(utilityBN) == 1) {
            return -1;
        }

        // Second gradient condition
        BigInteger distanceA = (utilityA.subtract(utilityBN)).abs();
        BigInteger distanceB = (utilityB.subtract(utilityBN)).abs();

        if (distanceA.compareTo(distanceB) == -1) {
            return 1;
        }

        return -1;
    }
}

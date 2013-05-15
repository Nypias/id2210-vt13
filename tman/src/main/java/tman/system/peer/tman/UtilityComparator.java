package tman.system.peer.tman;

import common.peer.PeerAddress;
import java.math.BigInteger;
import java.util.Comparator;


public class UtilityComparator implements Comparator<PeerAddress>
{
    private BigInteger utilityBN;
    
    public UtilityComparator(PeerAddress baseNode) {
        this.utilityBN = baseNode.getPeerId();
    }
    
    /**
     * Compare utility values of two nodes
     * @param a
     * @param b
     * @return 
     */
    @Override
    public int compare(PeerAddress a, PeerAddress b) {
        BigInteger utilityA = a.getPeerId();
        BigInteger utilityB = b.getPeerId();
        
        // First gradient condition\
        if(utilityA.compareTo(utilityBN) == 1 && utilityB.compareTo(utilityBN) == -1) {
            return 1;
        }
        
        // Second gradient condition
        BigInteger distanceA = (utilityA.subtract(utilityBN)).abs();
        BigInteger distanceB = (utilityB.subtract(utilityBN)).abs();
        
        return (-1) * distanceA.compareTo(distanceB);
    }
}

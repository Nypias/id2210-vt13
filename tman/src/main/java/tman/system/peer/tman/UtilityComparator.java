package tman.system.peer.tman;

import common.peer.PeerAddress;
import java.math.BigInteger;
import java.util.Comparator;


public class UtilityComparator implements Comparator<PeerAddress>
{
    private PeerAddress baseNode;
    
    public UtilityComparator(PeerAddress baseNode) {
        this.baseNode = baseNode;
    }
    
    @Override
    public int compare(PeerAddress a, PeerAddress b) {
        BigInteger distanceA = (a.getPeerId().subtract(baseNode.getPeerId())).abs();
        BigInteger distanceB = (b.getPeerId().subtract(baseNode.getPeerId())).abs();
        return (-1) * distanceA.compareTo(distanceB);
    }
}

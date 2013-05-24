package common.simulation;

import common.peer.PeerAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Class to collect statistics of the system
 */
public class Stats
{
    private static final int NUMBER_OF_PEERS = 200;
    private static PeerAddress leader;
    
    private static boolean reported = false;
    private static int electionMessages = 0;
    private static ArrayList<Integer> leaderSearchStats = new ArrayList<Integer>();
    private static ArrayList<Integer> indexDissemninationStats = new ArrayList<Integer>();
    private static Map<Integer, Integer> partitionLoadBalancingStats = new HashMap<Integer, Integer>();

    // Election Statistics
    public synchronized static void registerElectionMessage() {
        electionMessages++;
    }

    public synchronized static void registerElectionMessages(int numberOfMessages) {
        electionMessages = electionMessages + numberOfMessages;
    }

    public synchronized static void clearElectionMessages() {
        electionMessages = 0;
    }

    public synchronized static void reportElectionMessages() {
        System.err.println("[STATS] Election messages " + electionMessages);
    }

    // Leader Search Statistics
    public synchronized static void registerLeaderSearchStats(int hops) {
        leaderSearchStats.add(new Integer(hops));
    }

    public synchronized static void reportLeaderSearchStats() {
        System.err.println("[STATS] Leader search stats\n\t\tMaximum " + maximum(leaderSearchStats) + "\n\t\tMinimum " + minimum(leaderSearchStats) + "\n\t\tAverage " + average(leaderSearchStats));
    }

    // Index Dissemination Statistics
    public synchronized static void registerCompleteIndex(int rounds) {
        if(indexDissemninationStats.size() < NUMBER_OF_PEERS) {
            indexDissemninationStats.add(new Integer(rounds));
        } else if(indexDissemninationStats.size() == NUMBER_OF_PEERS && !reported) {
            reported = true;
            System.err.println("[STATS] The index was disseminated to " + indexDissemninationStats.size() + " peers in : ");
            System.err.println("Maximum : " + maximum(indexDissemninationStats));
            System.err.println("Average : " + average(indexDissemninationStats));
            System.err.println("Minimum : " + minimum(indexDissemninationStats));
        }
    }
    
    public synchronized static void reportIndexDisseminationStats() {
        if(indexDissemninationStats.size() == NUMBER_OF_PEERS) {
            System.err.println("[STATS] The index was disseminated to " + indexDissemninationStats.size() + "in " + maximum(indexDissemninationStats) + " rounds!");
        } else {
            System.err.println("[STATS] Index dissemination is not complete yet (" + indexDissemninationStats.size() + " / " + NUMBER_OF_PEERS + ")!");
        }
    }
    
    // Partition index load balancing statistics
    public synchronized static void registerPartitionIndexLoad(int partitionID, int indexCount) {
        partitionLoadBalancingStats.put(partitionID, indexCount);
    }
    
    public synchronized static void reportPartitionIndexLoad() {
        System.err.println("Partition Load Balancing");
        System.err.println(partitionLoadBalancingStats);
    }
    
    // Leader reporting
    public synchronized static void reportLeader(PeerAddress leader) {
        Stats.leader = leader;
        System.err.println("[STATS] Leader reported to be " + leader.getPeerId());
    }
    
    public synchronized static PeerAddress getLeader() {
        return Stats.leader;
    }

    // Private Utility Functions
    private static int average(ArrayList<Integer> list) {
        int sum = 0;
        for (Integer datum : list) {
            sum += datum;
        }

        return (sum / list.size());
    }

    private static int minimum(ArrayList<Integer> list) {
        int min = 0;
        for (Integer datum : list) {
            if (datum < min) {
                min = datum;
            }
        }

        return min;
    }

    private static int maximum(ArrayList<Integer> list) {
        int max = list.get(0);
        for (Integer datum : list) {
            if (datum > max) {
                max = datum;
            }
        }

        return max;
    }
}

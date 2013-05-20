package tman.simulator.snapshot;

import java.util.ArrayList;


/**
 * Class to collect statistics of the system
 */
public class Stats
{
    private static final int NUMBER_OF_PEERS = 50;
    
    private static boolean reported = false;
    private static int electionMessages = 0;
    private static ArrayList<Integer> leaderSearchStats = new ArrayList<Integer>();
    private static ArrayList<Integer> indexDissemninationStats = new ArrayList<Integer>();

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
            System.err.println("[STATS] The index was disseminated to " + indexDissemninationStats.size() + " peers in " + maximum(indexDissemninationStats) + " rounds!");
        }
    }
    
    public synchronized static void reportIndexDisseminationStats() {
        if(indexDissemninationStats.size() == NUMBER_OF_PEERS) {
            System.err.println("[STATS] The index was disseminated to " + indexDissemninationStats.size() + "in " + maximum(indexDissemninationStats) + " rounds!");
        } else {
            System.err.println("[STATS] Index dissemination is not complete yet (" + indexDissemninationStats.size() + " / " + NUMBER_OF_PEERS + ")!");
        }
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

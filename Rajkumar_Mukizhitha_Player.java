class Rajkumar_Mukizhitha_Player extends Player {
    // initial forgiveness threshold: probabaility that even if other players
    // defect, agent still forgives and cooperates
    double forgivenessThreshold = 0.99;
    double retaliationThreshold = 0.01; // Initial retaliation threshold

    int selectAction(int n, int[] myAgentPrevMoves, int[] otherAgent1PrevMoves, int[] otherAgent2PrevMoves) {
        // exploration proabaility refers to the probability that the agent explores by
        // randomly choosing
        final double EXPLORATION_PROBABILITY = 0.01; // Probability of exploration

        // check if we're still in the early rounds for exploration
        if (n < 10 && Math.random() < EXPLORATION_PROBABILITY) {
            // if we are in the early rounds and the exploration probability threshold is
            // met, the agent explores by randomly selecting cooperation or defection
            return Math.random() < 0.5 ? 0 : 1;
        }

        // Update forgiveness and retaliation thresholds based on observed behavior
        thresholdUpdate(myAgentPrevMoves, otherAgent1PrevMoves, otherAgent2PrevMoves);

        // if the current round number is invalid, return a default action (cooperate)
        if (n <= 0) {
            // If n is not valid, return a default action (cooperate)
            return 0;
        }

        // store the last move of other agents
        int otherAgent1LastMove = otherAgent1PrevMoves[n - 1];
        int otherAgent2LastMove = otherAgent2PrevMoves[n - 1];

        // calculate forgiveness and retaliation probabilities
        double forgivenessProbability = calculateForgiveness(myAgentPrevMoves, otherAgent1PrevMoves,
                otherAgent2PrevMoves);
        double retaliationProbability = calculateRetaliation(myAgentPrevMoves, otherAgent1PrevMoves,
                otherAgent2PrevMoves);

        // determine action based on probabilities and thresholds
        // if the random number generated falls within the forgiveness probability and
        // threshold, the agent cooperates despite opponents' defections
        // if the random number generated falls within the retaliation probability and
        // threshold, the agent defects in retaliation
        // if neither forgiveness nor retaliation conditions are met, the agent mimics
        // the opponent who cooperated in the previous round
        if (Math.random() < forgivenessProbability && Math.random() < forgivenessThreshold) {
            return 0;
        } else if (Math.random() < retaliationProbability && Math.random() < retaliationThreshold) {
            return 1;
        } else {
            if (otherAgent1LastMove == 0) {
                return otherAgent1LastMove;
            } else {
                return otherAgent2LastMove;
            }
        }
    }

    // update forgiveness and retaliation thresholds
    void thresholdUpdate(int[] myAgentPrevMoves, int[] otherAgent1PrevMoves, int[] otherAgent2PrevMoves) {
        // first, calculate the cooperation rates of other agents to decide whether to
        // increase or decrease the thresholds
        double otherAgent1CooperationRate = calculateCooperationRate(otherAgent1PrevMoves);
        double otherAgent2CooperationRate = calculateCooperationRate(otherAgent2PrevMoves);

        // if other agent cooperated more than it defected then increase forgiveness
        if (otherAgent1CooperationRate > 0.7 || otherAgent2CooperationRate > 0.7) {
            forgivenessThreshold += 0.05; // Increase forgiveness threshold
            // if not decrease the threshold
        } else if (otherAgent1CooperationRate < 0.3 && otherAgent2CooperationRate < 0.3) {
            forgivenessThreshold -= 0.05; // Decrease forgiveness threshold
        }

        // need to make sure threshold stays within bounds of 0 to 1
        forgivenessThreshold = Math.max(0, Math.min(1, forgivenessThreshold));

        // likewise, update the retaliation threshold but the logic is opposite
        if (otherAgent1CooperationRate < 0.3 || otherAgent2CooperationRate < 0.3) {
            // increasing the retailiation threshold by a little
            retaliationThreshold += 0.05;
        } else if (otherAgent1CooperationRate > 0.7 && otherAgent2CooperationRate > 0.7) {
            // decreasing the retailiation threshold by a little
            retaliationThreshold -= 0.05;
        }
        // need to make sure threshold stays within bounds of 0 to 1
        retaliationThreshold = Math.max(0, Math.min(1, retaliationThreshold));
    }

    // to determine the action, need to calculate the forgiveness probability based
    // on agents' history
    double calculateForgiveness(int[] myAgentPrevMoves, int[] oppHistory1, int[] oppHistory2) {
        // Count the number of recent defections by opponents
        int recentDefections = 0;
        for (int i = Math.max(0, myAgentPrevMoves.length - 10); i < myAgentPrevMoves.length; i++) {
            if (oppHistory1[i] == 1 || oppHistory2[i] == 1) {
                recentDefections++;
            }
        }
        // Calculate forgiveness probability based on recent defections
        return 1.0 - (double) recentDefections / 10.0;
    }

    // to calculate retaliation probability
    // count the number of recent mutual defections
    double calculateRetaliation(int[] myAgentPrevMoves, int[] otherAgent1PrevMoves, int[] otherAgent2PrevMoves) {
        int recentMutualDefections = 0;
        for (int i = Math.max(0, myAgentPrevMoves.length - 10); i < myAgentPrevMoves.length; i++) {
            if (myAgentPrevMoves[i] == 1 && (otherAgent1PrevMoves[i] == 1 || otherAgent2PrevMoves[i] == 1)) {
                recentMutualDefections++;
            }
        }
        // calculate retaliation probability based on recent mutual defections
        return (double) recentMutualDefections / 10.0;
    }

    // helper method to calculate opponent's cooperation rate
    double calculateCooperationRate(int[] history) {
        // calculate opponent's cooperation rate based on their history
        int totalCooperation = 0;
        for (int move : history) {
            if (move == 0) {
                totalCooperation++;
            }
        }
        return (double) totalCooperation / history.length;
    }
}

class Lim_Jiexian_Player extends Player {
    private int forgivenessThreshold = 2; // Number of consecutive cooperations required for forgiveness
    private int strictDefectionResponse = 2; // Become strict if any opponent defects this many times consecutively
    private double randomnessThreshold = 0.02; // Probability of random cooperation
    
    int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
        if (n == 0) return 0; // Always cooperate on the first turn

        // Implementing randomness
        if (Math.random() < randomnessThreshold) {
            return 0; // Randomly cooperate with a small probability
        }

        // Check for recent consecutive defections by either opponent
        boolean strictResponseTriggered = false;
        for (int i = Math.max(0, n - strictDefectionResponse); i < n; i++) {
            if ((i < n - 1 && oppHistory1[i] == 1 && oppHistory1[i+1] == 1) || 
                (i < n - 1 && oppHistory2[i] == 1 && oppHistory2[i+1] == 1)) {
                strictResponseTriggered = true;
                break;
            }
        }

        if (strictResponseTriggered) {
            return 1; // Defect in response to consecutive defections
        }

        // Calculate the majority action across all rounds for forgiveness consideration
        int coopCount = 0;
        for (int i = 0; i < n; i++) {
            coopCount += (oppHistory1[i] == 0 ? 1 : 0) + (oppHistory2[i] == 0 ? 1 : 0);
        }

        if (coopCount < n) {
            int recentCoop = 0;
            for (int i = Math.max(0, n - forgivenessThreshold); i < n; i++) {
                recentCoop += (oppHistory1[i] == 0 ? 1 : 0) + (oppHistory2[i] == 0 ? 1 : 0);
            }

            if (recentCoop == forgivenessThreshold * 2) { // * 2 because two opponents
                return 0; // Forgive and cooperate if recent cooperation is observed
            }
        }

        // Default behavior based on overall history
        return (coopCount >= n) ? 0 : 1;
    }
}
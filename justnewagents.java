import java.util.Random;

public class justnewagents {
    /*
     * This Java program models the two-player Prisoner's Dilemma game.
     * We use the integer "0" to represent cooperation, and "1" to represent
     * defection.
     * 
     * Recall that in the 2-players dilemma, U(DC) > U(CC) > U(DD) > U(CD), where
     * we give the payoff for the first player in the list. We want the three-player
     * game
     * to resemble the 2-player game whenever one player's response is fixed, and we
     * also want symmetry, so U(CCD) = U(CDC) etc. This gives the unique ordering
     * 
     * U(DCC) > U(CCC) > U(DDC) > U(CDC) > U(DDD) > U(CDD)
     * 
     * The payoffs for player 1 are given by the following matrix:
     */

    static int[][][] payoff = {
            { { 6, 3 }, // payoffs when first and second players cooperate
                    { 3, 0 } }, // payoffs when first player coops, second defects
            { { 8, 5 }, // payoffs when first player defects, second coops
                    { 5, 2 } } };// payoffs when first and second players defect

    /*
     * So payoff[i][j][k] represents the payoff to player 1 when the first
     * player's action is i, the second player's action is j, and the
     * third player's action is k.
     * 
     * In this simulation, triples of players will play each other repeatedly in a
     * 'match'. A match consists of about 100 rounds, and your score from that match
     * is the average of the payoffs from each round of that match. For each round,
     * your
     * strategy is given a list of the previous plays (so you can remember what your
     * opponent did) and must compute the next action.
     */

    abstract class Player {
        // This procedure takes in the number of rounds elapsed so far (n), and
        // the previous plays in the match, and returns the appropriate action.
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            throw new RuntimeException("You need to override the selectAction method.");
        }

        // Used to extract the name of this player class.
        final String name() {
            String result = getClass().getName();
            return result.substring(result.indexOf('$') + 1);
        }
    }
    /* Here are four simple strategies: */

    class NicePlayer extends Player {
        // NicePlayer always cooperates
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 0;
        }
    }

    class NastyPlayer extends Player {
        // NastyPlayer always defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 1;
        }
    }

    class RandomPlayer extends Player {
        // RandomPlayer randomly picks his action each time
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (Math.random() < 0.5)
                return 0; // cooperates half the time
            else
                return 1; // defects half the time
        }
    }

    class TolerantPlayer extends Player {
        // TolerantPlayer looks at his opponents' histories, and only defects
        // if at least half of the other players' actions have been defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int opponentCoop = 0;
            int opponentDefect = 0;
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 0)
                    opponentCoop = opponentCoop + 1;
                else
                    opponentDefect = opponentDefect + 1;
            }
            for (int i = 0; i < n; i++) {
                if (oppHistory2[i] == 0)
                    opponentCoop = opponentCoop + 1;
                else
                    opponentDefect = opponentDefect + 1;
            }
            if (opponentDefect > opponentCoop)
                return 1;
            else
                return 0;
        }
    }

    class FreakyPlayer extends Player {
        // FreakyPlayer determines, at the start of the match,
        // either to always be nice or always be nasty.
        // Note that this class has a non-trivial constructor.
        int action;

        FreakyPlayer() {
            if (Math.random() < 0.5)
                action = 0; // cooperates half the time
            else
                action = 1; // defects half the time
        }

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return action;
        }
    }

    class T4TPlayer extends Player {
        // Picks a random opponent at each play,
        // and uses the 'tit-for-tat' strategy against them
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 0; // cooperate by default
            if (Math.random() < 0.5)
                return oppHistory1[n - 1];
            else
                return oppHistory2[n - 1];
        }
    }

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

        class Lim_Jiexian_Player extends Player {
            private int forgivenessThreshold = 2; // Number of consecutive cooperations required for forgiveness
            private int strictDefectionResponse = 2; // Become strict if any opponent defects this many times
                                                     // consecutively
            private double randomnessThreshold = 0.02; // Probability of random cooperation

            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0; // Always cooperate on the first turn

                // Implementing randomness
                if (Math.random() < randomnessThreshold) {
                    return 0; // Randomly cooperate with a small probability
                }

                // Check for recent consecutive defections by either opponent
                boolean strictResponseTriggered = false;
                for (int i = Math.max(0, n - strictDefectionResponse); i < n; i++) {
                    if ((i < n - 1 && oppHistory1[i] == 1 && oppHistory1[i + 1] == 1) ||
                            (i < n - 1 && oppHistory2[i] == 1 && oppHistory2[i + 1] == 1)) {
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

        // Strategy 1: AdaptiveForgiver
        // Forgives occasional defections but becomes less tolerant if defections
        // persist.
        class AdaptiveForgiver extends Player {
            private int consecutiveDefections = 0;

            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1)
                    consecutiveDefections++;
                else
                    consecutiveDefections = 0;

                return consecutiveDefections > 2 ? 1 : 0;
            }
        }

        // Strategy 2: ResponsiveTFT
        // Tit for Tat strategy that adjusts responsiveness based on opponents'
        // cooperation rate.
        class ResponsiveTFT extends Player {
            double cooperationThreshold = 0.6; // Adapt responsiveness based on this threshold.

            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                double coopRate = (double) java.util.Arrays.stream(oppHistory1).filter(x -> x == 0).count() / n;
                if (coopRate > cooperationThreshold) {
                    return oppHistory1[n - 1]; // Mimic last action if above threshold.
                }
                return 1; // Default to defect if cooperation is low.
            }
        }

        // Strategy 3: StrategicDefector
        // Defects strategically when it can gain an upper hand.
        class StrategicDefector extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                if (n % 5 == 0)
                    return 1; // Defect every 5 rounds to disrupt opponent strategy.
                return oppHistory1[n - 1]; // Otherwise mimic the last action of opponent 1.
            }
        }

        // Strategy 4: CoopMajority
        // Cooperates if the majority of actions in the last few rounds were
        // cooperative.
        class CoopMajority extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                int coopCount = 0;
                int lookBack = Math.min(n, 10);
                for (int i = 1; i <= lookBack; i++) {
                    if (oppHistory1[n - i] == 0)
                        coopCount++;
                    if (oppHistory2[n - i] == 0)
                        coopCount++;
                }
                return coopCount >= lookBack ? 0 : 1;
            }
        }

        // Strategy 5: OpportunisticTFT
        // Plays Tit for Tat but takes occasional opportunities to defect when doing
        // well.
        class OpportunisticTFT extends Player {
            private int myScore = 0;

            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                myScore += payoff[myHistory[n - 1]][oppHistory1[n - 1]][oppHistory2[n - 1]];
                if (myScore > n * 6 && Math.random() < 0.1)
                    return 1; // Defect occasionally when scoring high.
                return oppHistory1[n - 1];
            }
        }

        // Strategy 6: ScoreAwareCooperator
        // Cooperates based on comparative scoring, favoring cooperation when leading.
        class ScoreAwareCooperator extends Player {
            private int myScore = 0;

            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                myScore += payoff[myHistory[n - 1]][oppHistory1[n - 1]][oppHistory2[n - 1]];
                if (myScore > n * 6)
                    return 0; // Continue cooperating if leading.
                return 1; // Defect to catch up or maintain lead.
            }
        }

        // Strategy 7: MutualCooperationTracker
        // Encourages mutual cooperation, defects if either opponent defects too
        // frequently.
        class MutualCooperationTracker extends Player {
            private int totalDefections = 0;

            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                totalDefections += (oppHistory1[n - 1] == 1 ? 1 : 0) + (oppHistory2[n - 1] == 1 ? 1 : 0);
                if ((double) totalDefections / (2 * n) > 0.3)
                    return 1; // Defect if defections exceed 30%.
                return 0;
            }
        }

        // Strategy 8: BalancedTFT
        // Similar to Tit for Tat but balances between two opponents' actions.
        class BalancedTFT extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                int lastAction1 = oppHistory1[n - 1];
                int lastAction2 = oppHistory2[n - 1];
                return (lastAction1 + lastAction2) / 2; // Average out the actions to decide.
            }
        }

        // Strategy 9: LeadFollow
        // Leads with cooperation and follows the most cooperative opponent.
        class LeadFollow extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0;
                return oppHistory1[n - 1] < oppHistory2[n - 1] ? oppHistory1[n - 1] : oppHistory2[n - 1];
            }
        }

        // Strategy 10: CyclicPlayer
        // Cycles through different strategies based on the round number.
        class CyclicPlayer extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                int cycle = n % 3;
                switch (cycle) {
                    case 0:
                        return 0; // Always cooperate on first phase.
                    case 1:
                        return (oppHistory1[n - 1] + oppHistory2[n - 1]) / 2; // Average opponents' actions.
                    case 2:
                        return 1; // Always defect on third phase.
                    default:
                        return 0;
                }
            }
        }

        class WeightedHistoryAwarePlayer extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n == 0)
                    return 0; // Cooperate on the first turn

                // Calculate the weighted sum of cooperation across all rounds
                int totalWeight = 0;
                int coopCount = 0;
                for (int i = 0; i < n; i++) {
                    // Assign higher weights to more recent rounds
                    int weight = n - i;
                    totalWeight += weight;
                    coopCount += (oppHistory1[i] == 0 ? weight : 0) + (oppHistory2[i] == 0 ? weight : 0);
                }

                // Calculate the average cooperation rate
                double coopRate = (double) coopCount / totalWeight;

                // Decide based on the weighted average cooperation rate
                return (coopRate >= 0.2) ? 0 : 1; // Cooperate if the weighted average is greater than or equal to 0.5
            }
        }

        class TitFor3TatsPlayer extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n < 3)
                    return 0; // Cooperate for the first three rounds

                // Check if the opponent has defected in the last three rounds
                if (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1 && oppHistory1[n - 3] == 1) {
                    return 1; // Defect if opponent has defected in the last three rounds
                }
                if (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1 && oppHistory2[n - 3] == 1) {
                    return 1; // Defect if opponent has defected in the last three rounds
                }

                return 0; // Cooperate otherwise
            }
        }

        class TitFor4TatsPlayer extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n < 4)
                    return 0; // Cooperate for the first four rounds

                // Check if the opponent has defected in the last four rounds
                if (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1 && oppHistory1[n - 3] == 1
                        && oppHistory1[n - 4] == 1) {
                    return 1; // Defect if opponent has defected in the last four rounds
                }
                if (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1 && oppHistory2[n - 3] == 1
                        && oppHistory2[n - 4] == 1) {
                    return 1; // Defect if opponent has defected in the last four rounds
                }

                return 0; // Cooperate otherwise
            }
        }

        class TitFor3TatsPlayerRandom extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n < 3)
                    return 0; // Cooperate for the first three rounds

                // Check if the opponent has defected in the last three rounds
                if (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1 && oppHistory1[n - 3] == 1) {
                    return 1; // Defect if opponent has defected in the last three rounds
                }
                if (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1 && oppHistory2[n - 3] == 1) {
                    return 1; // Defect if opponent has defected in the last three rounds
                }

                // Introduce randomness: Cooperate with a certain probability
                double randomProbability = 0.1; // Adjust this value as desired
                if (Math.random() < randomProbability) {
                    return 1; // Defect with probability randomProbability
                }

                return 0; // Cooperate otherwise
            }
        }

        class ForgivingTitFor3TatsPlayer extends Player {
            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                if (n < 3)
                    return 0; // Cooperate for the first three rounds

                // Check if the opponent has defected in the last three rounds
                if (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1 && oppHistory1[n - 3] == 1) {
                    return forgiveWithProbability(); // Defect if opponent has defected in the last three rounds
                }
                if (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1 && oppHistory2[n - 3] == 1) {
                    return forgiveWithProbability(); // Defect if opponent has defected in the last three rounds
                }

                return 0; // Cooperate otherwise
            }

            // Helper method to decide whether to forgive with a certain probability
            int forgiveWithProbability() {
                double forgivenessProbability = 0.2; // Adjust this value as desired
                Random random = new Random();
                if (random.nextDouble() < forgivenessProbability) {
                    return 0; // Cooperate with forgivenessProbability
                } else {
                    return 1; // Defect with (1 - forgivenessProbability)
                }
            }
        }

        public class AdaptiveStrategistPlayer extends Player {
            private int defectCounter = 0;

            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                // Adapts based on the number of defects observed
                if (n == 0)
                    return 0; // Start by cooperating
                defectCounter += (oppHistory1[n - 1] + oppHistory2[n - 1] == 2) ? 1 : 0;
                if (defectCounter > 3)
                    return 1; // Start defecting if too many defects observed
                return 0; // Otherwise, keep cooperating
            }
        }

        /*
         * In our tournament, each pair of strategies will play one match against each
         * other.
         * This procedure simulates a single match and returns the scores.
         */
        float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
            int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
            float ScoreA = 0, ScoreB = 0, ScoreC = 0;

            for (int i = 0; i < rounds; i++) {
                int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
                int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
                int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
                ScoreA = ScoreA + payoff[PlayA][PlayB][PlayC];
                ScoreB = ScoreB + payoff[PlayB][PlayC][PlayA];
                ScoreC = ScoreC + payoff[PlayC][PlayA][PlayB];
                HistoryA = extendIntArray(HistoryA, PlayA);
                HistoryB = extendIntArray(HistoryB, PlayB);
                HistoryC = extendIntArray(HistoryC, PlayC);
            }
            float[] result = { ScoreA / rounds, ScoreB / rounds, ScoreC / rounds };
            return result;
        }

        public class AdaptiveStrategist4Player extends Player {
            private int defectCounter = 0;

            int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
                // Adapts based on the number of defects observed
                if (n == 0)
                    return 0; // Start by cooperating
                defectCounter += (oppHistory1[n - 1] + oppHistory2[n - 1] == 2) ? 1 : 0;
                if (defectCounter > 3)
                    return 1; // Start defecting if too many defects observed
                return 0; // Otherwise, keep cooperating
            }
        }

        // This is a helper function needed by scoresOfMatch.
        int[] extendIntArray(int[] arr, int next) {
            int[] result = new int[arr.length + 1];
            for (int i = 0; i < arr.length; i++) {
                result[i] = arr[i];
            }
            result[result.length - 1] = next;
            return result;
        }

        /*
         * The procedure makePlayer is used to reset each of the Players
         * (strategies) in between matches. When you add your own strategy,
         * you will need to add a new entry to makePlayer, and change numPlayers.
         */

        int numPlayers = 7;

        Player makePlayer(int which) {
            switch (which) {
                case 0:
                    return new NicePlayer();
                case 1:
                    return new NastyPlayer();
                case 2:
                    return new RandomPlayer();
                case 3:
                    return new TolerantPlayer();
                case 4:
                    return new FreakyPlayer();
                case 5:
                    return new T4TPlayer();

                case 6:
                    return new Rajkumar_Mukizhitha_Player();
                // case 7: return new Lim_Jiexian_Player();
                // case 8: return new AdaptiveForgiver();
                // case 9: return new ResponsiveTFT();
                // case 10: return new StrategicDefector();
                // case 11: return new CoopMajority();
                // case 12: return new OpportunisticTFT();
                // case 13: return new ScoreAwareCooperator();
                // case 14: return new MutualCooperationTracker();
                // case 15: return new LeadFollow();
                // case 16: return new BalancedTFT();
                // case 17: return new CyclicPlayer();
                // case 18: return new WeightedHistoryAwarePlayer();
                // case 19: return new TitFor3TatsPlayer();
                // case 8: return new TitFor4TatsPlayer();
                // case 21: return new TitFor3TatsPlayerRandom();
                // case 22: return new ForgivingTitFor3TatsPlayer();
                // case 23: return new AdaptiveStrategistPlayer();
                // case 6: return new AdaptiveStrategist4Player();

            }
            throw new RuntimeException("Bad argument passed to makePlayer");
        }

        /* Finally, the remaining code actually runs the tournament. */

        public static void main(String[] args) {
            ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
            instance.runTournament();
        }

        boolean verbose = true; // set verbose = false if you get too much text output

        void runTournament() {
            float[] totalScore = new float[numPlayers];

            // This loop plays each triple of players against each other.
            // Note that we include duplicates: two copies of your strategy will play once
            // against each other strategy, and three copies of your strategy will play
            // once.

            for (int i = 0; i < numPlayers; i++)
                for (int j = i; j < numPlayers; j++)
                    for (int k = j; k < numPlayers; k++) {

                        Player A = makePlayer(i); // Create a fresh copy of each player
                        Player B = makePlayer(j);
                        Player C = makePlayer(k);
                        int rounds = 90 + (int) Math.rint(20 * Math.random()); // Between 90 and 110 rounds
                        float[] matchResults = scoresOfMatch(A, B, C, rounds); // Run match
                        totalScore[i] = totalScore[i] + matchResults[0];
                        totalScore[j] = totalScore[j] + matchResults[1];
                        totalScore[k] = totalScore[k] + matchResults[2];
                        if (verbose)
                            System.out.println(A.name() + " scored " + matchResults[0] +
                                    " points, " + B.name() + " scored " + matchResults[1] +
                                    " points, and " + C.name() + " scored " + matchResults[2] + " points.");
                    }
            int[] sortedOrder = new int[numPlayers];
            // This loop sorts the players by their score.
            for (int i = 0; i < numPlayers; i++) {
                int j = i - 1;
                for (; j >= 0; j--) {
                    if (totalScore[i] > totalScore[sortedOrder[j]])
                        sortedOrder[j + 1] = sortedOrder[j];
                    else
                        break;
                }
                sortedOrder[j + 1] = i;
            }

            // Finally, print out the sorted results.
            if (verbose)
                System.out.println();
            System.out.println("Tournament Results");
            for (int i = 0; i < numPlayers; i++)
                System.out.println(makePlayer(sortedOrder[i]).name() + ": "
                        + totalScore[sortedOrder[i]] + " points.");

        } // end of runTournament()
    }

}// end of class PrisonersDilemma

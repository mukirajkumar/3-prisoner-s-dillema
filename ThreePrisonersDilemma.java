import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ThreePrisonersDilemma {
	
	/* 
	 This Java program models the two-player Prisoner's Dilemma game.
	 We use the integer "0" to represent cooperation, and "1" to represent 
	 defection. 
	 
	 Recall that in the 2-players dilemma, U(DC) > U(CC) > U(DD) > U(CD), where
	 we give the payoff for the first player in the list. We want the three-player game 
	 to resemble the 2-player game whenever one player's response is fixed, and we
	 also want symmetry, so U(CCD) = U(CDC) etc. This gives the unique ordering
	 
	 U(DCC) > U(CCC) > U(DDC) > U(CDC) > U(DDD) > U(CDD)
	 
	 The payoffs for player 1 are given by the following matrix: */
	
	static int[][][] payoff = {  
		{{6,3},  //payoffs when first and second players cooperate 
		 {3,0}}, //payoffs when first player coops, second defects
		{{8,5},  //payoffs when first player defects, second coops
	     {5,2}}};//payoffs when first and second players defect
	
	/* 
	 So payoff[i][j][k] represents the payoff to player 1 when the first
	 player's action is i, the second player's action is j, and the
	 third player's action is k.
	 
	 In this simulation, triples of players will play each other repeatedly in a
	 'match'. A match consists of about 100 rounds, and your score from that match
	 is the average of the payoffs from each round of that match. For each round, your
	 strategy is given a list of the previous plays (so you can remember what your 
	 opponent did) and must compute the next action.  */
	
	
	abstract class Player {
		// This procedure takes in the number of rounds elapsed so far (n), and 
		// the previous plays in the match, and returns the appropriate action.
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			throw new RuntimeException("You need to override the selectAction method.");
		}
		
		// Used to extract the name of this player class.
		final String name() {
			String result = getClass().getName();
			return result.substring(result.indexOf('$')+1);
		}
	}
	
	/* Here are four simple strategies: */
	
	class NicePlayer extends Player {
		//NicePlayer always cooperates
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 0; 
		}
	}
	
	class NastyPlayer extends Player {
		//NastyPlayer always defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 1; 
		}
	}
	
	class RandomPlayer extends Player {
		//RandomPlayer randomly picks his action each time
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (Math.random() < 0.5)
				return 0;  //cooperates half the time
			else
				return 1;  //defects half the time
		}
	}
	
	class TolerantPlayer extends Player {
		//TolerantPlayer looks at his opponents' histories, and only defects
		//if at least half of the other players' actions have been defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponentCoop = 0;
			int opponentDefect = 0;
			for (int i=0; i<n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			for (int i=0; i<n; i++) {
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
		//FreakyPlayer determines, at the start of the match, 
		//either to always be nice or always be nasty. 
		//Note that this class has a non-trivial constructor.
		int action;
		FreakyPlayer() {
			if (Math.random() < 0.5)
				action = 0;  //cooperates half the time
			else
				action = 1;  //defects half the time
		}
		
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return action;
		}	
	}

	class T4TPlayer extends Player {
		//Picks a random opponent at each play, 
		//and uses the 'tit-for-tat' strategy against them 
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0; //cooperate by default
			if (Math.random() < 0.5)
				return oppHistory1[n-1];
			else
				return oppHistory2[n-1];
		}	
	}

    class GTfTPlayer extends Player {
        // GTfTPlayer starts by cooperating, then mostly mimics the opponent's last action.
        // Occasionally, it cooperates despite the opponent's defection.
        
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) {
                return 0; // Always cooperate in the first round
            } else {
                // Calculate the last actions of both opponents
                int lastAction1 = oppHistory1[n-1];
                int lastAction2 = oppHistory2[n-1];
                
                // Determine if both opponents cooperated in the last round
                if (lastAction1 == 0 && lastAction2 == 0) {
                    return 0; // Cooperate if both opponents cooperated
                } else {
                    // Implement the generosity: occasionally cooperate even if one or both defected
                    double chance = Math.random();
                    if (chance < 0.1) { // 10% chance to cooperate regardless of last actions
                        return 0;
                    } else {
                        // Mimic the action of the opponent who defected, if any
                        if (lastAction1 == 1 && lastAction2 == 1) {
                            return 1; // Defect if both defected
                        } else if (lastAction1 == 1) {
                            return 1; // Mimic defection of the first opponent
                        } else {
                            return 1; // Mimic defection of the second opponent (or cooperate if both did)
                        }
                    }
                }
            }
        }
    }

	class PavlovPlayer extends Player {
		// PavlovPlayer starts by cooperating.
		private int lastAction = 0; // 0 for cooperate, 1 for defect
		private int lastScore = 0; // Track the score from the last round
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// For the first round, cooperate
			if (n == 0) {
				lastAction = 0;
				return lastAction;
			}
	
			// Determine the threshold for a win. This is a simplification and might need adjustment.
			int winThreshold = 3; // This is arbitrary and depends on your interpretation of the payoff matrix
	
			// Calculate the score from the last round
			int myLastScore = calculatePayoff(myHistory[n-1], oppHistory1[n-1], oppHistory2[n-1]);
	
			// Win-Stay: If last score was above the threshold, repeat the last action
			// Lose-Shift: If last score was not a "win", switch the action
			if (myLastScore <= winThreshold) {
				lastAction = 1 - lastAction; // Switch action
			}
			// Otherwise, keep lastAction as is (Win-Stay)
	
			lastScore = myLastScore; // Update the last score
			return lastAction;
		}
	
		private int calculatePayoff(int myAction, int oppAction1, int oppAction2) {
			// Accessing the correct payoff for player 1 based on actions.
			return payoff[myAction][oppAction1][oppAction2];
		}
		
	}
	
	class AdaptiveTFTPlayer extends Player {
		// AdaptiveTFTPlayer starts by cooperating
		private int lastAction = 0; // 0 for cooperate, 1 for defect
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// For the first round, cooperate
			if (n == 0) {
				return 0;
			}
			
			// Assess the majority action of the opponents in the previous round
			int oppMajorityAction = majorityAction(oppHistory1[n-1], oppHistory2[n-1]);
			
			// Adapt to the majority action: cooperate if the majority cooperated, defect otherwise
			lastAction = oppMajorityAction;
			return lastAction;
		}
		
		// Helper method to determine the majority action between two opponents
		private int majorityAction(int action1, int action2) {
			// If both opponents took the same action, that's the majority action
			if (action1 == action2) {
				return action1;
			}
			
			// If there's a tie, the strategy decides based on its last action
			// This could be enhanced with more sophisticated tie-breaking logic
			return lastAction;
		}
	}
	
	class ConditionalLeaderPlayer extends Player {
		// ConditionalLeaderPlayer initially cooperates and then adapts based on opponents' actions.
		private int lastAction = 0; // Start with cooperation (0 for cooperate, 1 for defect)
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				// Always cooperate in the first round
				return 0;
			} else {
				// Assess the overall level of cooperation vs. defection from opponents
				int cooperationLevel = assessCooperation(oppHistory1, oppHistory2, n);
				
				// Adapt strategy based on the level of cooperation observed
				if (cooperationLevel > n * 0.5) { // More than 50% cooperation overall
					// Continue leading with cooperation if the majority is cooperating
					lastAction = 0;
				} else {
					// Switch to defection if there's more defection or if cooperation doesn't seem to be reciprocated
					lastAction = 1;
				}
				return lastAction;
			}
		}
	
		// Helper method to assess the level of cooperation in the opponents' history
		private int assessCooperation(int[] oppHistory1, int[] oppHistory2, int n) {
			int totalCooperation = 0;
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0) totalCooperation++; // Count cooperation instances
				if (oppHistory2[i] == 0) totalCooperation++;
			}
			return totalCooperation;
		}
	}
	
    class ProfileBasedAdaptivePlayer extends Player {
		private int[] opp1CooperationProfile;
		private int[] opp2CooperationProfile;
	
		// Initialize profiles in the constructor
		ProfileBasedAdaptivePlayer() {
			opp1CooperationProfile = new int[2]; // Index 0 for cooperation count, Index 1 for defection count
			opp2CooperationProfile = new int[2];
		}
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				// Cooperate in the first round
				return 0;
			} else {
				// Update profiles based on the last round
				updateProfile(opp1CooperationProfile, oppHistory1[n-1]);
				updateProfile(opp2CooperationProfile, oppHistory2[n-1]);
				
				// Decide next action based on opponents' profiles
				if (shouldCooperate(opp1CooperationProfile, opp2CooperationProfile)) {
					return 0; // Cooperate if profiles suggest it's beneficial
				} else {
					return 1; // Defect otherwise
				}
			}
		}
	
		private void updateProfile(int[] profile, int lastAction) {
			profile[lastAction]++; // Increment cooperation or defection count based on the action
		}
	
		private boolean shouldCooperate(int[] profile1, int[] profile2) {
			// Simplified decision-making: cooperate if both opponents have cooperated more than they defected
			return profile1[0] > profile1[1] && profile2[0] > profile2[1];
		}
	}
	
	class GrudgerPlayer extends Player {
		private boolean hasBeenBetrayed = false; // Flag to track if the Grudger has been defected against
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				return 0; // Always cooperate in the first round
			}
	
			// If Grudger has not been betrayed yet, check for defection from opponents
			if (!hasBeenBetrayed) {
				for (int i = 0; i < n; i++) {
					if (oppHistory1[i] == 1 || oppHistory2[i] == 1) {
						hasBeenBetrayed = true; // Opponent has defected, switch to defection permanently
						break;
					}
				}
			}
	
			// If Grudger has been betrayed, always defect
			if (hasBeenBetrayed) {
				return 1;
			} else {
				return 0; // Continue cooperating if no betrayal has occurred
			}
		}
	}
	
	class RandomTFTPlayer extends Player {
		// RandomTFTPlayer starts by cooperating.
		private int lastAction = 0; // 0 for cooperate, 1 for defect
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// For the first round, always cooperate
			if (n == 0) {
				return 0;
			}
	
			// Implement the randomness
			double chance = Math.random();
			if (chance < 0.05) { // 5% chance to randomly defect or cooperate
				lastAction = Math.random() < 0.5 ? 0 : 1;
			} else if (chance < 0.10) { // Additional 5% chance, total of 10% for random action
				lastAction = Math.random() < 0.5 ? 0 : 1;
			} else {
				// Mimic the last action of a randomly chosen opponent
				if (Math.random() < 0.5) {
					lastAction = oppHistory1[n-1];
				} else {
					lastAction = oppHistory2[n-1];
				}
			}
			
			return lastAction;
		}
	}
	
	class MirrorMajorityPlayer extends Player {
		// MirrorMajorityPlayer starts by cooperating.
		private int lastAction = 0; // 0 for cooperate, 1 for defect
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				return 0; // Always cooperate in the first round
			} else {
				// Assess the majority action of the opponents in the previous round
				int majorityAction = determineMajorityAction(oppHistory1[n-1], oppHistory2[n-1]);
				
				if (majorityAction == -1) {
					// If there's a tie, continue with the last action
					return lastAction;
				} else {
					// Mirror the majority action
					lastAction = majorityAction;
					return lastAction;
				}
			}
		}
		
		// Helper method to determine the majority action, returns -1 if there's a tie
		private int determineMajorityAction(int opp1Action, int opp2Action) {
			if (opp1Action == opp2Action) {
				return opp1Action; // Both opponents took the same action, so return this as the majority
			} else {
				// There's a tie, return -1 to indicate that the player should stick with its last action
				return -1;
			}
		}
	}
	
	class ForgivingTFTPlayer extends Player {
		private int lastAction = 0; // 0 for cooperate, 1 for defect
		private boolean readyToForgive = false; // Flag to indicate willingness to forgive
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				return 0; // Always cooperate in the first round
			}
			
			// Check if the player was defected against in the last round
			if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) {
				if (readyToForgive) {
					readyToForgive = false; // Reset forgiveness
					return 0; // Cooperate as a sign of forgiveness
				} else {
					readyToForgive = true; // Prepare to forgive next round
					return 1; // Defect this round
				}
			}
			
			readyToForgive = false; // No need to forgive if not defected against
			lastAction = oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0 ? 0 : 1;
			return lastAction; // Mimic the majority action
		}
	}
	
	class MajorityRulePlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				return 0; // Cooperate on the first turn
			}
			
			// Assess the majority action in the previous round
			int actionSum = oppHistory1[n-1] + oppHistory2[n-1];
			
			if (actionSum == 1) { // If there's a tie
				// Randomly decide to cooperate or defect
				return Math.random() < 0.5 ? 0 : 1;
			} else if (actionSum == 2) { // If both defected
				return 1;
			} else { // If both cooperated or one cooperated and one defected, leaning towards cooperation
				return 0;
			}
		}
	}
	
	class OpportunisticAllyPlayer extends Player {
		private int lastAction = 0; // Start by cooperating
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				return 0; // Cooperate in the first round
			}
			
			// Identify the "weaker" (more cooperative) opponent
			int opp1Cooperations = 0, opp2Cooperations = 0;
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0) opp1Cooperations++;
				if (oppHistory2[i] == 0) opp2Cooperations++;
			}
			
			// Side with the more cooperative opponent
			if (opp1Cooperations > opp2Cooperations) {
				lastAction = oppHistory1[n-1];
			} else if (opp2Cooperations > opp1Cooperations) {
				lastAction = oppHistory2[n-1];
			} else {
				// If both are equally cooperative/aggressive, default to Tit-for-Tat with a random opponent
				lastAction = Math.random() < 0.5 ? oppHistory1[n-1] : oppHistory2[n-1];
			}
			
			return lastAction;
		}
	}
	
	class DetectivePlayer extends Player {
		private boolean inTestPhase = true;
		private int[] testSequence = new int[]{0, 1, 0, 0}; // C, D, C, C sequence
		private int testIndex = 0;
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < testSequence.length) {
				return testSequence[n]; // Follow the test sequence initially
			} else {
				inTestPhase = false; // End of test phase
			}
	
			// Analyze opponents' responses to the test sequence to decide on strategy
			if (!inTestPhase) {
				// Simple example strategy after test phase based on opponents' actions
				if (oppHistory1[n-1] == 1 && oppHistory2[n-1] == 1) { // If both defected last round
					return 1; // Defect
				}
				return 0; // Otherwise, cooperate
			}
			return 0; // Default action
		}
	}
	
	class AdaptiveMirrorPlayer extends Player {
		private int score = 0; // Track own score to compare against opponents
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate in the first round
			
			// Adaptive mirroring based on opponents' score, simplified version
			// Here, simply follow the more successful opponent's last move, assuming equal initial scores
			int opp1Score = calculateTotalScore(oppHistory1);
			int opp2Score = calculateTotalScore(oppHistory2);
			
			if (opp1Score > opp2Score) {
				return oppHistory1[n-1];
			} else {
				return oppHistory2[n-1];
			}
		}
		
		private int calculateTotalScore(int[] history) {
			// Simplified scoring assuming a win (cooperation) adds 1, a loss (defection) subtracts 1
			int score = 0;
			for (int action : history) {
				score += action == 0 ? 1 : -1;
			}
			return score;
		}
	}
	
	class ContrarianPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate initially
			
			// Determine the majority action of the last round and do the opposite
			int lastRoundCooperation = (oppHistory1[n-1] == 0 ? 1 : 0) + (oppHistory2[n-1] == 0 ? 1 : 0);
			return lastRoundCooperation >= 1 ? 1 : 0; // If majority cooperated (>=1), defect; otherwise, cooperate
		}
	}
	
	class ReflectiveTFTPlayer extends Player {
		private int lastMyAction = 0; // Start by cooperating
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				return 0; // Cooperate in the first round
			}
	
			// Reflect on own last action to decide the current action
			if (lastMyAction == 1) { // If I defected last round, cooperate this round
				lastMyAction = 0;
				return 0;
			} else { // If I cooperated last round, mirror the opponent's last action
				int oppLastAction1 = oppHistory1[n-1];
				int oppLastAction2 = oppHistory2[n-1];
				// Decide based on a mix or random choice if opponents' actions were different
				if (oppLastAction1 != oppLastAction2) {
					lastMyAction = Math.random() < 0.5 ? 0 : 1;
					return lastMyAction;
				} else {
					lastMyAction = oppLastAction1; // Mirror if both opponents took the same action
					return lastMyAction;
				}
			}
		}
	}
	
	class PredictiveTFTPlayer extends Player {
		private int lastAction = 0; // Start by cooperating
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate on the first turn
			
			// Simple prediction based on the last two actions of the opponent
			if (n > 2 && oppHistory1[n-1] == oppHistory1[n-3] && oppHistory2[n-1] == oppHistory2[n-3]) {
				// Predicts opponent will continue the pattern
				lastAction = oppHistory1[n-2]; // Chooses the action that opponents did two turns ago, anticipating repetition
			} else {
				// Fallback to standard TfT if no pattern is detected
				lastAction = oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0 ? 0 : 1;
			}
			
			return lastAction;
		}
	}

	class LearningTFTPlayer extends Player {
		private int cooperationScore = 0;
		private int defectionScore = 0;
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate on the first turn
	
			// Update scores based on last round's payoff
			int myLastAction = myHistory[n-1];
			int payoff = calculatePayoff(myLastAction, oppHistory1[n-1], oppHistory2[n-1]);
			if (myLastAction == 0) {
				cooperationScore += payoff;
			} else {
				defectionScore += payoff;
			}
	
			// Decide action based on historical success
			if (cooperationScore > defectionScore) {
				return 0; // Prefer cooperation if it's been more successful
			} else if (defectionScore > cooperationScore) {
				return 1; // Prefer defection if it's been more successful
			} else {
				// If both strategies are equally successful, fall back to standard TfT
				return oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0 ? 0 : 1;
			}
		}
		
		// Stub for calculating payoff, implement according to your game's logic
		private int calculatePayoff(int myAction, int oppAction1, int oppAction2) {
			// Return the payoff for the player's action based on the game's payoff matrix
			return payoff[myAction][oppAction1][oppAction2];
		}
	}
	
	class HistoryAwareTFTPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate on the first turn
			
			// Calculate the majority action across all rounds
			int coopCount = 0;
			for (int i = 0; i < n; i++) {
				coopCount += (oppHistory1[i] == 0 ? 1 : 0) + (oppHistory2[i] == 0 ? 1 : 0);
			}
			// Decide based on the majority of historical actions
			return (coopCount >= n) ? 0 : 1;
		}
	}

	class HistoryAwareTFTPlayerRandom extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate on the first turn
			
			// Calculate the majority action across all rounds
			int coopCount = 0;
			for (int i = 0; i < n; i++) {
				coopCount += (oppHistory1[i] == 0 ? 1 : 0) + (oppHistory2[i] == 0 ? 1 : 0);
			}
			// Initially decide based on the majority of historical actions
			int decision = (coopCount >= n) ? 0 : 1;
	
			// If the decision is to cooperate, introduce a 1% chance to randomize the decision
			if (decision == 0 && Math.random() < 0.2) {
				decision = (Math.random() < 0.5) ? 0 : 1; // 50-50 chance to cooperate or defect
			}
	
			return decision;
		}
	}
	
	class TitForTwoTatsPlayer extends Player {
		private int lastOpp1Action = 0;
		private int lastOpp2Action = 0;
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 2) return 0; // Cooperate for the first two turns
			
			// Defect only if an opponent has defected in the last two consecutive rounds
			boolean defectOpp1 = oppHistory1[n-1] == 1 && oppHistory1[n-2] == 1;
			boolean defectOpp2 = oppHistory2[n-1] == 1 && oppHistory2[n-2] == 1;
			
			return (defectOpp1 || defectOpp2) ? 1 : 0;
		}
	}
	
	class DynamicTFTPlayer extends Player {
		private int lastAction = 0; // Start by cooperating
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate on the first turn
	
			double randomFactor = Math.random();
			// Occasionally respond to defection with cooperation
			if ((oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) && randomFactor < 0.1) {
				return 0; // Cooperate despite defection
			}
			// Occasionally defect in response to cooperation to test the waters
			else if ((oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0) && randomFactor < 0.1) {
				return 1; // Defect despite cooperation
			}
			// Otherwise, follow the classic TfT strategy
			else {
				return (oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0) ? 0 : 1;
			}
		}
	}
	
	class CoalitionBuilderPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Start by cooperating
			
			// Identify the most cooperative player so far
			int coopCount1 = 0, coopCount2 = 0;
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0) coopCount1++;
				if (oppHistory2[i] == 0) coopCount2++;
			}
			
			// Decide to cooperate with the more cooperative player
			if (coopCount1 > coopCount2) {
				// If opp1 is more cooperative, mimic their last action and cooperate with them
				return oppHistory1[n-1];
			} else if (coopCount2 > coopCount1) {
				// If opp2 is more cooperative, mimic their last action and cooperate with them
				return oppHistory2[n-1];
			} else {
				// If both are equally cooperative, or in the case of initial rounds, cooperate
				return 0;
			}
		}
	}

	class QuasiRandomCDPlayer extends Player {
		private double cooperationProbability = 0.5; // Initial base probability of cooperating
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// Dynamically adjust cooperationProbability based on the history of opponents' actions
			if (n > 0) { // Ensure there is history to analyze
				int totalCooperations = 0;
				for (int i = 0; i < n; i++) {
					if (oppHistory1[i] == 0) totalCooperations++;
					if (oppHistory2[i] == 0) totalCooperations++;
				}
				double cooperationRate = (double) totalCooperations / (2 * n); // Calculate the rate of cooperation
	
				// Adjust the probability of cooperation based on the cooperation rate
				// This example makes the strategy more likely to cooperate if opponents have been mostly cooperative
				cooperationProbability = 0.3 + (cooperationRate * 0.7); // Base 30% + up to 70% based on cooperation rate
			}
	
			// Occasionally introduces unexpected moves to probe opponents based on the adjusted probability
			if (Math.random() < cooperationProbability) {
				return 0; // Cooperate
			} else {
				return 1; // Defect
			}
		}
	}
	
	class PatternRecognitionAgent extends Player {
		// Thresholds to classify behavior
		private final int minRoundsForAnalysis = 5;
		private double cooperationThreshold = 0.7;
		private double defectionThreshold = 0.7;
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < minRoundsForAnalysis) {
				return 0; // Cooperate initially to gather data
			}
	
			// Analyze patterns and classify opponents
			String opp1Type = classifyOpponent(oppHistory1, n);
			String opp2Type = classifyOpponent(oppHistory2, n);
	
			// Devise counter-strategies
			if ("cooperative".equals(opp1Type) && "cooperative".equals(opp2Type)) {
				return 0; // Continue cooperating if both are cooperative
			} else if ("retaliatory".equals(opp1Type) || "retaliatory".equals(opp2Type)) {
				// Mimic tit-for-tat against retaliatory opponents
				return oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0 ? 0 : 1;
			} else if ("random".equals(opp1Type) || "random".equals(opp2Type)) {
				// Continue with a cooperative stance against random behavior to encourage cooperation
				return 0;
			} else {
				// Default to defection if there's any sign of exploitative behavior
				return 1;
			}
		}
	
		private String classifyOpponent(int[] oppHistory, int n) {
			int cooperationCount = 0;
			for (int action : oppHistory) {
				if (action == 0) cooperationCount++;
			}
			double cooperationRate = (double) cooperationCount / n;
	
			if (cooperationRate > cooperationThreshold) {
				return "cooperative";
			} else if (1 - cooperationRate > defectionThreshold) {
				return "exploitative";
			} else {
				// Check for tit-for-tat or random behavior heuristically (simplified)
				boolean isRetaliatory = true;
				for (int i = 1; i < oppHistory.length; i++) {
					if (oppHistory[i] != oppHistory[i-1]) {
						isRetaliatory = false;
						break;
					}
				}
				if (isRetaliatory) {
					return "retaliatory";
				} else {
					return "random";
				}
			}
		}
	}
	
	class AdaptivePlayer extends Player {
        // AdaptivePlayer starts by cooperating and adapts its strategy based on the behavior of its opponents.
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int cooperateThreshold = 60; // Threshold of cooperation percentage to decide whether to cooperate or defect.
            int myAction = 0; // Default action is to cooperate.
            if (n > 0) { // After the first round, adapt strategy based on opponents' history.
                double opp1CoopRate = calculateCooperationRate(oppHistory1, n);
                double opp2CoopRate = calculateCooperationRate(oppHistory2, n);
                // Defect if the cooperation rate of either opponent is below the threshold.
                if (opp1CoopRate < cooperateThreshold || opp2CoopRate < cooperateThreshold) {
                    myAction = 1; // Defect
                } else {
                    myAction = 0; // Cooperate
                }
                // Introduce a small chance of random behavior to avoid predictable patterns.
                if (Math.random() < 0.05) {
                    myAction = (myAction == 0) ? 1 : 0; // Switch action randomly.
                }
            }
            return myAction;
        }

        // Helper method to calculate the cooperation rate of an opponent.
        double calculateCooperationRate(int[] oppHistory, int n) {
            int coopCount = 0;
            for (int i = 0; i < n; i++) {
                if (oppHistory[i] == 0) { // Count cooperation instances.
                    coopCount++;
                }
            }
            return ((double) coopCount / n) * 100; // Return cooperation rate as a percentage.
        }
    }

    class CautiousPlayer extends Player {
        int consecutiveCooperationsRequired = 2;
        int currentConsecutiveCooperations = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) {
                return 0; // Always start by cooperating.
            }

            // Check if both opponents cooperated in the last round.
            boolean bothCooperated = oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0;

            if (bothCooperated) {
                currentConsecutiveCooperations++;
            } else {
                currentConsecutiveCooperations = 0; // Reset if any opponent defected.
            }

            // Decide to cooperate only if the opponents have cooperated consecutively
            // the required number of times after a defection.
            if (currentConsecutiveCooperations >= consecutiveCooperationsRequired) {
                return 0; // Cooperate
            } else {
                return 1; // Defect
            }
        }
    }
    
	class OpportunistPlayer extends Player {
        double defectionThreshold = 0.5; // Threshold to switch to cooperation if defections exceed this rate.

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) {
                return 1; // Start by defecting to test opponents.
            }

            int totalDefections = 0;
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 1) totalDefections++;
                if (oppHistory2[i] == 1) totalDefections++;
            }

            double defectionRate = (double)totalDefections / (2 * n);

            if (defectionRate > defectionThreshold) {
                return 0; // Switch to cooperation if facing too much defection.
            } else {
                return 1; // Continue defecting otherwise.
            }
        }
    }

    class AnalyzerPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) {
                return 0; // Always start by cooperating.
            } else if (n == 1) {
                // In the second round, defect to gauge reactions.
                return 1;
            } else {
                // Analyze the last few rounds for patterns; this is a simple pattern analysis.
                if (oppHistory1[n-1] == oppHistory1[n-2] && oppHistory2[n-1] == oppHistory2[n-2]) {
                    // If both opponents repeated their last action, mirror the most common action.
                    return (oppHistory1[n-1] + oppHistory2[n-1]) >= 1 ? 1 : 0;
                } else {
                    // If no clear pattern, cooperate as a default strategy.
                    return 0;
                }
            }
        }
    }

    class BalancerPlayer extends Player {
        int totalMoves = 0;
        int totalCooperations = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            totalMoves = n;
            for (int move : myHistory) {
                if (move == 0) {
                    totalCooperations++;
                }
            }

            if (n == 0) {
                return 0; // Start by cooperating.
            } else {
                double cooperationRate = (double)totalCooperations / totalMoves;
                if (cooperationRate > 0.5) {
                    return 1; // If cooperating more than defecting, defect to balance.
                } else {
                    return 0; // Otherwise, cooperate to maintain balance.
                }
            }
        }
    }

    class RandomTFTPlayerKEL extends Player {
        double randomnessFactor = 0.1; // Chance to perform a random action instead of Tit-for-Tat.

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) {
                return 0; // Start by cooperating.
            } else {
                // Occasionally choose a random action.
                if (Math.random() < randomnessFactor) {
                    return Math.random() < 0.5 ? 0 : 1;
                }
                // Otherwise, mimic the last action of a randomly chosen opponent.
                return Math.random() < 0.5 ? oppHistory1[n-1] : oppHistory2[n-1];
            }
        }
    }

    class VengefulPlayer extends Player {
        boolean isVengeful = false;
        int vengeanceCounter = 0;
        int vengeancePeriod = 3; // Number of rounds to defect after an opponent defects.

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) {
                return 0; // Always start by cooperating.
            }
            if (!isVengeful) {
                if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) {
                    isVengeful = true; // Become vengeful if any opponent defects.
                    vengeanceCounter = vengeancePeriod;
                }
                return 0; // Continue to cooperate if not vengeful.
            } else {
                if (vengeanceCounter > 0) {
                    vengeanceCounter--;
                    return 1; // Defect while vengeful.
                } else {
                    isVengeful = false; // Stop being vengeful after the period ends.
                    return 0; // Return to cooperation.
                }
            }
        }
    }

    class PacifistPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 0; // Always cooperate.
        }
    }

    class AgitatorPlayer extends Player {
        boolean hasDefected = false;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n < 2) {
                hasDefected = true;
                return 1; // Start by defecting.
            } else {
                if (hasDefected && (oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0)) {
                    return 0; // Switch to cooperation if both opponents cooperated last round.
                } else {
                    return 1; // Otherwise, continue defecting.
                }
            }
        }
    }

	class AlternatingTitForTatsPlayer extends Player {
		// private int lastOpp1Action = 0;
		// private int lastOpp2Action = 0;
		// Add a state to track the retaliation threshold, starting with 1 defection.
		private int retaliationThreshold = 1;
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Always cooperate in the first round
	
			// Count consecutive defections for each opponent
			int consecutiveDefectionsOpp1 = 0;
			int consecutiveDefectionsOpp2 = 0;
	
			for (int i = 1; i <= retaliationThreshold && i <= n; i++) {
				if (oppHistory1[n-i] == 1) consecutiveDefectionsOpp1++;
				if (oppHistory2[n-i] == 1) consecutiveDefectionsOpp2++;
			}
	
			// Decide action based on the current retaliation threshold
			boolean shouldDefect = false;
			if (retaliationThreshold == 1) {
				// Retaliate if there's at least 1 defection in the last round
				shouldDefect = oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1;
			} else if (retaliationThreshold == 2) {
				// Retaliate if there are 2 consecutive defections
				shouldDefect = consecutiveDefectionsOpp1 == 2 || consecutiveDefectionsOpp2 == 2;
			}
	
			// If we retaliated, or if opponents cooperated, switch the mode for the next decision
			if (shouldDefect || (!shouldDefect && oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0)) {
				retaliationThreshold = (retaliationThreshold == 1) ? 2 : 1;
			}
	
			return shouldDefect ? 1 : 0;
		}
	}

	class RotatingTitForTatsPlayer extends Player {
		private int cyclePhase = 0; // Tracks which part of the cycle the strategy is in
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Always cooperate in the first round
	
			// Calculate defections in the last two rounds for both opponents
			int recentDefectionsOpp1 = (n >= 1 ? oppHistory1[n-1] : 0) + (n >= 2 ? oppHistory1[n-2] : 0);
			int recentDefectionsOpp2 = (n >= 1 ? oppHistory2[n-1] : 0) + (n >= 2 ? oppHistory2[n-2] : 0);
	
			// Determine if we should retaliate based on the cycle phase
			boolean shouldDefect = false;
			if (cyclePhase < 2) { // First two phases: retaliate after two consecutive defections
				shouldDefect = recentDefectionsOpp1 == 2 || recentDefectionsOpp2 == 2;
			} else if (cyclePhase == 2) { // Third phase: retaliate after a single defection
				shouldDefect = oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1;
			}
	
			// Update the cycle phase for the next round
			cyclePhase = (cyclePhase + 1) % 3; // Cycles through 0, 1, 2, then back to 0
	
			return shouldDefect ? 1 : 0;
		}
	}
	
	class ModifiedRotatingTitForTatsPlayer extends Player {
		private int cyclePhase = 0; // Tracks which part of the cycle the strategy is in
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Always cooperate in the first round
	
			// Initialize variable to check for defections
			boolean shouldDefect = false;
	
			// For the first two phases: retaliate after a single defection
			if (cyclePhase < 2) {
				shouldDefect = oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1;
			}
			// For the third phase: retaliate after two consecutive defections
			else if (cyclePhase == 2) {
				if (n >= 2) { // Ensure there are at least two rounds to check
					boolean defectOpp1 = oppHistory1[n-1] == 1 && oppHistory1[n-2] == 1;
					boolean defectOpp2 = oppHistory2[n-1] == 1 && oppHistory2[n-2] == 1;
					shouldDefect = defectOpp1 || defectOpp2;
				}
			}
	
			// Update the cycle phase for the next round
			cyclePhase = (cyclePhase + 1) % 3; // Cycles through 0, 1, 2, then back to 0
	
			return shouldDefect ? 1 : 0;
		}
	}
	
	class TitForTwoTatsPlayerRandom extends Player {
		private int lastOpp1Action = 0;
		private int lastOpp2Action = 0;
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 2) return 0; // Cooperate for the first two turns
	
			// Defect only if an opponent has defected in the last two consecutive rounds
			boolean defectOpp1 = oppHistory1[n-1] == 1 && oppHistory1[n-2] == 1;
			boolean defectOpp2 = oppHistory2[n-1] == 1 && oppHistory2[n-2] == 1;
	
			int calculatedAction = (defectOpp1 || defectOpp2) ? 1 : 0;
	
			// Introduce a 5% chance to switch the action randomly
			if (calculatedAction == 1 && Math.random() < 0.02) {
				calculatedAction = calculatedAction == 0 ? 1 : 0; // Switch action randomly
			}
	
			return calculatedAction;
		}
	}
	
    class GosuTheMinion extends NicePlayer {

        // For tracking Defect/Cooperate probabilities
        private double opp1Def = 0;
        private double opp2Def = 0;

        // Thresholds
        private static final double FRIENDLY_THRESHOLD = 0.850;
        private static final double DEFENSIVE_THRESHOLD = 0.750;

        /* ALL HAIL KING CHODY!! */
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            // Start by cooperating
            if (n == 0) {

                return 0;
            }

            // Calculate probability for Def/Coop (Opponent 1)
            opp1Def += oppHistory1[n - 1];
            double opp1DefProb = opp1Def / oppHistory1.length;
            double opp1CoopProb = 1.000 - opp1DefProb;

            // Calculate probability for Def/Coop (Opponent 2)
            opp2Def += oppHistory2[n - 1];
            double opp2DefProb = opp2Def / oppHistory2.length;
            double opp2CoopProb = 1.000 - opp2DefProb;

            /*System.out.printf("Opponent 1: %.3f, %.3f, Opponent 2: %.3f, %.3f%n",
					opp1CoopProb, opp1DefProb, opp2CoopProb, opp2DefProb);*/
            if (opp1CoopProb >= FRIENDLY_THRESHOLD
                    && opp2CoopProb >= FRIENDLY_THRESHOLD
                    && oppHistory1[n - 1] == 0
                    && oppHistory2[n - 1] == 0) {

                // Good chance that both opponents will cooperate
                // Just cooperate so that everyone will be happy
                return 0;

            } else if ((opp1DefProb >= DEFENSIVE_THRESHOLD || opp2DefProb >= DEFENSIVE_THRESHOLD)
                    && (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1)) {

                // Given that one of the opponents have been relatively nasty,
                // and one of them has defected in the previous turn,
                // high prob that one of them will defect again,
                // defect to protect myself!
                return 1;

            } else if (n >= 2) {

                // Check if either opponent has defected in the last 2 turns
                if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1
                        || oppHistory1[n - 2] == 1 || oppHistory2[n - 2] == 1) {

                    // DESTROY them!!
                    return 1;
                } else {

                    // Just be friendly!
                    return 0;
                }
            } else {

                // At this moment, both players are not that friendly,
                // and yet neither of them are relatively nasty.
                // Just be friendly for now.
                return 0;
            }
        }
    }

	class PM_Low extends Player {

        int myScore = 0;
        int opp1Score = 0;
        int opp2Score = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            if (n == 0) {
                return 0; // cooperate by default
            }

            // get the recent history index
            int i = n - 1;

            // add up the total score/points for each player
            myScore += payoff[myHistory[i]][oppHistory1[i]][oppHistory2[i]];
            opp1Score += payoff[oppHistory1[i]][oppHistory2[i]][myHistory[i]];
            opp2Score += payoff[oppHistory2[i]][myHistory[i]][oppHistory1[i]];

            // if my score is lower than the any of them
            // it means that at least one of them have defected
            if (myScore >= opp1Score && myScore >= opp2Score) {

                // cooperate if my score is higher or equal than all of them
                return 0;
            }

            return 1; // defect if my score is lower than any of them
        }
    }

	class Bummer extends NastyPlayer {

		//Count the number of defects by opp
		int intPlayer1Defects = 0;
		int intPlayer2Defects = 0;

		//Store the round where agent retaliate against defects
		int intRoundRetailate = -1;

		//Number of rounds where agent coop to observer opp actions
		int intObservationRound = 1;

		//Number of rounds where agent retaliate defects with defects
		//After this round, see opp actions to check if they decide to coop again
		int intGrudgeRound = 3;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			//Record Defects count
			if (n > 0) {
				intPlayer1Defects += oppHistory1[n - 1];
				intPlayer2Defects += oppHistory2[n - 1];
			}

			//Start by cooperating
			if (n < intObservationRound) {
				return 0; //cooperate by default
			}

			//Loop rounds where agent coop to reverse the effects of retaliation
			if (intRoundRetailate < -1) {
				intRoundRetailate += 1;
				intPlayer1Defects = 0;
				intPlayer2Defects = 0;

				return 0;
			}

			//Check at round retaliated + threshold to measure if opp wishes to coop again
			if (intRoundRetailate > -1 && n == intRoundRetailate + intGrudgeRound + 1) {

				//Count the number of coop during retaliate round to check opp coop level
				int intPlayer1Coop = 0;
				int intPlayer2Coop = 0;

				for (int intCount = 0; intCount < intGrudgeRound; intCount++) {
					intPlayer1Coop += oppHistory1[n - 1 - intCount] == 0 ? 1 : 0;
					intPlayer2Coop += oppHistory2[n - 1 - intCount] == 0 ? 1 : 0;
					//intPlayer1Coop += oppHistory1[n - 1 - intCount] == 1 ? 1 : 0;
					//intPlayer2Coop += oppHistory2[n - 1 - intCount] == 1 ? 1 : 0;
				}

				//If both players wish to coop again, start to coop with them
				if (intPlayer1Coop > 1 && intPlayer2Coop > 1 && (oppHistory1[n - 1] + oppHistory2[n - 1]) == 0) {
					//Hold round where agent coop to show intention to coop again
					//Count backwards from -2
					//-2 indicates 1 round where agent coop to reverse effect of retailation
					//-5 indicates 4 rounds where agent coop to reverse effect
					intRoundRetailate = -2;

					intPlayer1Defects = 0;
					intPlayer2Defects = 0;

					return 0;
				} else {
					intRoundRetailate = n;
					return 1;
				}

			}

			//Punish Defection by defecting straight away
			//Stores the round defected
			if (intPlayer1Defects + intPlayer2Defects > 0) {
				intRoundRetailate = n;
				return 1;
			}

			//Coop as default action
			return 0;
		}
	}

	class Huang_KyleJunyuan_Player extends Player {
		// Helper function to calculate percentage of cooperation
		float calCoopPercentage(int[] history) {
			int cooperates = 0;
			int length = history.length;
	
			for (int i = 0; i < length; i++)
				if (history[i] == 0)
					cooperates++;
	
			return (float) cooperates / length * 100;
		}
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0)
				return 0; // First round: Cooperate
	
			/* 1. Calculate percentage of cooperation */
			float perOpp1Coop = calCoopPercentage(oppHistory1);
			float perOpp2Coop = calCoopPercentage(oppHistory2);
	
			/* 2. If both players are mostly cooperating */
			if (perOpp1Coop > 90 && perOpp2Coop > 90) {
				int range = (10 - 5) + 1; // Max: 10, Min: 5
				int random = (int) (Math.random() * range) + 5;
				
				if (n > (90 + random))  // Selfish: Last min defect
					return 1;
				else
					return 0;	// First ~90 rounds: Cooperate
			}
	
			/* 3. Defect by default */
			return 1;
		}
	}

	class Ngo_Jason_Player extends Player { // extends Player
			int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
				if (n == 0)
					return 0; // cooperate by default
		
				if (n >= 109)
					return 1; // opponents cannot retaliate
		
				// https://www.sciencedirect.com/science/article/abs/pii/S0096300316301011
				if (oppHistory1[n-1] == oppHistory2[n-1])
					return oppHistory1[n-1];
		
				// n starts at 0, so compare history first
		
				if (n % 2 != 0) { // odd round - be tolerant
					// TolerantPlayer
					int opponentCoop = 0;
					int opponentDefect = 0;
		
					for (int i = 0; i < n; i++) {
						if (oppHistory1[i] == 0)
							opponentCoop += 1;
						else
							opponentDefect += 1;
		
						if (oppHistory2[i] == 0)
							opponentCoop += 1;
						else
							opponentDefect += 1;
					}
		
					return (opponentDefect > opponentCoop) ? 1 : 0;
				}
				// else: even round - compare history
				
				// HistoryPlayer
				int myNumDefections = 0;
				int oppNumDefections1 = 0;
				int oppNumDefections2 = 0;
		
				for (int index = 0; index < n; ++index) {
					myNumDefections += myHistory[index];
					oppNumDefections1 += oppHistory1[index];
					oppNumDefections2 += oppHistory2[index];
				}
		
				if (myNumDefections >= oppNumDefections1 && myNumDefections >= oppNumDefections2)
					return 0;
				else
					return 1;
			}
		}

	class LessTolerantPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// cooperate by default
			if (n == 0)
				return 0;

			// TolerantPlayer
			int opponentCoop = 0;
			int opponentDefect = 0;

			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;

				if (oppHistory2[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;
			}

			return (opponentDefect >= opponentCoop) ? 1 : 0;
		}
	}		
	
	class Teo_WeiJie_Player extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// First round - be nice, always cooperate.
			if (n == 0) return 0;
	
			// If any agent defected in the previous round,
			// punish immediately by defecting.
			if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) return 1;
	
			// Calculate the trustworthiness of each agent based on defection rate.
			// Trustworthy --> rate < 5%
			// Untrustworthy --> rate >= 20%
			// Neutral --> in between
			// Defection has a heavier emphasis than cooperation.
			// However, punishment should use "measured force", so there's a neutral range set up.
			int defections1 = 0;
			int defections2 = 0;
			for (int action : oppHistory1) defections1 += action;
			for (int action : oppHistory2) defections2 += action;
			double defectRate1 = 1.0 * defections1 / n;
			double defectRate2 = 1.0 * defections2 / n;
	
			// If any agent is not trustworthy, defect.
			// Even if both cooperated in the previous round.
			if (defectRate1 >= 0.2 || defectRate2 >= 0.2) return 1;
			
			// If both agents are trustworthy, cooperate.
			if (defectRate1 < 0.05 && defectRate2 < 0.05) return 0;
			
			// If any agent is neutral, go back one more round and check for defection.
			
			// Second round, can't go back any further, so just cooperate.
			// Shouldn't reach this code though given the above conditions.
			if (n == 1) return 0;
			
			// If any of the neutral agents defected, punish by defecting.
			// Otherwise, cooperate.
			if (defectRate1 >= 0.05 && oppHistory1[n - 2] == 1) return 1;
			if (defectRate2 >= 0.05 && oppHistory2[n - 2] == 1) return 1;
			return 0;
		}
	}
	
	class Naing_Htet_Player extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            // Rule 1: our agent will cooperate in the first round
            if (n == 0)  {
                return 0;
            }

            // Rule 2: our agent will defect in the last few rounds, NastyPlayer mode is turned on
            if (n > 95) {
                return 1;
            }

            // Rule 3: if all players including our agent cooperated in the previous round,
            // then our agent will continue to cooperate
            if (myHistory[n-1] == 0 && oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0) {
                return 0;
            }

            // Rule 4: check opponents history to see if they have defected before
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 1 || oppHistory2[i] == 1) {
                    // if either one of them defected before, our agent will always defect
                    return 1;
                }
            }
            // Rule 5: Otherwise, by default nature, our agent will always cooperate
            return 0;
        }
    }
	
	class HistoricalPatternAnalysisPlayer extends Player {
    private int[] lastOpponentActions = new int[2];
    private HashMap<String, Integer> patternMap = new HashMap<>();

    @Override
    int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
        if (n < 2) {
            return 0; // Cooperate initially to gather data
        }

        // Update pattern map with last round's actions
        String lastPattern = "" + lastOpponentActions[0] + lastOpponentActions[1];
        int nextAction = oppHistory1[n-1]; // Assume continuation of pattern by default
        patternMap.put(lastPattern, nextAction);

        // Identify current pattern
        String currentPattern = "" + oppHistory1[n-1] + oppHistory2[n-1];
        lastOpponentActions[0] = oppHistory1[n-1];
        lastOpponentActions[1] = oppHistory2[n-1];

        // Predict next action based on historical patterns
        return patternMap.getOrDefault(currentPattern, 0); // Default to cooperation
    }
}

	class BayesianUpdatingPlayer extends Player {
    private double beliefCooperate = 0.5; // Initial belief that opponent will cooperate

    @Override
    int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
        if (n == 0) {
            return 0; // Cooperate on the first move
        }

        // Update belief based on opponents' last actions
        int lastAction1 = oppHistory1[n-1];
        int lastAction2 = oppHistory2[n-1];
        updateBelief(lastAction1, lastAction2);

        // Decide action based on updated belief
        if (beliefCooperate > 0.5) {
            return 0; // More likely to cooperate
        } else {
            return 1; // More likely to defect
        }
    }

    private void updateBelief(int action1, int action2) {
        // Simple Bayesian update mechanism
        // Adjust these values based on the observed behavior of opponents
        double cooperateWeight = 0.1;
        double defectWeight = 0.2;
        if (action1 == 0 && action2 == 0) {
            beliefCooperate += cooperateWeight;
        } else {
            beliefCooperate -= defectWeight;
        }
        beliefCooperate = Math.min(Math.max(beliefCooperate, 0), 1); // Keep within bounds
    }
}

	class ReinforcementLearningPlayer extends Player {
		private double[][] qTable = new double[2][2]; // Q-values: rows for actions (0=cooperate, 1=defect), columns for opponent's last action
		private double learningRate = 0.1;
		private double discountFactor = 0.9;
		private int lastAction = 0; // Last action taken
		private int lastOpponentAction = 0; // Last opponent's action

		@Override
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) {
				return 0; // Start by cooperating
			}

			// Update Q-table with reward from last action
			int reward = payoff[lastAction][oppHistory1[n-1]][oppHistory2[n-1]];
			qTable[lastAction][lastOpponentAction] += learningRate * (reward + discountFactor * Math.max(qTable[0][oppHistory1[n-1]], qTable[1][oppHistory1[n-1]]) - qTable[lastAction][lastOpponentAction]);

			// Choose next action based on Q-table (greedy policy)
			lastAction = qTable[0][oppHistory1[n-1]] > qTable[1][oppHistory1[n-1]] ? 0 : 1;
			lastOpponentAction = oppHistory1[n-1];

			return lastAction;
		}
	}

	class EmotionalIntelligencePlayer extends Player {
		private int trustLevel = 5; // Neutral trust level on a scale from 0 (distrustful) to 10 (trusting)
		private int lastAction = 0; // Start by cooperating
		private int betrayalCount = 0; // Count of opponent's betrayals
	
		@Override
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Start by cooperating
			
			int oppLastAction1 = oppHistory1[n-1];
			int oppLastAction2 = oppHistory2[n-1];
	
			// Adjust trust level based on opponents' last actions
			if (oppLastAction1 == 1 || oppLastAction2 == 1) { // If any opponent defected
				trustLevel = Math.max(0, trustLevel - 2); // Decrease trust
				betrayalCount++;
			} else {
				trustLevel = Math.min(10, trustLevel + 1); // Increase trust
				betrayalCount = 0; // Reset betrayal count if both cooperated
			}
	
			// Decision making based on trust level and emotional context
			if (betrayalCount >= 2) {
				// Retaliate after repeated betrayals, but give a chance to forgive
				lastAction = (trustLevel > 3) ? 0 : 1;
			} else if (trustLevel > 7) {
				// High trust level - tend to cooperate but stay cautious
				lastAction = 0;
			} else if (trustLevel < 3) {
				// Low trust level - tend to defect but allow for reconciliation
				lastAction = (n % 5 == 0) ? 0 : 1; // Occasionally cooperate to test waters
			} else {
				// Neutral trust level - mimic last action of the more cooperative opponent
				lastAction = (oppLastAction1 + oppLastAction2 < 1) ? 0 : 1;
			}
	
			return lastAction;
		}
	}
	
	class CoEvolutionaryPlayer extends Player {
    private int strategy = 0; // Current strategy index
    private ArrayList<int[]> strategyPopulation = new ArrayList<>();
    private int generationCounter = 0;

    public CoEvolutionaryPlayer() {
        // Initialize with a set of diverse strategies
        strategyPopulation.add(new int[]{0, 1, 0, 1}); // Alternate between cooperation and defection
        strategyPopulation.add(new int[]{1, 1, 1, 0}); // Defect thrice, then cooperate once
        strategyPopulation.add(new int[]{0, 0, 1, 1}); // Cooperate twice, then defect twice
        // Add more initial strategies as needed
    }

    @Override
    int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
        // Evolve strategy every 50 rounds
        if (n % 50 == 0) {
            evolveStrategies(oppHistory1, oppHistory2);
        }

        // Execute current strategy
        int currentStrategyLength = strategyPopulation.get(strategy).length;
        return strategyPopulation.get(strategy)[n % currentStrategyLength];
    }

    private void evolveStrategies(int[] oppHistory1, int[] oppHistory2) {
        // Simplified evolution: if current strategy is not effective, switch to another
        generationCounter++;
        int effectiveness = calculateEffectiveness(oppHistory1, oppHistory2);
        if (effectiveness < 50) { // Assuming effectiveness is some score metric
            strategy = (strategy + 1) % strategyPopulation.size(); // Switch to next strategy
        }

        // Potential area for implementing genetic algorithm operations like mutation or crossover
    }

    private int calculateEffectiveness(int[] oppHistory1, int[] oppHistory2) {
        // Placeholder for calculating strategy effectiveness
        // This could be based on win rates, average scores, etc.
        return 100; // Dummy return value
    }
}

	class PredictiveAnalysisPlayer extends Player {
		// Predicts next move based on majority action in recent history
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 2) return 0; // Not enough history to analyze, cooperate by default
			int opp1LastAction = oppHistory1[n-1];
			int opp2LastAction = oppHistory2[n-1];
			return (opp1LastAction + opp2LastAction >= 1) ? 1 : 0; // Defect if majority defected
		}
	}

	class AdaptativeGenerousTitForTat extends Player {
		// Cooperates but has a chance to be generous after an opponent defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate initially
			double generosityChance = 0.2; // 20% chance to be generous
			int opp1LastAction = oppHistory1[n-1];
			int opp2LastAction = oppHistory2[n-1];
			if (Math.random() < generosityChance) return 0; // Be generous occasionally
			return (opp1LastAction + opp2LastAction >= 1) ? 1 : 0; // Otherwise, mimic majority action
		}
	}

	class RoundBasedStrategist extends Player {
		// Changes strategy based on the round number
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 10) return 0; // Cooperate in early rounds
			if (n >= 10 && n < 50) return n % 2; // Alternate between cooperate and defect in mid rounds
			return 1; // Defect in later rounds
		}
	}

	class HistoryLengthDependentPlayer extends Player {
		// Adapts strategy based on the length of the history
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n % 5 == 0) return 0; // Cooperate every 5 rounds
			int opp1LastAction = oppHistory1[Math.max(0, n-1)];
			int opp2LastAction = oppHistory2[Math.max(0, n-1)];
			return (opp1LastAction + opp2LastAction > 0) ? 1 : 0; // Defect if any opponent defected last round
		}
	}

	class OpportunisticCopycat extends Player {
		// Mimics the strategy of the most successful opponent so far
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate initially
			int opp1Score = 0;
			int opp2Score = 0;
			for (int i = 0; i < n; i++) {
				opp1Score += oppHistory1[i] == 0 ? 1 : -1; // Simplistic scoring
				opp2Score += oppHistory2[i] == 0 ? 1 : -1;
			}
			if (opp1Score > opp2Score) return oppHistory1[n-1]; // Mimic most successful opponent
			else return oppHistory2[n-1];
		}
	}

	class Muki_Player extends Player {
		double forgivenessThreshold = 0.99; // Initial forgiveness threshold
		double retaliationThreshold = 0.001; // Initial retaliation threshold
	
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// Constants defining probabilities
			final double EXPLORATION_PROBABILITY = 0.00; // Probability of exploration
			
			// Check if we're still in the early rounds for exploration
			if (n < 10 && Math.random() < EXPLORATION_PROBABILITY) {
				// Exploration: Cooperate or defect randomly in early rounds
				return Math.random() < 0.5 ? 0 : 1;
			}
			
			// Update forgiveness and retaliation thresholds based on observed behavior
			updateThresholds(myHistory, oppHistory1, oppHistory2);

			if (n <= 0) {
				// If n is not valid, return a default action (cooperate)
				return 0;
			}
	
			// Analyze opponents' recent moves
			int opp1LastMove = oppHistory1[n - 1];
			int opp2LastMove = oppHistory2[n - 1];
			
			// Calculate forgiveness and retaliation probabilities
			double forgivenessProbability = calculateForgiveness(myHistory, oppHistory1, oppHistory2);
			double retaliationProbability = calculateRetaliation(myHistory, oppHistory1, oppHistory2);
			
			// Determine action based on probabilities and thresholds
			if (Math.random() < forgivenessProbability && Math.random() < forgivenessThreshold) {
				// Forgive: Cooperate with a probability of forgiveness
				return 0;
			} else if (Math.random() < retaliationProbability && Math.random() < retaliationThreshold) {
				// Retaliate: Defect with a probability of retaliation
				return 1;
			} else {
				// Default strategy: Mimic the opponent who cooperated in the previous round
				if (opp1LastMove == 0) {
					return opp2LastMove;
				} else {
					return opp1LastMove;
				}
			}
		}
	
		// Helper method to update forgiveness and retaliation thresholds
		void updateThresholds(int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// Calculate relevant metrics (e.g., opponent's cooperation rate)
			double opp1CooperationRate = calculateCooperationRate(oppHistory1);
			double opp2CooperationRate = calculateCooperationRate(oppHistory2);
	
			// Update forgiveness threshold based on opponent's cooperation rate
			if (opp1CooperationRate > 0.7 || opp2CooperationRate > 0.7) {
				forgivenessThreshold += 0.05; // Increase forgiveness threshold
			} else if (opp1CooperationRate < 0.3 && opp2CooperationRate < 0.3) {
				forgivenessThreshold -= 0.05; // Decrease forgiveness threshold
			}
	
			// Ensure forgiveness threshold stays within bounds (e.g., between 0 and 1)
			forgivenessThreshold = Math.max(0, Math.min(1, forgivenessThreshold));
	
			// Similar logic for updating retaliation threshold...
		}
	
		// Helper method to calculate forgiveness probability
		double calculateForgiveness(int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// Count the number of recent defections by opponents
			int recentDefections = 0;
			for (int i = Math.max(0, myHistory.length - 10); i < myHistory.length; i++) {
				if (oppHistory1[i] == 1 || oppHistory2[i] == 1) {
					recentDefections++;
				}
			}
			// Calculate forgiveness probability based on recent defections
			return 1.0 - (double)recentDefections / 10.0;
		}
		
		// Helper method to calculate retaliation probability
		double calculateRetaliation(int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// Count the number of recent mutual defections
			int recentMutualDefections = 0;
			for (int i = Math.max(0, myHistory.length - 10); i < myHistory.length; i++) {
				if (myHistory[i] == 1 && (oppHistory1[i] == 1 || oppHistory2[i] == 1)) {
					recentMutualDefections++;
				}
			}
			// Calculate retaliation probability based on recent mutual defections
			return (double)recentMutualDefections / 10.0;
		}
	
		// Helper method to calculate opponent's cooperation rate
		double calculateCooperationRate(int[] history) {
			// Calculate opponent's cooperation rate based on their history
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

	// Strategy 1: AdaptiveForgiver
    // Forgives occasional defections but becomes less tolerant if defections persist.
    class AdaptiveForgiver extends Player {
        private int consecutiveDefections = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) consecutiveDefections++;
            else consecutiveDefections = 0;

            return consecutiveDefections > 2 ? 1 : 0;
        }
    }

    // Strategy 2: ResponsiveTFT
    // Tit for Tat strategy that adjusts responsiveness based on opponents' cooperation rate.
    class ResponsiveTFT extends Player {
        double cooperationThreshold = 0.6; // Adapt responsiveness based on this threshold.

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            double coopRate = (double) java.util.Arrays.stream(oppHistory1).filter(x -> x == 0).count() / n;
            if (coopRate > cooperationThreshold) {
                return oppHistory1[n-1]; // Mimic last action if above threshold.
            }
            return 1; // Default to defect if cooperation is low.
        }
    }

    // Strategy 3: StrategicDefector
    // Defects strategically when it can gain an upper hand.
    class StrategicDefector extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            if (n % 5 == 0) return 1; // Defect every 5 rounds to disrupt opponent strategy.
            return oppHistory1[n-1]; // Otherwise mimic the last action of opponent 1.
        }
    }

    // Strategy 4: CoopMajority
    // Cooperates if the majority of actions in the last few rounds were cooperative.
    class CoopMajority extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            int coopCount = 0;
            int lookBack = Math.min(n, 10);
            for (int i = 1; i <= lookBack; i++) {
                if (oppHistory1[n-i] == 0) coopCount++;
                if (oppHistory2[n-i] == 0) coopCount++;
            }
            return coopCount >= lookBack ? 0 : 1;
        }
    }

    // Strategy 5: OpportunisticTFT
    // Plays Tit for Tat but takes occasional opportunities to defect when doing well.
    class OpportunisticTFT extends Player {
        private int myScore = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            myScore += payoff[myHistory[n-1]][oppHistory1[n-1]][oppHistory2[n-1]];
            if (myScore > n * 6 && Math.random() < 0.1) return 1; // Defect occasionally when scoring high.
            return oppHistory1[n-1];
        }
    }

    // Strategy 6: ScoreAwareCooperator
    // Cooperates based on comparative scoring, favoring cooperation when leading.
    class ScoreAwareCooperator extends Player {
        private int myScore = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            myScore += payoff[myHistory[n-1]][oppHistory1[n-1]][oppHistory2[n-1]];
            if (myScore > n * 6) return 0; // Continue cooperating if leading.
            return 1; // Defect to catch up or maintain lead.
        }
    }

    // Strategy 7: MutualCooperationTracker
    // Encourages mutual cooperation, defects if either opponent defects too frequently.
    class MutualCooperationTracker extends Player {
        private int totalDefections = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            totalDefections += (oppHistory1[n-1] == 1 ? 1 : 0) + (oppHistory2[n-1] == 1 ? 1 : 0);
            if ((double)totalDefections / (2 * n) > 0.3) return 1; // Defect if defections exceed 30%.
            return 0;
        }
    }

    // Strategy 8: BalancedTFT
    // Similar to Tit for Tat but balances between two opponents' actions.
    class BalancedTFT extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            int lastAction1 = oppHistory1[n-1];
            int lastAction2 = oppHistory2[n-1];
            return (lastAction1 + lastAction2) / 2; // Average out the actions to decide.
        }
    }

    // Strategy 9: LeadFollow
    // Leads with cooperation and follows the most cooperative opponent.
    class LeadFollow extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            return oppHistory1[n-1] < oppHistory2[n-1] ? oppHistory1[n-1] : oppHistory2[n-1];
        }
    }

    // Strategy 10: CyclicPlayer
    // Cycles through different strategies based on the round number.
    class CyclicPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int cycle = n % 3;
            switch (cycle) {
                case 0: return 0; // Always cooperate on first phase.
                case 1: return (oppHistory1[n-1] + oppHistory2[n-1]) / 2; // Average opponents' actions.
                case 2: return 1; // Always defect on third phase.
                default: return 0;
            }
        }
    }

	class WeightedHistoryAwarePlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; // Cooperate on the first turn
			
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
			if (n < 3) return 0; // Cooperate for the first three rounds
			
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
			if (n < 4) return 0; // Cooperate for the first four rounds
			
			// Check if the opponent has defected in the last four rounds
			if (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1 && oppHistory1[n - 3] == 1 && oppHistory1[n - 4] == 1) {
				return 1; // Defect if opponent has defected in the last four rounds
			}
			if (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1 && oppHistory2[n - 3] == 1 && oppHistory2[n - 4] == 1) {
				return 1; // Defect if opponent has defected in the last four rounds
			}
			
			return 0; // Cooperate otherwise
		}
	}
	class TitFor3TatsPlayerRandom extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 3) return 0; // Cooperate for the first three rounds
			
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
			if (n < 3) return 0; // Cooperate for the first three rounds
			
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

	/* In our tournament, each pair of strategies will play one match against each other. 
	 This procedure simulates a single match and returns the scores. */
	float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
		int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
		float ScoreA = 0, ScoreB = 0, ScoreC = 0;
		
		for (int i=0; i<rounds; i++) {
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
		float[] result = {ScoreA/rounds, ScoreB/rounds, ScoreC/rounds};
		return result;
	}

	
//	This is a helper function needed by scoresOfMatch.
	int[] extendIntArray(int[] arr, int next) {
		int[] result = new int[arr.length+1];
		for (int i=0; i<arr.length; i++) {
			result[i] = arr[i];
		}
		result[result.length-1] = next;
		return result;
	}
	
	/* The procedure makePlayer is used to reset each of the Players 
	 (strategies) in between matches. When you add your own strategy,
	 you will need to add a new entry to makePlayer, and change numPlayers.*/
	
	int numPlayers = 78;
	Player makePlayer(int which) {
		switch (which) {
		case 0: return new NicePlayer();
		case 1: return new NastyPlayer();
		case 2: return new RandomPlayer();
		case 3: return new TolerantPlayer();
		case 4: return new FreakyPlayer();
		case 5: return new T4TPlayer();

        case 6: return new GTfTPlayer();
		case 7: return new PavlovPlayer();
		case 8: return new AdaptiveTFTPlayer();
		case 9: return new ConditionalLeaderPlayer();
		case 10: return new ProfileBasedAdaptivePlayer();
		case 11: return new GrudgerPlayer();
		case 12: return new RandomTFTPlayer();
		case 13: return new MirrorMajorityPlayer();
		case 14: return new ForgivingTFTPlayer();
		case 15: return new MajorityRulePlayer();
		case 16: return new OpportunisticAllyPlayer();
		case 17: return new DetectivePlayer();
		case 18: return new AdaptiveMirrorPlayer();
		case 19: return new ContrarianPlayer();
		case 20: return new ReflectiveTFTPlayer();
		case 21: return new PredictiveTFTPlayer();
		case 22: return new LearningTFTPlayer();
		case 23: return new HistoryAwareTFTPlayer();
		case 24: return new TitForTwoTatsPlayer();
		case 25: return new DynamicTFTPlayer();
		case 26: return new CoalitionBuilderPlayer();
		case 27: return new QuasiRandomCDPlayer();
		case 28: return new PatternRecognitionAgent();


		case 29: return new AdaptivePlayer();
		case 30: return new CautiousPlayer();
		case 31: return new OpportunistPlayer();
		case 32: return new AnalyzerPlayer();
		case 33: return new BalancerPlayer();
		case 34: return new RandomTFTPlayerKEL();
		case 35: return new VengefulPlayer();
		case 36: return new PacifistPlayer();
		case 37: return new AgitatorPlayer();

		case 38: return new AlternatingTitForTatsPlayer();
		case 39: return new RotatingTitForTatsPlayer();
		case 40: return new ModifiedRotatingTitForTatsPlayer();
		case 41: return new TitForTwoTatsPlayerRandom();
		case 42: return new HistoryAwareTFTPlayerRandom();
		
		case 43: return new GosuTheMinion();
		case 44: return new PM_Low();
		case 45: return new Bummer();

		case 46: return new Huang_KyleJunyuan_Player();

		case 47: return new Ngo_Jason_Player();

		case 48: return new LessTolerantPlayer();
		
		case 49: return new Teo_WeiJie_Player();
		
		case 50: return new Naing_Htet_Player();

		case 51: return new HistoricalPatternAnalysisPlayer();
		case 52: return new BayesianUpdatingPlayer();
		case 53: return new ReinforcementLearningPlayer();
		case 54: return new EmotionalIntelligencePlayer();
		case 55: return new CoEvolutionaryPlayer();

		case 56: return new PredictiveAnalysisPlayer();
		case 57: return new AdaptativeGenerousTitForTat();
		case 58: return new RoundBasedStrategist();
		case 59: return new HistoryLengthDependentPlayer();
		case 60: return new OpportunisticCopycat();
		case 61: return new Muki_Player();
		case 62: return new Lim_Jiexian_Player();
		case 63: return new AdaptiveForgiver();
		case 64: return new ResponsiveTFT();
		case 65: return new StrategicDefector();
		case 66: return new CoopMajority();
		case 67: return new OpportunisticTFT();
		case 68: return new ScoreAwareCooperator();
		case 69: return new MutualCooperationTracker();
		case 70: return new LeadFollow();
		case 71: return new BalancedTFT();
		case 72: return new CyclicPlayer();
		case 73: return new WeightedHistoryAwarePlayer();
		case 74: return new TitFor3TatsPlayer();
		case 75: return new TitFor4TatsPlayer();
		case 76: return new TitFor3TatsPlayerRandom();
		case 77: return new ForgivingTitFor3TatsPlayer();



		






		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}
	
	/* Finally, the remaining code actually runs the tournament. */
	
	public static void main (String[] args) {
		ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
		instance.runTournament();
	}
	
	boolean verbose = true; // set verbose = false if you get too much text output
	
	void runTournament() {
		float[] totalScore = new float[numPlayers];

		// This loop plays each triple of players against each other.
		// Note that we include duplicates: two copies of your strategy will play once
		// against each other strategy, and three copies of your strategy will play once.

		for (int i=0; i<numPlayers; i++) for (int j=i; j<numPlayers; j++) for (int k=j; k<numPlayers; k++) {

			Player A = makePlayer(i); // Create a fresh copy of each player
			Player B = makePlayer(j);
			Player C = makePlayer(k);
			int rounds = 90 + (int)Math.rint(20 * Math.random()); // Between 90 and 110 rounds
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
		for (int i=0; i<numPlayers; i++) {
			int j=i-1;
			for (; j>=0; j--) {
				if (totalScore[i] > totalScore[sortedOrder[j]]) 
					sortedOrder[j+1] = sortedOrder[j];
				else break;
			}
			sortedOrder[j+1] = i;
		}
		
		// Finally, print out the sorted results.
		if (verbose) System.out.println();
		System.out.println("Tournament Results");
		for (int i=0; i<numPlayers; i++) 
			System.out.println(makePlayer(sortedOrder[i]).name() + ": " 
				+ totalScore[sortedOrder[i]] + " points.");
		
	} // end of runTournament()
	
    
} // end of class PrisonersDilemma

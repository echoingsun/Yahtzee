
/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Extension_Yahtzee_RS extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Extension_Yahtzee_RS().start(args);
	}

	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	private void playGame() {

		// Array scoreCard keeps track of the players' scores.
		// It's N_CATEGORES (17) * number of players.
		// When modifying the values in scoreCard, minus 1 from the parameters.
		scoreCard = new int[N_CATEGORIES][nPlayers];

		// Array isScoreUpdated keeps track of whether the scoring part of the
		// scoreCard is already updated. If yes, player cannot modify the
		// results,
		// but can only turn to another category instead.
		// It does not include the non-scoring parts of the scoreCard.
		// When modifying the values in isScoreUpdated, minus 1 from the
		// parameters.
		isScoreUpdated = new boolean[N_SCORING_CATEGORIES][nPlayers];

		while (!gameEnds()) {
			for (int i = 1; i <= nPlayers; i++) {
				playOneRound(i); // Here i starts at 1.
			}
		}

		// Calculate and display all parts of scores of all players.
		getAllScores();

		// Store the best score and the player who scored it into an array
		// that has two elements, namely player(index) and best score.
		int[] bestPlayerAndScore = getBest(nPlayers);

		// Get the player name through the player index obtained above.
		String bestPlayer = playerNames[bestPlayerAndScore[0]];

		// Similarly, return the best score.
		int bestScore = bestPlayerAndScore[1];

		display.printMessage(
				"Congratulations " + bestPlayer + "! You're the winner with a total score of " + bestScore + "!");

	}

	/*
	 * Method getBest takes nPlayers as input and returns an integer array that
	 * has two elements: the index of the player that has the best score and
	 * that best score.
	 */
	private int[] getBest(int nPlayers) {

		// Make the total score row into a new array that has nPlayers number of
		// elements.
		// Pass the total score from the scorecard to the new array.
		int[] allTotalScores = new int[nPlayers];
		for (int i = 0; i < allTotalScores.length; i++) {
			allTotalScores[i] = scoreCard[TOTAL - 1][i];
		}

		// Compare the total scores and get the highest.
		// Let the best player and his score be the first player's,
		// loop through the new array, compare all values and get the highest.
		// Update the best player index and the highest score.
		int bestPlayerIndex = 0;
		int maxScore = allTotalScores[0];
		for (int i = 0; i < allTotalScores.length; i++) {
			if (allTotalScores[i] > maxScore) {
				maxScore = allTotalScores[i];
				bestPlayerIndex = i;
			}
		}

		saveToFile(bestPlayerIndex, maxScore);

		// Create the array this method is going to return.
		// Put in the best player index and his score.
		int[] bestPlayerAndScore = new int[2];
		bestPlayerAndScore[0] = bestPlayerIndex;
		bestPlayerAndScore[1] = maxScore;
		return bestPlayerAndScore;
	}

	/*
	 * http://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it-in-java
	 */
	private void saveToFile(int bestPlayerIndex, int maxScore) {
		String nameStr = playerNames[bestPlayerIndex];
		String scoreStr = Integer.toString(maxScore);
		try {
			
			File highScoresTxt = new File("highScores.txt");
			FileWriter fw = new FileWriter(highScoresTxt);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(nameStr + " " + scoreStr);
			bw.close();
		} catch (Exception e) {
		}
	}

	/*
	 * Method getAllScores sums up the upper, lower and total scores of all
	 * players, and display them on the screen.
	 */
	private void getAllScores() {

		// Loop through each player and calculate upper, lower and totals
		// respectively.
		for (int playerIndex = 0; playerIndex < nPlayers; playerIndex++) {

			// For each one player, get and display upper scores and bonus if
			// applicable.
			// The upper categories are from 1-6, but in the scoreCard they
			// range from 0 - 5.
			// Category 7 (in scorecard, index 6) adds up the scores in the
			// upper categories.
			for (int upper = ONES - 1; upper < SIXES; upper++) {
				scoreCard[UPPER_SCORE - 1][playerIndex] = scoreCard[UPPER_SCORE - 1][playerIndex]
						+ scoreCard[upper][playerIndex];
			}
			int upperScore = scoreCard[UPPER_SCORE - 1][playerIndex];

			// For convenience, define int upperScore to store the value.
			// Apply bonus if applicable.
			// Display upperScore and upperBonus.
			int bonusIfAny = 0;
			if (upperScore >= UPPER_BONUS_LIMIT) {
				bonusIfAny = UPPER_BONUS_AMT;
			} else {
				bonusIfAny = 0;
			}

			display.updateScorecard(UPPER_BONUS, playerIndex + 1, bonusIfAny);
			display.updateScorecard(UPPER_SCORE, playerIndex + 1, upperScore);

			// get and display lower scores.
			for (int lower = THREE_OF_A_KIND - 1; lower < CHANCE; lower++) {
				scoreCard[LOWER_SCORE - 1][playerIndex] = scoreCard[LOWER_SCORE - 1][playerIndex]
						+ scoreCard[lower][playerIndex];
			}
			int lowerScore = scoreCard[LOWER_SCORE - 1][playerIndex];
			display.updateScorecard(LOWER_SCORE, playerIndex + 1, lowerScore);

			// update and show total score.
			int totalScore = upperScore + bonusIfAny + lowerScore;
			scoreCard[TOTAL - 1][playerIndex] = totalScore;
			display.updateScorecard(TOTAL, playerIndex + 1, totalScore);
		}

	}

	/*
	 * Method playOneRound defines the events that happen in one single cycle of
	 * rolling dice, re-rolling, categorizing and scoring. Since the caller of
	 * this method has player index starting at 1 while the array playerNames
	 * starts at 0, minus 1 when dealing with the array.
	 */
	private void playOneRound(int playerIndex) {

		display.printMessage(playerNames[playerIndex - 1] + "'s turn! Click \"Roll Dice\" button to roll the dice.");

		// Wait for the player to roll for the 1st time.
		display.waitForPlayerToClickRoll(playerIndex);

		// Give a random 1-6 value to each die in the dice array.
		// Show the values on the screen.
		for (int i = 0; i < N_DICE; i++) {
			randomValue(i);
		}
		display.displayDice(diceValue);

		// Player has two chances to reshuffle as they like.
		// Since the waitForPlayerToSelectDice method only returns
		// when player rolls again (displayDice again),
		// call this method first, wait for player for their choices,
		// and finally display the chosen dice.
		// This process repeats twice.
		for (int i = 0; i < TURN_PER_PLAYER - 1; i++) {
			display.printMessage("Select the dice you wish to re-roll and click \"Roll Again\"");
			display.waitForPlayerToSelectDice();
			for (int k = 0; k < N_DICE; k++) {
				if (display.isDieSelected(k)) {
					randomValue(k);
				}
			}
			display.displayDice(diceValue);
		}

		display.printMessage("Select a category for this roll.");

		// Once the player has used up two re-roll chances,
		// let them pick a category and calculate the score
		// according to the category they picked.
		int category = selectCategory(playerIndex);
		int score = calculateScore(category);

		// For each category they finally pick, update their score card.
		// This is different from simply displaying the score.
		// It stores all the scores into the 2D scoreCard array.
		// Here category and playerIndex starts at 1,
		// So minus 1 when playing with the array thing.
		scoreCard[category - 1][playerIndex - 1] = score;

		// For every score recorded, update the player's total score.
		int totalScore = updateTotal(score, playerIndex);

		// display score;
		display.updateScorecard(category, playerIndex, score);
		display.updateScorecard(TOTAL, playerIndex, totalScore);

		// Make sure the category that has been chosen by the player
		// will not and cannot be updated later.
		markAsUpdated(category, playerIndex);

	}

	/*
	 * Method calculateScore passes in the category the player picks and decides
	 * if the pattern of the dice fits in that category. If yes it will return a
	 * legit score according to the rules if not it will just return 0.
	 */
	private int calculateScore(int category) {

		// **************Dice Value Calculations*********************
		// Some categories will require summing up the dice values.
		// For convenience it is calculated here.
		int sum = 0;
		for (int i = 0; i < diceValue.length; i++) {
			sum = sum + diceValue[i];
		}

		// For many categories, it might be helpful to calculate the
		// frequency of the numbers on the dice
		// by creating a new array that records the frequencies.
		// For example, [1,0,1,1,1,1] means that
		// the number "2" did not appear in this roll, while
		// other five numbers appeared once each.
		int[] freq = new int[6];

		// Loop through the array diceValue that
		// stores the value for dice 1-5 (in array, 0 - 4).
		for (int i = 0; i < diceValue.length; i++) {
			switch (diceValue[i]) {
			case 1:
				freq[0]++;
				break; // If die value equals 1, the frequency of 1 ++.
			case 2:
				freq[1]++;
				break;
			case 3:
				freq[2]++;
				break;
			case 4:
				freq[3]++;
				break;
			case 5:
				freq[4]++;
				break;
			case 6:
				freq[5]++;
				break;
			default:
				diceValue[i] = 0;
				break;
			}
		}

		// *****************Categorization Logic*********************
		// This part takes the category the player picks
		// in selectCategory method and checks if the dice pattern fits
		// the category.
		switch (category) {

		// For upper categories, just check how many times the value appeared.
		case ONES:
			if (freq[0] != 0)
				return freq[0] * 1; // return the point * times appeared.
			break;
		case TWOS:
			if (freq[1] != 0)
				return freq[1] * 2;
			break;
		case THREES:
			if (freq[2] != 0)
				return freq[2] * 3;
			break;
		case FOURS:
			if (freq[3] != 0)
				return freq[3] * 4;
			break;
		case FIVES:
			if (freq[4] != 0)
				return freq[4] * 5;
			break;
		case SIXES:
			if (freq[5] != 0)
				return freq[5] * 6;
			break;

		// For 3 or 4 of a kind, they would require a certain value
		// appear more than 3 or 4 times.
		// Similar logic can be applied to full house and Yahtzee.
		// Full house requires there be only 1 shown-twice and
		// 1 shown-three-times.
		case THREE_OF_A_KIND:
			for (int i = 0; i < freq.length; i++) {
				if (freq[i] >= 3)
					return sum;
			}
			break;
		case FOUR_OF_A_KIND:
			for (int i = 0; i < freq.length; i++) {
				if (freq[i] >= 4)
					return sum;
			}
			break;
		case FULL_HOUSE:
			int twoCount = 0;
			int threeCount = 0;
			for (int i = 0; i < freq.length; i++) {
				if (freq[i] == 2)
					twoCount++;
				if (freq[i] == 3)
					threeCount++;
			}
			if (twoCount == 1 && threeCount == 1)
				return PTS_FULL_HOUSE;
			break;
		case YAHTZEE:
			for (int i = 0; i < freq.length; i++) {
				if (freq[i] == 5)
					return PTS_YAHTZEE;
			}
			break;
		case CHANCE:
			return sum;

		// For straights, we need to consider the frequency pattern.
		// For large straight, it will either be 12345 or 23456.
		// Therefore, it requires 5 frequency counts to be 1 and
		// the frequency of either 1 or 6 be 0.
		case LARGE_STRAIGHT:
			int oneCount = 0;
			for (int i = 0; i < freq.length; i++) {
				if (freq[i] == 1)
					oneCount++;
			}
			if (oneCount == 5 && (freq[0] == 0 || freq[5] == 0))
				return PTS_LG_STRT;
			break;

		// Small straight requires two-step thinking.
		// First, there can be 3 types of small straights:
		// (1) large straight
		// (2) something like 12346, 13456
		// (3) something like 12234, 33456, 34556, etc.
		// Second, for (1) and (2), as long as the frequency distribution
		// is not 110111 or 111011 (12456/12356), they should count.
		// For (3), instead of coming up with all the combinations,
		// it's easier to think about the position of 0s in the frequency map.
		// Since small straights will always have at least 4 consecutive values
		// in a row, possible frequency distribution would be something like:
		// 111200, 012110, 001121, etc. The positions of 0s cannot be other than
		// what these 3 situations indicate.
		case SMALL_STRAIGHT:
			oneCount = 0;
			twoCount = 0;
			for (int i = 0; i < freq.length; i++) {
				if (freq[i] == 1)
					oneCount++;
				if (freq[i] == 2)
					twoCount++;
			}

			boolean allAppearedOnce = oneCount == 5 && (freq[2] != 0 && freq[3] != 0);
			boolean someAppearedTwice = twoCount == 1 && oneCount == 3 && ((freq[4] == 0 && freq[5] == 0)
					|| (freq[0] == 0 && freq[5] == 0) || (freq[0] == 0 && freq[1] == 0));
			boolean isSmallStraight = allAppearedOnce || someAppearedTwice;
			if (isSmallStraight)
				return PTS_SML_STRT;
			break;

		default:
			return 0;
		}

		// Even if the dice patterns look nice, as long as
		// it does not match the category the player picks, return 0 as the
		// score.
		return 0;
	}

	/*
	 * Method selectCategory mainly checks whether the category for that player
	 * is available. If yes, it returns the value of the category, and points
	 * will be calculated accordingly. If no, player is asked to pick an
	 * available one until he does so.
	 */
	private int selectCategory(int playerIndex) {
		int category = display.waitForPlayerToSelectCategory();

		// Since category index and scoring category index does not match
		// perfectly, we have to look into them respectively.
		// isScoreUpdated is a boolean array that only covers the scoring part
		// of the scorecard. Deal with the difference in numbering.
		boolean updated = (category <= SIXES && isScoreUpdated[category - 1][playerIndex - 1] == true)
				|| (category >= 9 && category <= 15 && isScoreUpdated[category - 3][playerIndex - 1] == true);
		while (updated) {
			display.printMessage("This category has already been used. Please choose a different category.");
			category = display.waitForPlayerToSelectCategory(); // Ask player to
																// re-pick.
			// Update the condition.
			updated = (category <= 6 && isScoreUpdated[category - 1][playerIndex - 1] == true)
					|| (category >= 9 && category <= 15 && isScoreUpdated[category - 3][playerIndex - 1] == true);
		}
		return category; // Finally return a valid category.

	}

	/*
	 * Method updateTotal takes in a score and the player index, and update the
	 * the total score in the scoreCard array. This is to be displayed on the
	 * screen later.
	 */
	private int updateTotal(int score, int playerIndex) {
		scoreCard[TOTAL - 1][playerIndex - 1] = scoreCard[TOTAL - 1][playerIndex - 1] + score;
		int totalScore = scoreCard[TOTAL - 1][playerIndex - 1];

		return totalScore;
	}

	/*
	 * Method markAsUpdated takes in the category the player chooses and marks
	 * "true" in the isScoreUpdated boolean array. It means that play already
	 * put a score in there and it cannot be
	 */
	private void markAsUpdated(int category, int playerIndex) {
		if (category <= 6) {
			isScoreUpdated[category - 1][playerIndex - 1] = true;
		} else if (category >= 9 && category <= 15) {
			isScoreUpdated[category - 3][playerIndex - 1] = true;
		}
	}

	/*
	 * Method randomValue generates the random value for the i'th die, and
	 * catches if exception occurs. The range of i varies in different
	 * situations.
	 */
	private void randomValue(int i) {
		try {
			diceValue[i] = rg.nextInt(1, 6);
		} catch (ErrorException e) {
		}
	}

	/*
	 * Method gameEnds stops players from rolling dice once all the scoring
	 * parts are filled with valid scores. That is to say, when each element of
	 * isScoreUpdated is true (updated), the game should end.
	 */
	private boolean gameEnds() {
		for (int r = 0; r < isScoreUpdated.length; r++) {
			for (int c = 0; c < isScoreUpdated[0].length; c++) {
				if (isScoreUpdated[r][c] == false)
					return false;
			}
		}
		return true;
	}

	/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rg = new RandomGenerator();

	// Define the dice array that holds the random dice value for each die in
	// the array.
	private int[] diceValue = new int[N_DICE];

	// Define the score card for all players. It is to be initialized at the
	// beginning of the game once the number of players is decided.
	private int[][] scoreCard;

	// Use this boolean to keep track of whether the scoring part of the
	// scoreCard is already updated.
	private boolean[][] isScoreUpdated;

	// Create a 2D array to store all the history high scores.
	private String [][] hallOfFame = new String [11][3];
}

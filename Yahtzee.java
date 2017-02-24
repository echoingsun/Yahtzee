
/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
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
		getAllScores();

		int[] bestPlayerAndScore = getBest(nPlayers);
		String bestPlayer = playerNames[bestPlayerAndScore [0]];
		int bestScore = bestPlayerAndScore [1];
		display.printMessage("Congratulations " + bestPlayer + "! You're the winner with a total score of " + bestScore + "!");

	}

	private int[] getBest(int nPlayers) {
		int[] allTotalScores = new int [nPlayers];
		for (int i = 0; i < allTotalScores.length; i++){
			allTotalScores[i] = scoreCard[TOTAL-1][i];
		}
		
		int bestPlayerIndex = 0;
		int maxScore = allTotalScores[0];
		for (int i = 0; i < allTotalScores.length; i++) {
			if (allTotalScores[i] > maxScore) {
				maxScore = allTotalScores[i];
				bestPlayerIndex = i;
			}
		}
		int[] bestPlayerAndScore = new int[2];
		bestPlayerAndScore[0] = bestPlayerIndex;
		bestPlayerAndScore[1] = maxScore;
		return bestPlayerAndScore;
	}

	private void getAllScores() {

		for (int playerIndex = 0; playerIndex < nPlayers; playerIndex++) {

			// get and display upper scores and bonus if applicable,.
			for (int upper = ONES - 1; upper < SIXES; upper++) {
				scoreCard[UPPER_SCORE - 1][playerIndex] = scoreCard[UPPER_SCORE - 1][playerIndex]
						+ scoreCard[upper][playerIndex];
			}
			int upperScore = scoreCard[UPPER_SCORE - 1][playerIndex];
			if (upperScore >= UPPER_BONUS_LIMIT) {
				display.updateScorecard(UPPER_BONUS, playerIndex + 1, UPPER_BONUS_AMT);
				upperScore = upperScore + UPPER_BONUS_AMT;
			}
			display.updateScorecard(UPPER_SCORE, playerIndex + 1, upperScore);
			
			// get and display lower scores.
			for (int lower = THREE_OF_A_KIND - 1; lower < CHANCE; lower++) {
				scoreCard[LOWER_SCORE - 1][playerIndex] = scoreCard[LOWER_SCORE - 1][playerIndex]
						+ scoreCard[lower][playerIndex];
			}
			int lowerScore = scoreCard[LOWER_SCORE - 1][playerIndex];
			display.updateScorecard(LOWER_SCORE, playerIndex + 1, lowerScore);

			// update total score.
			int totalScore = upperScore + lowerScore;
			scoreCard[TOTAL-1][playerIndex]=totalScore;
			display.updateScorecard(TOTAL, playerIndex + 1, totalScore);
		}

	}

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
			for (int k = 0; k < N_DICE - 1; k++) {
				if (display.isDieSelected(k)) {
					randomValue(k);
				}
			}
			display.displayDice(diceValue);
		}

		display.printMessage("Select a category for this roll.");
		

		int score = 100;

		int category = selectCategory(playerIndex);
		// categorize; calculate score.
		scoreCard[category-1][playerIndex-1] = score;
		
		int totalScore = updateTotal(score, playerIndex);

		// display score;
		display.updateScorecard(category, playerIndex, score);
		display.updateScorecard(TOTAL, playerIndex, totalScore);
		markAsUpdated(category, playerIndex);

	}
	
	private int calculateScore (int category){
		
		// Calculate the frequency of the numbers on the dice
		// by creating a new array that records the frequencies.
		// For example, [1,0,1,1,1,1] means that
		// the number "2" did not appear in this roll, while
		// other five numbers appeared once each.
		
		new int[] freq = new int [6];
		

		
		
		

		
		
	}

	private int selectCategory(int playerIndex) {
		int category = display.waitForPlayerToSelectCategory();
		boolean updated = (category <= 6 && isScoreUpdated[category - 1][playerIndex - 1] == true)
				|| (category >= 9 && category <= 15 && isScoreUpdated[category - 3][playerIndex - 1] == true);
		while (updated) {
			display.printMessage("This category has already been used. Please choose a different category.");
			category = display.waitForPlayerToSelectCategory();
			updated = (category <= 6 && isScoreUpdated[category - 1][playerIndex - 1] == true)
					|| (category >= 9 && category <= 15 && isScoreUpdated[category - 3][playerIndex - 1] == true);
		}
		return category;

	}

	private int updateTotal(int score, int playerIndex) {
		scoreCard[TOTAL - 1][playerIndex - 1] = scoreCard[TOTAL - 1][playerIndex - 1] + score;
		int totalScore = scoreCard[TOTAL - 1][playerIndex - 1];
		return totalScore;
	}

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

}

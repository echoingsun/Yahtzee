
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
		
		scoreCard = new int [N_CATEGORIES][nPlayers]; // 0-16, 0-3
		isScoreUpdated = new boolean [N_SCORING_CATEGORIES][nPlayers]; // 0-12, 0-3
				
		while (!gameEnds()){
			for (int i = 1; i <= nPlayers; i++){
				playOneRound(i);
			}
		}
		display.printMessage("Game ends."); 
	
	}

	private void playOneRound(int playerIndex) {
		
		// Initialize the total score of one player
		int totalScore = scoreCard[N_CATEGORIES - 1][playerIndex];
		
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
			display.waitForPlayerToSelectDice();
			for (int k = 0; k < N_DICE - 1; k++) {
				if (display.isDieSelected(k)) {
					randomValue(k);
				}
			}
			display.displayDice(diceValue);
		}
		
		// categorize;
		// check if used
		
		int category = display.waitForPlayerToSelectCategory();
		int score = 100;
		totalScore += score;
		
		
		// display score;
		display.updateScorecard(category, playerIndex, score); 
		display.updateScorecard(N_CATEGORIES-1, playerIndex, totalScore);
		markAsUpdated(category, playerIndex);
		
		
	}

	private void markAsUpdated(int category, int playerIndex) {
		if (category <=6){
			isScoreUpdated [category - 1][playerIndex - 1] = true;
		} else if (category >=9 && category <=15 ){
			isScoreUpdated [category -3 ][playerIndex -1] = true;
		} 		
	}
	

	/*
	 * Method randomValue generates the random value for the i'th die,
	 * and catches if exception occurs.
	 * The range of i varies in different situations.
	 */
	private void randomValue(int i) {
		try {
			diceValue[i] = rg.nextInt(1, 6);
		} catch (ErrorException e) {
		}
	}

	private boolean gameEnds(){
		for (int r = 0; r < isScoreUpdated.length; r++){
			for (int c = 0; c < isScoreUpdated[0].length; c ++){
				if (isScoreUpdated[r][c] == false) return false;
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
	private int [][] scoreCard;
	private boolean [][] isScoreUpdated;
	

}

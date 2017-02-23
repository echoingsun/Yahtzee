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

		// for loop nPlayers.
		// For one single player:
		display.waitForPlayerToClickRoll(1);
		arrayNDICE[0] = 1;
		arrayNDICE[1] = 1;
		arrayNDICE[2] = 1;
		arrayNDICE[3] = 1;
		arrayNDICE[4] = 1;
		display.displayDice(arrayNDICE);
		
	}
		
/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rg = new RandomGenerator();
	
	private int[] arrayNDICE = new int [N_DICE];

}

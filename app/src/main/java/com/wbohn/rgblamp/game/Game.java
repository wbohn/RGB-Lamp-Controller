package com.wbohn.rgblamp.game;

import android.util.Log;

import java.util.Random;

/**
 * Created by William on 5/30/2015.
 */
public class Game {
    private static final int NUM_LEVELS = 100;
    private static final String TAG = "Game";
    private int[] sequence;
    private int score = 0;

    public boolean running;

    public interface GameInterface {
        void guessingDone(int roundScore);
        void gameOver(int score);
    }
    private GameInterface gameInterface;

    public int index;
    public int guessIndex;
    public boolean guessing;

    public void setGameInterface(GameInterface gameInterface) {
        this.gameInterface = gameInterface;
    }

    public Game() {
        sequence = new int[NUM_LEVELS];
        makeSequence(sequence);
        running = false;
        index = 0;
        guessIndex = 0;
        guessing = false;
    }

    public int advance() {
        Log.i("advance from ", String.valueOf(index));
        index++;
        Log.i("advance to ", String.valueOf(index));
        return index;
    }

    private void makeSequence(int[] levels) {
        for (int i = 0; i < NUM_LEVELS; i++) {
            Random random = new Random();
            levels[i] = random.nextInt(5);
        }
    }

    public int[] getSequence() {
        return sequence;
    }

    public void guessMade(int id) {
        Log.i(TAG, "Guess: " + String.valueOf(id));
        if (!guessing) {
            return;
        }
        // last guess
        if (guessIndex == index) {
            if (id == sequence[guessIndex]) {
                score++;
                guessingDone();
            } else {
                gameOver();
            }
        } else {
            if (id == sequence[guessIndex]) {
                guessIndex++;
                score++;
            } else {
                gameOver();
            }
        }
    }

    private void guessingDone() {
        guessing = false;
        guessIndex = 0;
        advance();
        gameInterface.guessingDone(score);
    }

    public void gameOver() {
        gameInterface.gameOver(score);
        score = 0;
    }
}

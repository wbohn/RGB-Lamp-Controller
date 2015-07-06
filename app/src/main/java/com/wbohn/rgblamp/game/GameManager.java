package com.wbohn.rgblamp.game;

import android.os.CountDownTimer;

import com.squareup.otto.Subscribe;
import com.wbohn.rgblamp.App;
import com.wbohn.rgblamp.bus.GuessEvent;
import com.wbohn.rgblamp.bus.IncomingMessageEvent;
import com.wbohn.rgblamp.bus.Message;
import com.wbohn.rgblamp.bus.MessageBuilder;

import java.util.Arrays;

/**
 * Created by William on 7/4/2015.
 */
public class GameManager implements Game.GameInterface{

    private Game game;
    private GameStartDelayTimer gameStartDelayTimer;

    public int[] getSequence() {
        if (game != null) {
            return game.getSequence();
        } else {
            return null;
        }
    }

    public int getScore() {
        if (game != null) {
            return game.getScore();
        }
        else {
            return 0;
        }
    }

    public void setSequence(int[] sequence) {
        game.setSequence(sequence);
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public interface GameManagerInterface {
        void updateScore(String score);
        void updateHighScore(String highScore);
        void updateCountdown(String secondsLef);
    }
    private GameManagerInterface gameManagerInterface;

    public void setInterface(GameManagerInterface gameManagerInterface) {
        this.gameManagerInterface = gameManagerInterface;
    }

    public void onNewGameClicked() {
        game = new Game();
        game.setGameInterface(this);
        gameManagerInterface.updateScore("0");
        App.getEventBus().post(new Message(Arrays.toString(game.getSequence())));
    }

    @Subscribe
    public void onIncomingMessageEvent(IncomingMessageEvent event) {
        switch (event.text) {
            case IncomingMessageEvent.SEQUENCE_RECEIVED:
                startGame();
                return;
            case IncomingMessageEvent.LEVEL_SHOWN:
                levelShown();
                return;
        }
    }

    public void startGame() {
        gameManagerInterface.updateCountdown("5");
        gameStartDelayTimer = new GameStartDelayTimer(5000, 100);
        gameStartDelayTimer.start();
    }

    public void levelShown() {
        game.guessing = true;
    }

    @Subscribe
    public void onGuessEvent(GuessEvent event) {
        if (game != null) {
            game.checkGuess(event.bulbIndex);
        }
    }

    @Override
    public void gameOver(int score) {
        App.getEventBus().post(new Message(MessageBuilder.GAME_OVER));

        int currentHighScore = App.getAppPreferences().getHighScore();
        if (score >= currentHighScore) {
            App.getAppPreferences().saveHighScore(score);
            gameManagerInterface.updateHighScore(String.valueOf(currentHighScore));
        }
        gameManagerInterface.updateScore("0");
    }

    @Override
    public void guessMade(int score) {
        if (score > App.getAppPreferences().getHighScore()) {
            App.getAppPreferences().saveHighScore(score);
            gameManagerInterface.updateHighScore(String.valueOf(score));
        }
        gameManagerInterface.updateScore(String.valueOf(score));    }

    @Override
    public void guessingDone(int score) {
        gameManagerInterface.updateScore(String.valueOf(score));
        int currentHighScore = App.getAppPreferences().getHighScore();

        if (score > currentHighScore) {
            App.getAppPreferences().saveHighScore(score);
            gameManagerInterface.updateHighScore(String.valueOf(score));
        }
        advanceGame();
    }

    public void advanceGame() {
        game.guessing = false;
        App.getEventBus().post(new Message(MessageBuilder.SHOW_LEVEL));
    }

    private class GameStartDelayTimer extends CountDownTimer {

        private int roundedSecondsLeft;
        public GameStartDelayTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            roundedSecondsLeft = 0;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (Math.round((float) millisUntilFinished / 1000.0f) != roundedSecondsLeft) {
                roundedSecondsLeft = Math.round((float) millisUntilFinished / 1000.0f);
                gameManagerInterface.updateCountdown(String.valueOf(roundedSecondsLeft));
            }
        }

        @Override
        public void onFinish() {
            gameManagerInterface.updateCountdown("");
            game.running = true;
            advanceGame();
        }
    }
}

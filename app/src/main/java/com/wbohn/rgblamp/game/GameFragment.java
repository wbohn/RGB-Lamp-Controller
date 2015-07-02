package com.wbohn.rgblamp.game;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wbohn.rgblamp.prefs.AppPreferences;
import com.wbohn.rgblamp.R;

public class GameFragment extends Fragment implements Game.GameInterface {
    private static final String TAG = "GameFragment";

    private TextView countdownTextView;
    private TextView scoreTextView;
    private TextView highScoreTextView;

    private GameStartDelayTimer gameStartDelayTimer;
    private Game game;
    private int highScore;

    private AppPreferences appPrefs;

    private GameFragmentInterface gameFragmentInterface;
    public interface GameFragmentInterface {
        void setSequence(int[] sequence);
        void showLevel();
        void gameOver(int score);
        void saveHighScore(int score);
    }

    public void startGame() {
        countdownTextView.setText("5");
        gameStartDelayTimer = new GameStartDelayTimer(5000, 100);
        gameStartDelayTimer.start();
    }

    @Override
    public void guessingDone(int score) {
        scoreTextView.setText(String.valueOf(score));
        if (score > highScore) {
            highScore = score;
            highScoreTextView.setText(String.valueOf(score));
            gameFragmentInterface.saveHighScore(score);
        }
        Log.i(TAG, String.valueOf(score));
        advanceGame();
    }

    @Override
    public void gameOver(int score) {
        gameFragmentInterface.gameOver(score);
        scoreTextView.setText("0");
    }

    public static GameFragment newInstance(int highScore) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putInt("high_score", highScore);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        countdownTextView = (TextView) rootView.findViewById(R.id.text_view_countdown);
        scoreTextView = (TextView) rootView.findViewById(R.id.text_view_score);
        highScoreTextView = (TextView) rootView.findViewById(R.id.text_view_high_score);

        highScore = appPrefs.getHighScore();

        highScoreTextView.setText(String.valueOf(highScore));

        Button newGame = (Button) rootView.findViewById(R.id.button_new_game);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game = new Game();
                game.setGameInterface((Game.GameInterface) getActivity().getFragmentManager().findFragmentByTag("gameFragment"));
                gameFragmentInterface.setSequence(game.getSequence());
            }
        });
        return rootView;
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
                countdownTextView.setText(String.valueOf(roundedSecondsLeft));
            }
        }

        @Override
        public void onFinish() {
            countdownTextView.setText("");
            game.running = true;
            advanceGame();
        }
    }
    public void levelShown() {
        game.guessing = true;
    }

    public void advanceGame() {
        Log.i(TAG, "advanceGame");
        game.guessing = false;

        gameFragmentInterface.showLevel();
    }

    public void guessMade(int id) {
        if (game != null) {
            game.guessMade(id);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        gameFragmentInterface = (GameFragmentInterface) activity;
        appPrefs = new AppPreferences(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        gameFragmentInterface = null;
    }
}

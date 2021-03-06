package com.wbohn.rgblamp.game;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wbohn.rgblamp.App;
import com.wbohn.rgblamp.R;

public class GameFragment extends Fragment implements GameManager.GameManagerInterface {
    private static final String TAG = "GameFragment";

    private TextView countdownTextView;
    private TextView scoreTextView;
    private TextView highScoreTextView;

    private GameManager gameManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameManager = new GameManager();
        gameManager.setInterface(this);

        App.getEventBus().register(gameManager);
        App.getEventBus().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        countdownTextView = (TextView) rootView.findViewById(R.id.text_view_countdown);
        scoreTextView = (TextView) rootView.findViewById(R.id.text_view_score);
        highScoreTextView = (TextView) rootView.findViewById(R.id.text_view_high_score);

        highScoreTextView.setText(String.valueOf(App.getAppPreferences().getHighScore()));

        Button newGame = (Button) rootView.findViewById(R.id.button_new_game);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameManager.onNewGameClicked();
            }
        });

        if (savedInstanceState != null) {
            Game game = savedInstanceState.getParcelable("game");
            if (game != null) {
                game.setGameInterface(gameManager);
                gameManager.setGame(game);
                updateScore(String.valueOf(game.getScore()));
                updateHighScore(String.valueOf(App.getAppPreferences().getHighScore()));
            }
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("game", gameManager.getGame());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getEventBus().unregister(this);
        App.getEventBus().unregister(gameManager);
    }

    @Override
    public void updateScore(String score) {
        scoreTextView.setText(score);
    }

    @Override
    public void updateHighScore(String highScore) {
        highScoreTextView.setText(highScore);
    }

    @Override
    public void updateCountdown(String secondsLeft) {
        countdownTextView.setText(secondsLeft);
    }
}

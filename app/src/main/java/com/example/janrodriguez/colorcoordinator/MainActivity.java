package com.example.janrodriguez.colorcoordinator;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.Color;
import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    enum BtnColors {
        Red (Color.RED),
        Green (Color.GREEN),
        Blue (Color.BLUE),
        Yellow (Color.YELLOW);

        private int value;

        private BtnColors(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final int UPDATE_COUNTDOWN = 0;
    private final int UPDATE_TURN_TIME = 1;
    private final int UPDATE_START_GAME = 2;
    private final int END_GAME = 3;
    private int score;
    private boolean isPlaying = false;
    private boolean canClick = false;

    private BtnColors currentColor;
    private int currentColorIndex;
    private Random random = new Random();
    private static final List<BtnColors> COLORS_LIST =
            Collections.unmodifiableList(Arrays.asList(BtnColors.values()));
    private static final int COLORS_SIZE = COLORS_LIST.size();

    private Button colorIndicatorBtn;
    Button startBtn;
    private TextView scoreText;
    private TextView countdownText;

    private Thread timer;
    private Animation growShrinkScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initialize () {
        growShrinkScore  = AnimationUtils.loadAnimation(MainActivity.this, R.anim.grow_shrink_score);
        setViews();
        setNewColor();
        setColorButtonListeners();
        resetTimer();

    }

    private void resetTimer () {
        isPlaying = false;

        final Handler timerHandler = new Handler(Looper.getMainLooper()) {

            Animation growShrink = AnimationUtils.loadAnimation(MainActivity.this, R.anim.grow_shrink);
            AnimatorSet blueToRedAnim = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.color_yellow_to_red);

            private void setCountdownText(String value) {
                countdownText.setText(value);
            }

            private void animateCountdownText() {
                countdownText.startAnimation(growShrink);
                blueToRedAnim.setTarget(countdownText);
                blueToRedAnim.start();
            }

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case UPDATE_COUNTDOWN:
                        setCountdownText(String.valueOf(msg.obj));
                        animateCountdownText();
                        break;
                    case UPDATE_START_GAME:
                        setCountdownText("GO!");
                        colorIndicatorBtn.setVisibility(View.VISIBLE);
                        countdownText.setTextColor(Color.BLACK);
                        break;
                    case UPDATE_TURN_TIME:
                        setCountdownText(String.valueOf(msg.obj));
                        break;
                    case END_GAME:
                        endGame();
                        break;
                }
            }
        };

        Runnable timerRunnable = new Runnable() {

            int timeRemaining = 15000;
            boolean interrupted;

            @Override
            public void run() {
                startCountdown(3);

                while (!interrupted && timeRemaining > 0){
                    try {
                        Thread.sleep(59);
                    } catch (InterruptedException e) {
                        break;
                    }
                    timeRemaining -= 59;
                    Message handlerMsg = timerHandler.obtainMessage();
                    handlerMsg.obj = timeRemaining;
                    handlerMsg.what = UPDATE_TURN_TIME;
                    handlerMsg.sendToTarget();
                    interrupted = Thread.currentThread().isInterrupted();
                }
                Message handlerMsg = timerHandler.obtainMessage();
                handlerMsg.what = END_GAME;
                handlerMsg.sendToTarget();
            }

            private void startCountdown (int seconds) {
                for(int i = seconds; i > 0; i--) {
                    Message handlerMsg = timerHandler.obtainMessage();
                    handlerMsg.obj = i;
                    handlerMsg.what = UPDATE_COUNTDOWN;
                    handlerMsg.sendToTarget();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message handlerMsg = timerHandler.obtainMessage();
                handlerMsg.what = UPDATE_START_GAME;
                handlerMsg.sendToTarget();

                canClick = true;
            }

        };


        timer = new Thread(timerRunnable);
    }

    private void startGame () {
        if(!isPlaying) {
            startBtn.setVisibility(View.INVISIBLE);
            timer.start();
            isPlaying = true;
            score = 0;
            updateScoreText();
        }
    }

    private void setViews () {
        colorIndicatorBtn = (Button)findViewById(R.id.currentColorBtn);
        scoreText = (TextView)findViewById(R.id.scoreText);
        countdownText = (TextView)findViewById(R.id.countDownText);
    }

    private void setColorButtonListeners() {

        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

        //Color buttons
        Button redBtn = (Button) findViewById(R.id.redBtn);
        redBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBtn(BtnColors.Red);
            }
        });

        Button greenBtn = (Button) findViewById(R.id.greenBtn);
        greenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBtn(BtnColors.Green);
            }
        });

        Button blueBtn = (Button) findViewById(R.id.blueBtn);
        blueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBtn(BtnColors.Blue);
            }
        });

        Button yellowBtn = (Button) findViewById(R.id.yellowBtn);
        yellowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBtn(BtnColors.Yellow);
            }
        });

    }

    private void clickBtn (BtnColors color) {
        if(canClick) {
            if(color == currentColor) {
                success();
                setNewColor();
            }else{
                endGame();
            }
        }
    }

    private void success () {
        score++;
        updateScoreText();
    }

    private void endGame() {
        timer.interrupt();
        resetTimer();
        canClick = false;
        startBtn.setText("Retry");
        startBtn.setVisibility(View.VISIBLE);
        colorIndicatorBtn.setVisibility(View.INVISIBLE);
        countdownText.setText("Try again!");
    }

    private void updateScoreText () {
        scoreText.setText(String.valueOf(score));
        scoreText.startAnimation(growShrinkScore);
    }

    private void setNewColor () {
        int randomIndex = random.nextInt(COLORS_SIZE);

        //Make sure same color doesn't appear twice
        if(randomIndex == currentColorIndex) {
            randomIndex = (randomIndex + 1) % COLORS_SIZE;
        }

        currentColorIndex = randomIndex;
        currentColor = COLORS_LIST.get(randomIndex);

        colorIndicatorBtn.setBackgroundColor(currentColor.getValue());
    }


}

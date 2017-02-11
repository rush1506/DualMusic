package com.tata.dualmusic;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import model.Music;

public class PlaySongsActivity extends AppCompatActivity {

    private TextView title;
    private TextView artist;
    private SeekBar s1_seekBar;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    private TextView s1_TimeTextLeft;
    private TextView s1_TimeTextRight;
    private Button s1_ButtonPlay;
    private Button s1_ButtonSkipForward;
    private Button s1_ButtonSkipBackward;
    private Button s1_ButtonPause;
    MediaPlayer s1;


    public static int currentPlayedSong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_songs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String JSONsong;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            JSONsong = extras.getString("song");
            final Music currentMusic = new Gson().fromJson(JSONsong, Music.class);

            title = (TextView) findViewById(R.id.support_play_songs_Title);
            artist = (TextView) findViewById(R.id.support_play_songs_Artist);
            s1_seekBar = (SeekBar) findViewById(R.id.support_play_songs_Seekbar);
            s1_TimeTextLeft = (TextView) findViewById(R.id.support_play_songs_TimeTextLeft);
            s1_TimeTextRight = (TextView) findViewById(R.id.support_play_songs_TimeTextRight);
            s1_ButtonPlay = (Button) findViewById(R.id.support_play_songs_buttonPlay);
            s1_ButtonSkipForward = (Button) findViewById(R.id.support_play_songs_buttonSkipForward);
            s1_ButtonSkipBackward = (Button) findViewById(R.id.support_play_songs_SkipBackward);
            s1_ButtonPause = (Button) findViewById(R.id.support_play_songs_buttonPause);

            title.setText(currentMusic.getMUSIC_TITLE());
            artist.setText(currentMusic.getMUSIC_ARTIST());

            s1_ButtonPause.setEnabled(false);
            s1_ButtonPlay.setEnabled(true);


            s1 = new MediaPlayer();
            try {
                s1.setDataSource(currentMusic.getMUSIC_PATH());
                s1.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }


            s1_ButtonPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    s1_ButtonPause.setEnabled(true);
                    s1_ButtonPlay.setEnabled(false);


                    s1.start();

                    finalTime = s1.getDuration();
                    startTime = s1.getCurrentPosition();


                    if (currentPlayedSong == 0) {
                        s1_seekBar.setMax((int) finalTime);
                        currentPlayedSong = 1;
                    }

                    s1_TimeTextRight.setText(String.format("%d min, %d sec",
                            TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
                    );

                    s1_TimeTextLeft.setText(String.format("%d min, %d sec",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
                    );

                    s1_seekBar.setProgress((int) startTime);
                    myHandler.postDelayed(UpdateSongTime, 100);
                    myHandler.postDelayed(UpdateProgressSeekBar, 100);


                }
            });

            s1_ButtonPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Toast.makeText(getApplicationContext(), "Pausing sound",Toast.LENGTH_SHORT).show();
                    s1.pause();
                    s1_ButtonPause.setEnabled(false);
                    s1_ButtonPlay.setEnabled(true);
                    //currentPlayedSong = 0;

                }
            });

            s1_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {


                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                    myHandler.removeCallbacks(UpdateProgressSeekBar);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {


                    int newProgress = seekBar.getProgress();
                    s1.seekTo(newProgress);
                    s1.start();
                    myHandler.post(UpdateProgressSeekBar);

                }
            });


        } else
            Toast.makeText(PlaySongsActivity.this, "Can't display music", Toast.LENGTH_SHORT).show();

    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = (double) s1.getCurrentPosition();
            s1_TimeTextLeft.setText(String.format("%d min, %d sec",

                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            s1_seekBar.setProgress((int) startTime);

            myHandler.postDelayed(this, 100);
        }
    };

    private Runnable UpdateProgressSeekBar = new Runnable() {
        @Override
        public void run() {

            double tmpStartTime = (double) s1.getCurrentPosition();
            s1_seekBar.setProgress((int) tmpStartTime);
            myHandler.postDelayed(this, 100);

        }
    };

}

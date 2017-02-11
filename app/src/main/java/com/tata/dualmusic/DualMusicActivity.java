package com.tata.dualmusic;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import data.MusicDatabaseHandler;
import model.Music;

public class DualMusicActivity extends AppCompatActivity {

    private SlidingPaneLayout slidePane;
    private MusicAdapter musicAdapter;
    private ArrayList<Music> musicsdb = new ArrayList<>();
    private MusicDatabaseHandler musicDatabaseHandler;
    private songListViewHolder songList = new songListViewHolder();
    private InfoAdapter infoAdpter;
    private ControlAdapter controlAdpater;
    private int InitListNumber;
    private ArrayList<MediaPlayer> mediaList;
    private ArrayList<Music> chosenSongList;
    //private Queue<MediaPlayer> mediaList;
    private double startTime[];
    private double finalTime[];
    private boolean currentPlayedSong[];
    private Handler myHandler = new Handler();
    private ListView songInfoList;
    private ListView songControlList;
    private int uniPos;
    private PanelControlViewHolder uniHolder;
    private boolean isPrepared[];
    private ImageView goBack;
    private ImageView searchButton;
    private final int SEARCH_SONG_REQUEST_CODE = 6969;
    private boolean isShuffled[];
    private final int maxVolume = 100;
    private int volumeList[];
    private NotificationCompat.Builder mBuilder[];

    int songChosenCount = 0;

    /*
    * Know problem: Button pause and play is not set up correctly
    * skip song when it's not playing (begin stage) -> click play to enable things
    * Volume is laggy (probable too much code)
    * */

    @Override
    protected void onDestroy() {
        super.onDestroy();


        //End music
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_music);

        SharedPreferences prefs = getSharedPreferences("user_settings", MODE_PRIVATE);
        InitListNumber = prefs.getInt(getString(R.string.input_song_numbers), 0);

        InitializeControlScreen(InitListNumber);

        DisplaySongs();
        slidePane = (SlidingPaneLayout) findViewById(R.id.dual_music_content_slidepanel);
        goBack = (ImageView) findViewById(R.id.dual_music_menu_item_GoBack);
        searchButton = (ImageView) findViewById(R.id.dual_music_menu_item_Search);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start search activity
                Intent search = new Intent(DualMusicActivity.this, DualMusicSearchActivity.class);
                search.putExtra("Init", InitListNumber);

                startActivityForResult(search, SEARCH_SONG_REQUEST_CODE);
                //
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goBack = new Intent(DualMusicActivity.this, MainMenu.class);
                //goBack.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //For main menu launcher
                //goBack.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(goBack);
                //DualMusicActivity.this.finish();
            }
        });

        slidePane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelOpened(View panel) {

                DisplayControl();

            }

            @Override
            public void onPanelClosed(View panel) {

                myHandler.removeCallbacks(UpdateSongTime);
                myHandler.removeCallbacks(UpdateProgressSeekBar);

                SharedPreferences prefs = getSharedPreferences("first_run", MODE_PRIVATE);
                //boolean prevStarted = prefs.getBoolean(getString(R.string.pref_previously_started_slidepanel), false);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(getString(R.string.pref_previously_started_slidepanel), Boolean.FALSE);
                edit.commit();


            }
        });


    }


    private void InitializeControlScreen(int InitNumber) {

        mediaList = new ArrayList<>(InitNumber);
        startTime = new double[InitNumber];
        finalTime = new double[InitNumber];
        chosenSongList = new ArrayList<>(InitNumber);
        currentPlayedSong = new boolean[InitNumber];
        isPrepared = new boolean[InitNumber];
        isShuffled = new boolean[InitNumber];
        volumeList = new int[InitNumber];
        mBuilder = new NotificationCompat.Builder[InitNumber];

        for (int i = 0; i < InitNumber; i++) {

            isPrepared[i] = false;
            isShuffled[i] = false;
            volumeList[i] = maxVolume;

        }
    }


    /*
    *
    * Start taking care of the song list and stufs
    *
    *
    * */


    private void DisplaySongs() {

        musicsdb.clear();
        musicDatabaseHandler = new MusicDatabaseHandler(getApplicationContext());

        ArrayList<Music> tmp = musicDatabaseHandler.getMusicArrayList();

        for (int i = 0; i < tmp.size(); i++) {

            Music tmpMusic = new Music();

            tmpMusic.setMUSIC_TITLE(tmp.get(i).getMUSIC_TITLE());
            tmpMusic.setMUSIC_PATH(tmp.get(i).getMUSIC_PATH());
            tmpMusic.setMUSIC_DURATION(tmp.get(i).getMUSIC_DURATION());
            tmpMusic.setMUSIC_DISPLAY_NAME(tmp.get(i).getMUSIC_DISPLAY_NAME());
            tmpMusic.setMUSIC_ALBUM(tmp.get(i).getMUSIC_ALBUM());
            tmpMusic.setMUSIC_ARTIST(tmp.get(i).getMUSIC_ARTIST());
            tmpMusic.setMUSIC_ID(tmp.get(i).getMUSIC_ID());

            musicsdb.add(tmpMusic);

        }
        musicDatabaseHandler.close();

        songList.songList = (ListView) findViewById(R.id.dual_music_content_SongsList);

        //set Adapter

        musicAdapter = new MusicAdapter(DualMusicActivity.this, R.layout.content_dual_music_content_row, musicsdb);
        songList.songList.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();


    }

    class songListViewHolder {

        ListView songList;

    }

    private class ViewHolder {

        TextView mTitle;
        TextView mArtis;
        Music mMusic;
        int mID;

    }

    private class MusicAdapter extends ArrayAdapter<Music> {

        Activity activity;
        int layoutResource;
        ArrayList<Music> mData;


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder holder = null;

            if (row == null || (row.getTag() == null)) {

                holder = new ViewHolder();
                LayoutInflater inf = LayoutInflater.from(activity);
                row = inf.inflate(layoutResource, null);

                holder.mTitle = (TextView) row.findViewById(R.id.content_dual_music_row_Title);
                holder.mArtis = (TextView) row.findViewById(R.id.content_dual_music_row_Artist);

                row.setTag(holder);

            } else {

                holder = (ViewHolder) row.getTag();
            }

            holder.mMusic = getItem(position);

            holder.mID = holder.mMusic.getMUSIC_ID();
            holder.mTitle.setText(holder.mMusic.getMUSIC_TITLE());
            holder.mArtis.setText(holder.mMusic.getMUSIC_ARTIST());

            final Music chosenMusic = holder.mMusic;

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //adjust shit here (Done)
                    //Pass music item to slidepane
                    /*
                    *   The song has been added to the queue
                    *
                    *
                    * */
                    if (chosenSongList.contains(chosenMusic)) {
                        Toast.makeText(DualMusicActivity.this, "This song has just been chosen", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (songChosenCount >= InitListNumber) {


                        if (mediaList.get(0).isPlaying()) {

                            myHandler.removeCallbacks(UpdateSongTime);
                            myHandler.removeCallbacks(UpdateProgressSeekBar);

                        }

                        /*push*/

                        for (int i = 0; i < InitListNumber - 1; i++) {

                            isPrepared[i] = isPrepared[i + 1];
                            volumeList[i] = volumeList [i + 1];

                        }
                        isPrepared[InitListNumber - 1] = false;
                        volumeList[InitListNumber - 1] = 100;
                        mediaList.get(0).release();
                        mediaList.remove(0);
                        chosenSongList.remove(0);

                        Music tmp;
                        tmp = chosenMusic;


                        chosenSongList.add(tmp);
                        MediaPlayer tmpMed = new MediaPlayer();

                        try {
                            tmpMed.setDataSource(chosenMusic.getMUSIC_PATH());
                            mediaList.add(tmpMed);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    } else {

                        Music tmp;
                        mBuilder[songChosenCount] = new NotificationCompat.Builder(getContext());
                        mBuilder[songChosenCount].setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                        mBuilder[songChosenCount].setContent()

                        tmp = chosenMusic;

                        chosenSongList.add(tmp);
                        MediaPlayer tmpMed = new MediaPlayer();

                        try {
                            tmpMed.setDataSource(chosenMusic.getMUSIC_PATH());
                            mediaList.add(tmpMed);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        songChosenCount++;

                    }
                }
            });


            return row;
        }

        public MusicAdapter(Activity act, int resource, ArrayList<Music> data) {
            super(act, resource, data);
            activity = act;
            layoutResource = resource;
            mData = data;
            notifyDataSetChanged();

        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Music getItem(int position) {
            return mData.get(position);
        }

        @Override
        public int getPosition(Music item) {
            return super.getPosition(item);
        }

        @Override
        public long getItemId(int position) {
            return mData.get(position).getMUSIC_ID();
        }
    }

    /*
    *
    * End taking care of the song list and stufs
    * Start taking care of the control slide (lots of shits)
    *
    * */

    private class PanelInfoViewHolder {

        private TextView mTitle;
        private TextView mAtist;

    }

    private class PanelControlViewHolder {

        private SeekBar mSeekBar;
        private TextView mTimeTextLeft;
        private TextView mTimeTextRight;
        private Button mButtonPlay;
        private Button mButtonSkipForward;
        private Button mButtonSkipBackward;
        private Button mButtonPause;
        private Button mButtonLoop;
        private Button mButtonShuffle;
        private SeekBar mVolume;

    }

    private void DisplayControl() {
        //setAdapter
        infoAdpter = new InfoAdapter(DualMusicActivity.this,
                R.layout.content_dual_music_slidepanel_playsongs_row_song_list, chosenSongList);
        controlAdpater = new ControlAdapter(DualMusicActivity.this,
                R.layout.content_dual_music_slidepanel_playsongs_row_control, mediaList);

        songInfoList = (ListView) findViewById(R.id.dual_music_content_slidepanel_SongInfo);
        songControlList = (ListView) findViewById(R.id.dual_music_content_slidepanel_SongControl);

        songInfoList.setAdapter(infoAdpter);
        songControlList.setAdapter(controlAdpater);
        infoAdpter.notifyDataSetChanged();
        controlAdpater.notifyDataSetChanged();


    }

    private class InfoAdapter extends BaseAdapter {

        Activity activity;
        int layoutResource;
        ArrayList<Music> mData = new ArrayList<>();

        public InfoAdapter(Activity activity, int resource, ArrayList<Music> data) {
            this.activity = activity;
            this.layoutResource = resource;
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View row = view;
            PanelInfoViewHolder holder = null;

            if (row == null || (row.getTag() == null)) {

                LayoutInflater inflater = LayoutInflater.from(activity);
                row = inflater.inflate(layoutResource, null);
                holder = new PanelInfoViewHolder();

                holder.mTitle = (TextView) row.findViewById(R.id.notification_Title);
                holder.mAtist = (TextView) row.findViewById(R.id.content_dual_music_slidepanel_playsongs_row_Artist);

                row.setTag(holder);

            } else {

                holder = (PanelInfoViewHolder) row.getTag();

            }
            holder.mTitle.setText(mData.get(i).getMUSIC_TITLE());
            holder.mAtist.setText(mData.get(i).getMUSIC_ARTIST());


            return row;
        }
    }

    private class ControlAdapter extends ArrayAdapter<MediaPlayer> {

        Activity activity;
        int layoutResource;
        ArrayList<MediaPlayer> mData;

        public ControlAdapter(Activity act, int resource, ArrayList<MediaPlayer> data) {
            super(act, resource, data);
            activity = act;
            layoutResource = resource;
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public MediaPlayer getItem(int position) {
            return mData.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View row = convertView;
            PanelControlViewHolder holder = null;

            if (row == null || (row.getTag() == null)) {

                LayoutInflater inflater = LayoutInflater.from(activity);
                row = inflater.inflate(layoutResource, null);
                holder = new PanelControlViewHolder();

                holder.mSeekBar = (SeekBar) row.findViewById(R.id.content_dual_music_slidepanel_row_control_Seekbar);
                holder.mButtonPause = (Button) row.findViewById(R.id.notification_buttonPause);
                holder.mButtonPlay = (Button) row.findViewById(R.id.notification_buttonPlay);
                holder.mButtonSkipForward = (Button) row.findViewById(R.id.notification_buttonSkipForward);
                holder.mButtonSkipBackward = (Button) row.findViewById(R.id.notification_buttonSkipBackward);
                holder.mButtonLoop = (Button) row.findViewById(R.id.content_dual_music_slidepanel_row_control_buttonLoop);
                holder.mButtonShuffle = (Button) row.findViewById(R.id.content_dual_music_slidepanel_row_control_buttonShuffle);
                holder.mTimeTextLeft = (TextView) row.findViewById(R.id.content_dual_music_slidepanel_row_control_TimeTextLeft);
                holder.mTimeTextRight = (TextView) row.findViewById(R.id.content_dual_music_slidepanel_row_control_TimeTextRight);
                holder.mVolume = (SeekBar) row.findViewById(R.id.content_dual_music_slidepanel_row_control_seekVolume);


                row.setTag(holder);


            } else {

                holder = (PanelControlViewHolder) row.getTag();

            }

            //Implement button

            holder.mButtonPause.setEnabled(false);
            holder.mButtonPlay.setEnabled(true);

            final PanelControlViewHolder finalHolder = holder;

            try {
                if (!isPrepared[position]) {

                    /*\
                    *
                    * Current problem: how to check if the song is PREPARED
                    * IT CRASH BECAUSE IT HAS BEEN PREPARED AND WE'RE PREPARING IT AGAIN
                    *
                    * class prepareAsync???
                    * UPDATE:
                    * FIXED OKAY: Create a is Prepared flags :'(
                    * */
                    mediaList.get(position).prepare();
                    isPrepared[position] = true;



                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            holder.mVolume.setMax(maxVolume);
            holder.mVolume.setProgress(volumeList[position]);

            mediaList.get(position).setVolume(toVolumeRange(volumeList[position]), toVolumeRange(volumeList[position]));


            holder.mButtonPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalHolder.mButtonPause.setEnabled(true);
                    finalHolder.mButtonPlay.setEnabled(false);


                    mediaList.get(position).start();

                    finalTime[position] = mediaList.get(position).getDuration();
                    startTime[position] = mediaList.get(position).getCurrentPosition();

                    finalHolder.mSeekBar.setMax((int) finalTime[position]);

                    finalHolder.mTimeTextRight.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes((long) finalTime[position]),
                            TimeUnit.MILLISECONDS.toSeconds((long) finalTime[position]) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime[position])))
                    );

                    finalHolder.mTimeTextLeft.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime[position]),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime[position]) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime[position])))
                    );

                    finalHolder.mSeekBar.setProgress((int) startTime[position]);

                    uniPos = position;
                    uniHolder = finalHolder;

                    myHandler.postDelayed(UpdateSongTime, 100);
                    myHandler.postDelayed(UpdateProgressSeekBar, 100);


                }
            });


            holder.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
                    mediaList.get(position).seekTo(newProgress);
                    mediaList.get(position).start();
                    uniPos = position;
                    uniHolder = finalHolder;
                    myHandler.post(UpdateProgressSeekBar);

                }
            });

            holder.mButtonPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Toast.makeText(getApplicationContext(), "Pausing sound",Toast.LENGTH_SHORT).show();
                    mediaList.get(position).pause();
                    finalHolder.mButtonPause.setEnabled(false);
                    finalHolder.mButtonPlay.setEnabled(true);
                    //currentPlayedSong = 0;

                }
            });

            holder.mVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    int newProgress = seekBar.getProgress();
                    volumeList[position] = newProgress;
                    finalHolder.mVolume.setProgress(newProgress);
                    mediaList.get(position).setVolume(toVolumeRange(volumeList[position]), toVolumeRange(volumeList[position]));

                }
            });


            holder.mButtonLoop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mediaList.get(position).isLooping()) {
                        mediaList.get(position).setLooping(true);
                        finalHolder.mButtonLoop.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.music_loop_button_clicked));


                    } else {
                        mediaList.get(position).setLooping(false);
                        finalHolder.mButtonLoop.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.music_loop_button));
                    }
                }
            });

            holder.mButtonShuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isShuffled[position]) {

                        isShuffled[position] = true;
                        finalHolder.mButtonShuffle.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.musc_shuffle_button_clicked));

                    } else {

                        isShuffled[position] = false;
                        finalHolder.mButtonShuffle.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.musc_shuffle_button));

                    }
                }
            });


            int prevPos = -1;
            mediaList.get(position).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //get new song
                    if (mediaPlayer.isLooping())
                        return;

                    if (!isShuffled[position]) {

                        mediaPlayer.reset();
                        int dbPos = musicsdb.indexOf(chosenSongList.get(position));

                        if (musicsdb.size() == 1 ||
                                musicsdb.size() == chosenSongList.size()) {
                            return;
                        }

                        boolean minus = false;
                        while (chosenSongList.contains(musicsdb.get(dbPos))) {
                            if (dbPos >= musicsdb.size() - 1) {
                                dbPos--;
                                minus = true;
                            } else if (!minus) {
                                dbPos++;
                            } else {
                                dbPos--;
                            }
                        }

                        chosenSongList.set(position, musicsdb.get(dbPos));
                        try {
                            mediaPlayer.setDataSource(musicsdb.get(dbPos).getMUSIC_PATH());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {

                        mediaPlayer.reset();
                        int dbPos = musicsdb.indexOf(chosenSongList.get(position));

                        if (musicsdb.size() == 1 ||
                                musicsdb.size() == chosenSongList.size()) {
                            return;
                        }

                        Random r = new Random();
                        int newPos = r.nextInt(musicsdb.size());

                        boolean minus = false;
                        while (chosenSongList.contains(musicsdb.get(newPos))) {
                            if (newPos >= musicsdb.size() - 1) {
                                newPos--;
                                minus = true;
                            } else if (!minus) {
                                newPos++;
                            } else {
                                newPos--;
                            }
                        }

                        chosenSongList.set(position, musicsdb.get(newPos));
                        try {
                            mediaPlayer.setDataSource(musicsdb.get(newPos).getMUSIC_PATH());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    try {
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    infoAdpter.notifyDataSetChanged();
                    controlAdpater.notifyDataSetChanged();


                }
            });

            holder.mButtonSkipForward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    boolean isLooped = false;
                    //get new song
                    if (mediaList.get(position).isLooping()) {
                        isLooped = true;
                    }

                    if (!isShuffled[position]) {

                        mediaList.get(position).reset();
                        int dbPos = musicsdb.indexOf(chosenSongList.get(position));

                        if (musicsdb.size() == 1 ||
                                musicsdb.size() == chosenSongList.size()) {
                            return;
                        }

                        boolean minus = false;
                        while (chosenSongList.contains(musicsdb.get(dbPos))) {
                            if (dbPos >= musicsdb.size() - 1) {
                                dbPos--;
                                minus = true;
                            } else if (!minus) {
                                dbPos++;
                            } else {
                                dbPos--;
                            }

                            if (dbPos < 0) {
                                dbPos++;
                                minus = false;
                            }
                        }

                        chosenSongList.set(position, musicsdb.get(dbPos));
                        try {
                            mediaList.get(position).setDataSource(musicsdb.get(dbPos).getMUSIC_PATH());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {

                        mediaList.get(position).reset();
                        int dbPos = musicsdb.indexOf(chosenSongList.get(position));

                        if (musicsdb.size() == 1 ||
                                musicsdb.size() == chosenSongList.size()) {
                            return;
                        }

                        Random r = new Random();
                        int newPos = r.nextInt(musicsdb.size());

                        boolean minus = false;
                        while (chosenSongList.contains(musicsdb.get(newPos)) || (newPos == dbPos)) {
                            if (newPos >= musicsdb.size() - 1) {
                                newPos--;
                                minus = true;
                            } else if (!minus) {
                                newPos++;
                            } else {
                                newPos--;
                            }

                            if (newPos < 0) {
                                newPos++;
                                minus = false;
                            }
                        }

                        chosenSongList.set(position, musicsdb.get(newPos));
                        try {
                            mediaList.get(position).setDataSource(musicsdb.get(newPos).getMUSIC_PATH());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    try {

                        mediaList.get(position).prepare();
                        isPrepared[position] = true;
                        finalHolder.mButtonPause.setEnabled(true);
                        finalHolder.mButtonPlay.setEnabled(false);


                        mediaList.get(position).start();

                        finalTime[position] = mediaList.get(position).getDuration();
                        startTime[position] = mediaList.get(position).getCurrentPosition();

                        finalHolder.mSeekBar.setMax((int) finalTime[position]);

                        finalHolder.mTimeTextRight.setText(String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes((long) finalTime[position]),
                                TimeUnit.MILLISECONDS.toSeconds((long) finalTime[position]) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime[position])))
                        );

                        finalHolder.mTimeTextLeft.setText(String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes((long) startTime[position]),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime[position]) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime[position])))
                        );

                        finalHolder.mSeekBar.setProgress((int) startTime[position]);

                        uniPos = position;
                        uniHolder = finalHolder;

                        myHandler.postDelayed(UpdateSongTime, 100);
                        myHandler.postDelayed(UpdateProgressSeekBar, 100);

                        if (isLooped)
                            mediaList.get(position).setLooping(true);



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    infoAdpter.notifyDataSetChanged();
                    controlAdpater.notifyDataSetChanged();


                }
            });

            holder.mButtonSkipBackward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    finalHolder.mButtonPause.setEnabled(true);
                    finalHolder.mButtonPlay.setEnabled(false);
                    boolean isLooped = false;
                    //get new song
                    if (mediaList.get(position).isLooping()) {
                        isLooped = true;
                    }

                    if (!isShuffled[position]) {

                        mediaList.get(position).reset();
                        int dbPos = musicsdb.indexOf(chosenSongList.get(position));

                        if (musicsdb.size() == 1 ||
                                musicsdb.size() == chosenSongList.size()) {
                            return;
                        }

                        boolean minus = true;
                        while (chosenSongList.contains(musicsdb.get(dbPos))) {
                            if (dbPos <= 0) {
                                dbPos++;
                                minus = false;
                            } else if (minus) {
                                dbPos--;
                            } else {
                                dbPos++;
                            }

                            if (dbPos >= musicsdb.size() - 1) {
                                dbPos--;
                                minus = true;
                            }
                        }

                        chosenSongList.set(position, musicsdb.get(dbPos));
                        try {
                            mediaList.get(position).setDataSource(musicsdb.get(dbPos).getMUSIC_PATH());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {

                        mediaList.get(position).reset();
                        int dbPos = musicsdb.indexOf(chosenSongList.get(position));

                        if (musicsdb.size() == 1 ||
                                musicsdb.size() == chosenSongList.size()) {
                            return;
                        }

                        Random r = new Random();
                        int newPos = r.nextInt(musicsdb.size());

                        boolean minus = false;
                        while (chosenSongList.contains(musicsdb.get(newPos)) || (newPos == dbPos)) {
                            if (newPos >= musicsdb.size() - 1) {
                                newPos--;
                                minus = true;
                            } else if (!minus) {
                                newPos++;
                            } else {
                                newPos--;
                            }

                            if (newPos < 0) {
                                newPos++;
                                minus = false;
                            }
                        }

                        chosenSongList.set(position, musicsdb.get(newPos));
                        try {
                            mediaList.get(position).setDataSource(musicsdb.get(newPos).getMUSIC_PATH());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    try {

                        mediaList.get(position).prepare();
                        isPrepared[position] = true;
                        finalHolder.mButtonPause.setEnabled(true);
                        finalHolder.mButtonPlay.setEnabled(false);


                        mediaList.get(position).start();

                        finalTime[position] = mediaList.get(position).getDuration();
                        startTime[position] = mediaList.get(position).getCurrentPosition();

                        finalHolder.mSeekBar.setMax((int) finalTime[position]);

                        finalHolder.mTimeTextRight.setText(String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes((long) finalTime[position]),
                                TimeUnit.MILLISECONDS.toSeconds((long) finalTime[position]) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime[position])))
                        );

                        finalHolder.mTimeTextLeft.setText(String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes((long) startTime[position]),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime[position]) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime[position])))
                        );

                        finalHolder.mSeekBar.setProgress((int) startTime[position]);

                        uniPos = position;
                        uniHolder = finalHolder;

                        myHandler.postDelayed(UpdateSongTime, 100);
                        myHandler.postDelayed(UpdateProgressSeekBar, 100);

                        if (isLooped)
                            mediaList.get(position).setLooping(true);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    infoAdpter.notifyDataSetChanged();
                    controlAdpater.notifyDataSetChanged();


                }
            });


            return row;
        }


    }

    private float toVolumeRange(int number) {

        float result;
        result = (float) (1 - (Math.log(maxVolume - number) / Math.log(maxVolume)));
        return result;
    }


    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime[uniPos] = (double) mediaList.get(uniPos).getCurrentPosition();
            uniHolder.mTimeTextLeft.setText(String.format("%02d:%02d",

                    TimeUnit.MILLISECONDS.toMinutes((long) startTime[uniPos]),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime[uniPos]) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime[uniPos])))
            );
            uniHolder.mSeekBar.setProgress((int) startTime[uniPos]);
            myHandler.postDelayed(this, 100);
        }
    };


    private Runnable UpdateProgressSeekBar = new Runnable() {
        @Override
        public void run() {

            double tmpStartTime = (double) mediaList.get(uniPos).getCurrentPosition();
            uniHolder.mSeekBar.setProgress((int) tmpStartTime);
            myHandler.postDelayed(this, 100);

        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEARCH_SONG_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                //get data
                ArrayList<Music> musicDataFromSearch = data.getParcelableArrayListExtra("MusicList");

                //full size

                if (musicDataFromSearch.size() == InitListNumber
                        && chosenSongList.size() == musicDataFromSearch.size()) {

                    //replace all

                    for (int i = 0; i < InitListNumber; i++) {

                        isPrepared[i] = false;
                        mediaList.get(i).release();
                        mediaList.remove(i);
                        chosenSongList.remove(i);

                        Music tmp = musicDataFromSearch.get(i);

                        chosenSongList.add(i, tmp);


                        MediaPlayer tmpMed = new MediaPlayer();

                        try {
                            tmpMed.setDataSource(tmp.getMUSIC_PATH());
                            mediaList.add(i, tmpMed);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }


                } else if (chosenSongList.size() == 0
                        && musicDataFromSearch.size() > 0) {

                    for (int i = 0; i < musicDataFromSearch.size(); i++) {

                        isPrepared[i] = false;

                        Music tmp = musicDataFromSearch.get(i);

                        chosenSongList.add(tmp);


                        MediaPlayer tmpMed = new MediaPlayer();

                        try {
                            tmpMed.setDataSource(tmp.getMUSIC_PATH());
                            mediaList.add(tmpMed);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                } else if (musicDataFromSearch.size() == 0) {
                    //do nothing
                } else {

                    int i = 0;
                    while (chosenSongList.size() < InitListNumber) {

                        Music tmp = musicDataFromSearch.get(i);

                        chosenSongList.add(tmp);


                        MediaPlayer tmpMed = new MediaPlayer();

                        try {
                            tmpMed.setDataSource(tmp.getMUSIC_PATH());
                            mediaList.add(tmpMed);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        songChosenCount++;
                        i++;
                    }

                    while (i < musicDataFromSearch.size()) {

                        if (mediaList.get(0).isPlaying()) {

                            myHandler.removeCallbacks(UpdateSongTime);
                            myHandler.removeCallbacks(UpdateProgressSeekBar);

                        }

                        /*push*/

                        for (int j = 0; j < InitListNumber - 1; j++) {

                            isPrepared[j] = isPrepared[j + 1];

                        }
                        isPrepared[InitListNumber - 1] = false;
                        mediaList.get(0).release();
                        mediaList.remove(0);
                        chosenSongList.remove(0);

                        Music tmp = musicDataFromSearch.get(i);


                        chosenSongList.add(tmp);
                        MediaPlayer tmpMed = new MediaPlayer();

                        try {
                            tmpMed.setDataSource(tmp.getMUSIC_PATH());
                            mediaList.add(tmpMed);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        i++;
                    }


                }

                //end gathering data

                DisplayControl();

            }

        }

    }
}





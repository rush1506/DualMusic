package com.tata.dualmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import data.MusicDatabaseHandler;
import model.Music;

public class DualMusicSearchActivity extends AppCompatActivity {

    private EditText SearchBox;
    private ImageView SearchOk;
    private Handler searchHandler = new Handler();
    private int InitNumber;
    private MusicDatabaseHandler musicDatabaseHandler;
    private ArrayList<Music> musicsdb = new ArrayList<>();
    private ArrayList<Music> chosenMusic = new ArrayList<>();
    private MusicAdapter musicAdapter;
    private ListView songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_music_search);

        musicDatabaseHandler = new MusicDatabaseHandler(getApplicationContext());
        SearchBox = (EditText) findViewById(R.id.dual_music_search_menu_item_SearchBox);
        SearchOk = (ImageView) findViewById(R.id.dual_music_search_menu_item_SearchOk);

        Intent data;
        data = getIntent();
        InitNumber = data.getIntExtra("Init", 0);

        DisplayScreen();

        SearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_NULL
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN)  {

                    searchHandler.post(UpdateSearchList);

                }
                return true;
            }
        });


        SearchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start Update Search list
                searchHandler.postDelayed(UpdateSearchList, 100);

            }


        });

        SearchOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //go back
                //setResult(RESULT_OK);
                Intent goBack = new Intent(DualMusicSearchActivity.this, DualMusicActivity.class);

                goBack.putParcelableArrayListExtra("MusicList", chosenMusic);


                musicDatabaseHandler.close();

                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, goBack);
                }
                else {
                    getParent().setResult(Activity.RESULT_OK, goBack);
                }

                //startActivity(goBack);
                DualMusicSearchActivity.this.finish();

            }


        });

    }

    private Runnable UpdateSearchList = new Runnable() {
        @Override
        public void run() {
            //Update search list on real time
            String searchString = SearchBox.getText().toString();

            musicsdb.clear();
                musicDatabaseHandler = new MusicDatabaseHandler(getApplicationContext());

            ArrayList<Music> tmp = musicDatabaseHandler.searchMusic(searchString);

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
                musicAdapter.notifyDataSetChanged();

            //stop here
            searchHandler.postDelayed(this, 1000);

        }
    };

    private void DisplayScreen() {

        //set Adapter
        songList = (ListView) findViewById(R.id.dual_music_search_content_SongList);
        musicAdapter = new MusicAdapter(DualMusicSearchActivity.this, R.layout.content_dual_music_content_row, musicsdb);
        songList.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();


    }

    private class MusicAdapter extends ArrayAdapter<Music> {

        Activity activity;
        int layoutRes;
        ArrayList<Music> mData;

        public MusicAdapter(Activity act, int resource, ArrayList<Music> data) {
            super(act, resource, data);
            activity = act;
            layoutRes = resource;
            mData = data;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder holder = null;

            if (row == null || (row.getTag() == null)) {

                holder = new ViewHolder();
                LayoutInflater inf = LayoutInflater.from(activity);
                row = inf.inflate(layoutRes, null);

                holder.mTitle = (TextView) row.findViewById(R.id.content_dual_music_row_Title);
                holder.mArtist = (TextView) row.findViewById(R.id.content_dual_music_row_Artist);

                row.setTag(holder);

            } else {

                holder = (ViewHolder) row.getTag();
            }

            holder.mMusic = getItem(position);

            holder.mID = holder.mMusic.getMUSIC_ID();
            holder.mTitle.setText(holder.mMusic.getMUSIC_TITLE());
            holder.mArtist.setText(holder.mMusic.getMUSIC_ARTIST());

            final ViewHolder finalHolder = holder;

            holder.mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(DualMusicSearchActivity.this, "Your song has been added", Toast.LENGTH_SHORT).show();
                    if (chosenMusic.size() >= InitNumber) {

                        chosenMusic.remove(0);

                        if (chosenMusic.contains(finalHolder.mMusic)) {
                            //Do nothing
                            Toast.makeText(DualMusicSearchActivity.this, "Song is chosen before", Toast.LENGTH_SHORT).show();
                        } else {
                            chosenMusic.add(finalHolder.mMusic);
                        }


                    } else {

                        if (chosenMusic.contains(finalHolder.mMusic)) {
                            //Do nothing
                            Toast.makeText(DualMusicSearchActivity.this, "Song has been chosen before", Toast.LENGTH_SHORT).show();
                        } else {
                            chosenMusic.add(finalHolder.mMusic);
                        }


                    }

                }
            });

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(DualMusicSearchActivity.this, "Your song has been added", Toast.LENGTH_SHORT).show();
                    if (chosenMusic.size() >= InitNumber) {

                        chosenMusic.remove(0);

                        if (chosenMusic.contains(finalHolder.mMusic)) {
                            //Do nothing
                            Toast.makeText(DualMusicSearchActivity.this, "Song is chosen before", Toast.LENGTH_SHORT).show();
                        } else {
                            chosenMusic.add(finalHolder.mMusic);
                        }


                    } else {

                        if (chosenMusic.contains(finalHolder.mMusic)) {
                            //Do nothing
                            Toast.makeText(DualMusicSearchActivity.this, "Song is chosen before", Toast.LENGTH_SHORT).show();
                        } else {
                            chosenMusic.add(finalHolder.mMusic);
                        }


                    }

                }
            });


            return row;
        }

        class ViewHolder {

            TextView mTitle;
            TextView mArtist;
            Music mMusic;
            int mID;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

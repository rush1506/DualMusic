package com.tata.dualmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import data.MusicDatabaseHandler;
import model.Music;

public class SongsActivity extends AppCompatActivity {

    private ListView songsListView;
    private MusicAdapter musicAdapter;
    private ArrayList<Music> musicsdb = new ArrayList<>();
    private MusicDatabaseHandler musicDatabaseHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_songs_Revert);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SongsActivity.this, MainMenu.class));
                SongsActivity.this.finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DisplaySongs();
    }

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


        songsListView = (ListView) findViewById(R.id.activity_songs_List);
        //set Adapter

        musicAdapter = new MusicAdapter(SongsActivity.this, R.layout.support_songs_row, musicsdb);
        songsListView.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();


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
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder holder = null;

            if (row == null || (row.getTag() == null)) {

                holder = new ViewHolder();
                LayoutInflater inf = LayoutInflater.from(activity);
                row = inf.inflate(layoutResource, null);

                holder.mTitle = (TextView) row.findViewById(R.id.support_songs_row_Title);
                holder.mArtis = (TextView) row.findViewById(R.id.support_songs_row_Artist);

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

                    Intent intent = new Intent(SongsActivity.this, PlaySongsActivity.class);
                    intent.putExtra("song", new Gson().toJson(chosenMusic));

                    startActivity(intent);
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

}

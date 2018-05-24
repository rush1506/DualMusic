package com.tata.dualmusic;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import data.MusicDatabaseHandler;
import model.MainListDefinition;
import model.Music;

public class MainMenu extends AppCompatActivity {

    private ListView main_menu_List;
    private MainMenuListAdapter mainMenuListAdapter;
    private final int REQUEST_CODE_READ_DATA = 1506;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        main_menu_List = (ListView) findViewById(R.id.main_menu_Choice);
        mainMenuListAdapter = new MainMenuListAdapter(MainMenu.this, R.layout.support_main_menu_row);
        main_menu_List.setAdapter(mainMenuListAdapter);

        FirstTimeRun();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_READ_DATA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    ScanForMusicList scanMusic = new ScanForMusicList();
                    scanMusic.execute();
                } else {
                    Toast.makeText(MainMenu.this, "The app need to read your permission to work", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void FirstTimeRun() {
        //Load music database if haven't load

        SharedPreferences prefs = getSharedPreferences("first_run", MODE_PRIVATE);
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.commit();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_DATA);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_DATA);
                }
            } else {
                //Load data
                ScanForMusicList scanMusic = new ScanForMusicList();
                scanMusic.execute();
            }


        } else {
            //Laugh :v
            // Toast.makeText(MainMenu.this, "BEEN THERE DONE THAT", Toast.LENGTH_SHORT).show();
        }

    }

    private class ScanForMusicList extends AsyncTask<Void, Void, Void> {

        final CharSequence[] waitDialog = {"I know that you're in a hurry...BUT", "..We're scanning for songs :v :(("};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
        AlertDialog alertDialog;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                Toast.makeText(MainMenu.this, "Okay, Done ♥", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.setTitle("Please be patient");
            builder.setItems(waitDialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MainMenu.this, "Please wait a little bit more :(", Toast.LENGTH_SHORT).show();
                }
            });
            alertDialog = builder.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {


            MusicDatabaseHandler dbHandler;
            dbHandler = new MusicDatabaseHandler(getApplicationContext());
            String[] STAR = {"*"};
            Uri allsongsuri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            Cursor cursor = getContentResolver().query(allsongsuri, STAR, selection, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String song_title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        int song_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE_KEY));

                        String fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                        String album_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                        int album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                        String artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        int artist_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));

                        String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        int display_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                        String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                        Music tmp;
                        tmp = new Music();

                        tmp.setMUSIC_ALBUM(album_name);
                        tmp.setMUSIC_TITLE(song_title);
                        tmp.setMUSIC_DISPLAY_NAME(display_name);
                        tmp.setMUSIC_DURATION(duration);
                        tmp.setMUSIC_PATH(fullpath);
                        tmp.setMUSIC_ARTIST(artist_name);

                        dbHandler.addMusic(tmp);

                    } while (cursor.moveToNext());

                    dbHandler.close();

                }
                cursor.close();
            }

            return null;
        }
    }


    private class MainMenuListAdapter extends BaseAdapter {

        Activity activity;
        int layoutResource;

        public MainMenuListAdapter(Activity activity, int resource) {
            this.activity = activity;
            this.layoutResource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder holder = null;

            //Create menu and using it this way to allow usser's custom modification in the future
            if (row == null || (row.getTag() == null)) {

                LayoutInflater inflater = LayoutInflater.from(activity);
                row = inflater.inflate(layoutResource, null);
                holder = new ViewHolder();

                holder.mImage = (ImageView) row.findViewById(R.id.support_menu_thumbnail);
                holder.mTitle = (TextView) row.findViewById(R.id.support_menu_title);

                //recycle the view
                row.setTag(holder);


                } else {
                    holder = (ViewHolder) row.getTag();
                }

            holder.mTitle.setText(MainListDefinition.getTitle(position));
            holder.mImage.setImageURI(null);
            holder.mImage.setImageURI(MainListDefinition.getImagePathUri(position));

            final int pos = position;

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tmp;
                    tmp = MainListDefinition.getTitle(pos);
                    switch (tmp) {
                        case "Songs": {
                            startActivity(new Intent(MainMenu.this, SongsActivity.class));
                            break;
                        }
                        case "Playlists": {
                            //start playlists
                            break;
                        }
                        case "Dual Music": {
                            //start Dual Music
                            SharedPreferences prefs = getSharedPreferences("first_run", MODE_PRIVATE);
                            boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started_input_song_number), false);
                            if (!previouslyStarted) {
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putBoolean(getString(R.string.pref_previously_started_input_song_number), Boolean.TRUE);
                                edit.commit();


                                //Load data
                                showDualMusicInputDialog();


                            } else {
                                //start normally
                                Intent DualMusicIntent = new Intent(MainMenu.this, DualMusicActivity.class);

                                startActivity(DualMusicIntent);
                            }

                            break;
                        }
                        case "Folders": {
                            startActivity(new Intent(MainMenu.this, FindFolders.class));
                            break;
                        }
                        case "Artists": {
                            //start artists
                            break;
                        }
                        case "Albums": {
                            //start albums
                            break;
                        }
                        default:
                            break;

                    }
                }
            });

            return row;
        }


        @Override
        public int getCount() {
            return MainListDefinition.count;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean isEmpty() {
            Toast.makeText(MainMenu.this,
                    "I'm sorry something is wrong :( \nCode 404: menu not found!\nPlease post this in the review section.",
                    Toast.LENGTH_LONG).show();
            return false;
        }


        private class ViewHolder {
            ImageView mImage;
            TextView mTitle;
        }


    }


    protected void showDualMusicInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainMenu.this);
        View promptView = layoutInflater.inflate(R.layout.content_dual_music_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainMenu.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.content_dual_dialog_EditText);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        SharedPreferences prefs = getSharedPreferences("user_settings", MODE_PRIVATE);
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.putInt(getString(R.string.input_song_numbers), Integer.parseInt(editText.getText().toString()));
                        edit.commit();

                        if (Integer.parseInt(editText.getText().toString().trim()) <= 1) {
                            startActivity(new Intent(MainMenu.this, SongsActivity.class));
                        }

                        Intent DualMusicIntent = new Intent(MainMenu.this, DualMusicActivity.class);
                        //DualMusicIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        DualMusicIntent.putExtra("Numbers", Integer.parseInt(editText.getText().toString().trim()));

                        startActivity(DualMusicIntent);


                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}

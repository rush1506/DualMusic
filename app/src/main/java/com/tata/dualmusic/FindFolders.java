package com.tata.dualmusic;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import data.FolderPathDatabaseHandler;
import model.FolderPath;

public class FindFolders extends AppCompatActivity {

    private ListView find_folders_list;
    private FindFoldersAdapter findFoldersAdapter;
    private FloatingActionButton newFolders;
    private final int GET_FOLDER_PATH_REQUEST = 1509;
    private FolderPathDatabaseHandler Folderdba;
    private ArrayList<FolderPath> dbFolerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_folders);



        find_folders_list = (ListView) findViewById(R.id.find_folders_List);
        newFolders = (FloatingActionButton) findViewById(R.id.find_folders_NewFolderButton);

        getDataFromDatabase();

    }



    private void getDataFromDatabase() {

        dbFolerList.clear();
        Folderdba = new FolderPathDatabaseHandler(getApplicationContext());

        ArrayList<FolderPath> foldersFromDB = Folderdba.getPaths();

        for (int i = 0; i < foldersFromDB.size(); i ++) {

            String tmpPath =   foldersFromDB.get(i).getmPath();
            int tmpId = foldersFromDB.get(i).getmID();

            FolderPath tmpFolder = new FolderPath();
            tmpFolder.setmPath(tmpPath);
            tmpFolder.setmID(tmpId);

            dbFolerList.add(tmpFolder);
        }

        Folderdba.close();



        //adapter

        findFoldersAdapter = new FindFoldersAdapter(FindFolders.this, R.layout.support_find_folders_row, dbFolerList);
        find_folders_list.setAdapter(findFoldersAdapter);
        findFoldersAdapter.notifyDataSetChanged();

        newFolders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, GET_FOLDER_PATH_REQUEST);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == GET_FOLDER_PATH_REQUEST) {

                if (resultCode == RESULT_OK) {


                    FolderPathDatabaseHandler db = new FolderPathDatabaseHandler(getApplicationContext());
                    Uri UriFPath = data.getData();

                    if (UriFPath == null) {

                        Toast.makeText(FindFolders.this, "Some how we cannot get the file path :(", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String Fpath = UriFPath.toString();


                    File file = new File(Fpath);
                    file = new File(file.getAbsolutePath());
                    Fpath = file.getParent();

                    //Toast.makeText(FindFolders.this, file.getAbsolutePath(), Toast.LENGTH_LONG).show();

                    FolderPath folderPath = new FolderPath();
                    folderPath.setmPath(Fpath);

                   // Toast.makeText(FindFolders.this, Fpath, Toast.LENGTH_LONG).show();

                    db.addPath(folderPath);
                    db.close();


                    startActivity(new Intent(FindFolders.this, FindFolders.class));
                    FindFolders.this.finish();
                }

            }


        } catch (Exception e) {
            Toast.makeText(FindFolders.this, "Cannot Retrieve Path!", Toast.LENGTH_SHORT).show();
        }


    }

    private class FindFoldersAdapter extends ArrayAdapter<FolderPath> {

        Activity activity;
        int layoutResource;
        ArrayList<FolderPath> mData;

        public FindFoldersAdapter(Activity act, int resource, ArrayList<FolderPath> data) {
            super(act, resource, data);
            activity = act;
            layoutResource = resource;
            mData = data;

          //  Toast.makeText(FindFolders.this, "SIZE: " + String.valueOf(mData.size()), Toast.LENGTH_LONG).show();

            //Wrong size?
            notifyDataSetChanged();
        }

        @Override
        public FolderPath getItem(int position) {
            return mData.get(position);
        }

        @Override
        public int getPosition(FolderPath item) {
            return super.getPosition(item);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder holder = null;


            if (row == null || (row.getTag() == null)) {

                LayoutInflater layoutInflater = LayoutInflater.from(activity);
                row = layoutInflater.inflate(layoutResource, null);
                holder = new ViewHolder();

                holder.mPath = (TextView) row.findViewById(R.id.support_find_folders_path);
                holder.mDeleteButton = (Button) row.findViewById(R.id.support_find_folders_Delete);

                row.setTag(holder);
            } else {

                holder = (ViewHolder) row.getTag();

            }

            holder.mFolder = getItem(position);
            holder.mID = holder.mFolder.getmID();
            holder.mPath.setText(holder.mFolder.getmPath());

            final ViewHolder finalHolder = holder;

            holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FolderPathDatabaseHandler db = new FolderPathDatabaseHandler(getApplicationContext());
                    db.deletePath(finalHolder.mID);
                    db.close();

                    Toast.makeText(FindFolders.this, "Path deleted!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(FindFolders.this, FindFolders.class));
                    FindFolders.this.finish();
                }
            });


            return row;
        }

        private class ViewHolder {

            TextView mPath;
            int mID;
            FolderPath mFolder;
            Button mDeleteButton;


        }


    }
}

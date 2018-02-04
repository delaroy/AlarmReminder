package com.delaroystudios.alarmreminder;


import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;

import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.delaroystudios.alarmreminder.data.AlarmReminderContract;
import com.delaroystudios.alarmreminder.data.AlarmReminderDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private FloatingActionButton mAddReminderButton;
    private Toolbar mToolbar;
    AlarmCursorAdapter mCursorAdapter;
    AlarmReminderDbHelper alarmReminderDbHelper = new AlarmReminderDbHelper(this);
    ListView reminderListView;
    ProgressDialog prgDialog;
    TextView reminderText;

    private String alarmTitle = "";

    private static final int VEHICLE_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.app_name);


        reminderListView = (ListView) findViewById(R.id.list);
        reminderText = (TextView) findViewById(R.id.reminderText);


        View emptyView = findViewById(R.id.empty_view);
        reminderListView.setEmptyView(emptyView);

        mCursorAdapter = new AlarmCursorAdapter(this, null);
        reminderListView.setAdapter(mCursorAdapter);

        reminderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);

                Uri currentVehicleUri = ContentUris.withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentVehicleUri);

                startActivity(intent);

            }
        });


        mAddReminderButton = (FloatingActionButton) findViewById(R.id.fab);

        mAddReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(v.getContext(), AddReminderActivity.class);
                //startActivity(intent);
                addReminderTitle();
            }
        });

        getSupportLoaderManager().initLoader(VEHICLE_LOADER, null, this);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                AlarmReminderContract.AlarmReminderEntry._ID,
                AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
                AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
                AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO,
                AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE,
                AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE

        };

        return new CursorLoader(this,   // Parent activity context
                AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0){
            reminderText.setVisibility(View.VISIBLE);
        }else{
            reminderText.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }

    public void addReminderTitle(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Reminder Title");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().isEmpty()){
                    return;
                }

                alarmTitle = input.getText().toString();
                ContentValues values = new ContentValues();

                values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, alarmTitle);

                Uri newUri = getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

                restartLoader();


                if (newUri == null) {
                    Toast.makeText(getApplicationContext(), "Setting Reminder Title failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Title set successfully", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void restartLoader(){
        getSupportLoaderManager().restartLoader(VEHICLE_LOADER, null, this);
    }
}

package com.lasa.mynotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.ui.AppBarConfiguration;

import com.lasa.mynotes.databinding.ActivityStartBinding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import util.DBHelper;
import util.NoteDataPOJO;
import util.WriteCSVFile;

public class StartActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private ActivityStartBinding binding;

    ListView listView;

    ImageView refreshButton, downloadButton;
    TextView dataMsgText;

    DBHelper dbHelper;

    NoteDataPOJO noteDataPOJO;

    WriteCSVFile writeCSVFile = new WriteCSVFile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dbHelper = new DBHelper(getApplicationContext());

        listView = findViewById(R.id.list_view);
        refreshButton = findViewById(R.id.refresh_list);
        downloadButton = findViewById(R.id.download_data);
        dataMsgText = findViewById(R.id.data_msg);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDataListSec();
                Toast.makeText(getApplicationContext(), "Refreshed", Toast.LENGTH_LONG).show();
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                builder.setTitle("Are you sure?");
                builder.setMessage("Do you want to download the data?")
                        .setCancelable(false)
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doFileExportOp();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        showDataListSec();  // on load show the note list

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        });
    }
    //*** end of oncreate method ***

    public void showDataListSec() {
        NoteDataPOJO noteDataVal = getNoteData();

        if(noteDataVal.getNoteDataList().size() > 0) {

            dataMsgText.setVisibility(View.INVISIBLE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    noteDataVal.getNoteDataList()
            );

            listView.setAdapter(adapter);

            // Set an OnItemClickListener
            /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the clicked item
                    String selectedItem = noteDataVal.getNoteDataList().get(position);

                    String selectedUniqueID = noteDataVal.getUniqueIDList().get(position);

                }
            });*/

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                    builder.setTitle("Are you sure");
                    builder.setMessage("You are about to delete the data. Delete the data?")
                            .setCancelable(false)
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String selectedUniqueID = noteDataVal.getUniqueIDList().get(position);

                                    int delReturnVal = dbHelper.deleteMyAccelerometerData(selectedUniqueID);

                                    if (delReturnVal > 0) {
                                        noteDataVal.getNoteDataList().remove(position);
                                        noteDataVal.getUniqueIDList().remove(position);
                                        adapter.notifyDataSetChanged();

                                        Toast.makeText(getApplicationContext(), "Deleted successfully", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed to delete", Toast.LENGTH_LONG).show();
                                    }

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    return true;
                }
            });

        }
        else {
            dataMsgText.setText("No data found!");
        }

    }

    public NoteDataPOJO getNoteData() {

        NoteDataPOJO noteDataList = new NoteDataPOJO();

        ArrayList<String> noteDataUniqueIDList = new ArrayList<>();
        ArrayList<String> noteDataSet = new ArrayList<>();

        Cursor cursor = dbHelper.getAllData();
        cursor.moveToFirst();

        if (cursor.moveToFirst()) {
            do {
                String id           = cursor.getString(cursor.getColumnIndexOrThrow ("id"));
                String lat          = cursor.getString(cursor.getColumnIndexOrThrow ("lat"));
                String lng          = cursor.getString(cursor.getColumnIndexOrThrow ("lng"));
                String xVal         = cursor.getString(cursor.getColumnIndexOrThrow ("x_val"));
                String yVal         = cursor.getString(cursor.getColumnIndexOrThrow ("y_val"));
                String zVal         = cursor.getString(cursor.getColumnIndexOrThrow ("z_val"));
                String roadName     = cursor.getString(cursor.getColumnIndexOrThrow ("road_name"));
                String vehicleName  = cursor.getString(cursor.getColumnIndexOrThrow ("vehicle_name"));
                String speed        = cursor.getString(cursor.getColumnIndexOrThrow("speed"));
                String dateVal      = cursor.getString(cursor.getColumnIndexOrThrow ("date_val"));
                String startTime    = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                String uniqueID     = cursor.getString(cursor.getColumnIndexOrThrow ("unique_id"));

                noteDataUniqueIDList.add(id);
                noteDataSet.add("\n"+roadName+"\n"+vehicleName+"\n");

            } while (cursor.moveToNext());

            noteDataList.setUniqueIDList(noteDataUniqueIDList);
            noteDataList.setNoteDataList(noteDataSet);
        }

        return noteDataList;

        //System.out.println("Note::"+noteDataSet.toString());

    }

    // method to fetch data from the sqlite db and write to a file
    public void doFileExportOp() {

        String newFileName = writeCSVFile.getCsvFileName();

        Handler handler = new Handler(Looper.getMainLooper());

        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            Cursor cursor = dbHelper.getAllData();
            cursor.moveToFirst();

            FileOutputStream fos = null;
            BufferedWriter bw = null;

            try {
                // Open file output stream
                fos = new FileOutputStream(new File(newFileName));
                bw = new BufferedWriter(new OutputStreamWriter(fos));

                // CSV header
                String csvHeader = "id, lat, long, x, y, z, heading, body, speed, date, stime, uid";

                bw.write(csvHeader);
                bw.newLine();

                if (cursor.moveToFirst()) {
                    do {
                        String id           = cursor.getString(cursor.getColumnIndexOrThrow ("id"));
                        String lat          = cursor.getString(cursor.getColumnIndexOrThrow ("lat"));
                        String lng          = cursor.getString(cursor.getColumnIndexOrThrow ("lng"));
                        String xVal         = cursor.getString(cursor.getColumnIndexOrThrow ("x_val"));
                        String yVal         = cursor.getString(cursor.getColumnIndexOrThrow ("y_val"));
                        String zVal         = cursor.getString(cursor.getColumnIndexOrThrow ("z_val"));
                        String roadName     = cursor.getString(cursor.getColumnIndexOrThrow ("road_name"));
                        String vehicleName  = cursor.getString(cursor.getColumnIndexOrThrow ("vehicle_name"));
                        String speed        = cursor.getString(cursor.getColumnIndexOrThrow("speed"));
                        String dateVal      = cursor.getString(cursor.getColumnIndexOrThrow ("date_val"));
                        String startTime    = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                        String uniqueID     = cursor.getString(cursor.getColumnIndexOrThrow ("unique_id"));

                        // Write data
                        bw.write(id +","+ lat +","+ lng +","+ xVal +","+ yVal +","+ zVal +","+ roadName +","+ vehicleName +","+ speed +","+ dateVal +","+ startTime +","+ uniqueID);
                        bw.newLine();

                    } while (cursor.moveToNext());
                }

                // Flush and close the writer
                bw.flush();

            } catch (IOException e) {
                e.printStackTrace();  // Handle IO exceptions
            } finally {
                // Close resources
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                    if (cursor != null) {
                        cursor.close();  // Close the cursor
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            handler.post(() -> {
                System.out.println("CSV file written successfully to " + newFileName);
                Toast.makeText(getApplicationContext(), "CSV file written successfully to " + newFileName, Toast.LENGTH_SHORT).show();
            });

        }).start();

    }
    //*** end of method ***

}
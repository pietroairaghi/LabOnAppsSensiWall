package project.labonappssensiwall;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    //enum COLOR {BLACK, GREY, BLUE, CLIMBING};

    private static final String TAG = "TestActivityJacopo";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<StringWithTag> listDevices = new ArrayList<>();
    private List<StringWithTag> listDivisions = new ArrayList<>();

    private String sessionID;

    // Selected variables for new drawing (used when push button DRAW)
    private String selectedDevice;
    private int selectedDivision;
    private String selectedColorHex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // get intent with session id
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        sessionID = extras.getString(SessionActivity.SESSION_ID);

        // Get devices from DB and populate spinner
        getSessionDevices();

        // Get divisions from DB and populate spinner
        getSessionDivisions();

        // Set dafault selected color
        setColor(R.id.buttonRed);
    }

    private void getSessionDevices() {

        // read database with doc ID, set screen and division spinners
        CollectionReference devices =  db.collection("sessions/"+sessionID+"/devices");

        devices.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String deviceName = document.get("name").toString();
                        String deviceID = document.getId();
                        Log.d(TAG,deviceID + " - " + deviceName);
                        listDevices.add(new StringWithTag(deviceName, deviceID));
                    }

                    populateSpinnerDevices();

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    private void populateSpinnerDevices(){

        // Create new spinner for devices selection
        Spinner spinnerDevices = findViewById(R.id.spinnerDevices);
        ArrayAdapter<StringWithTag> adapter = new ArrayAdapter<> (this, android.R.layout.simple_spinner_item, listDevices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevices.setAdapter(adapter);

        // Set on select functions
        spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                String tag = s.tag;
                String string = s.string;

                // Set selected for drawing
                selectedDevice = tag;

                // Notify the selected item text
                Toast toast = Toast.makeText(getApplicationContext(), "Selected : " + string + ", ID: "+tag, Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

    }

    private void getSessionDivisions() {

        DocumentReference session =  db.collection("sessions/"+sessionID+"/settings").document("divisions");

        session.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        int divisions = document.getDouble("value").intValue();
                        for(int i = 1; i <= divisions; i++){
                            listDivisions.add(new StringWithTag("Division "+i, Integer.toString(i)));
                        }

                        populateSpinnerDivisions();

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void populateSpinnerDivisions() {

        // Create new spinner for division selection
        Spinner spinnerDivisions = findViewById(R.id.spinnerDivisions);
        ArrayAdapter<StringWithTag> adapter = new ArrayAdapter<> (getApplicationContext(), android.R.layout.simple_spinner_item, listDivisions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivisions.setAdapter(adapter);

        // Set on select functions
        spinnerDivisions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                String tag = s.tag;
                String string = s.string;

                // Set selected for drawing
                selectedDivision = Integer.parseInt(tag);

                // Notify the selected item text
                Toast toast = Toast.makeText(getApplicationContext(), "Selected : " + string + ", ID: "+ tag, Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

    }


    // Set color functions
    public void clickedbuttonRed(View view) {
        setColor(R.id.buttonRed);
        selectedColorHex = "#FF0000";
    }

    public void clickedbuttonOrange(View view) {
        setColor(R.id.buttonOrange);
        selectedColorHex = "#FF6600";
    }

    public void clickedbuttonYellow(View view) {
        setColor(R.id.buttonYellow);
        selectedColorHex = "#FFFF00";
    }

    public void clickedbuttonGreen(View view) {
        setColor(R.id.buttonGreen);
        selectedColorHex = "#009933";
    }

    public void clickedbuttonBlue(View view) {
        setColor(R.id.buttonBlue);
        selectedColorHex = "#0000FF";
    }

    public void clickedbuttonViolet(View view) {
        setColor(R.id.buttonViolet);
        selectedColorHex = "#6600CC";
    }

    public void setColor(int selectedColor) {

        // disable all button
        //List<Button> buttons = new ArrayList<Button>();
        int[] BUTTON_IDS = {
                R.id.buttonRed,
                R.id.buttonOrange,
                R.id.buttonYellow,
                R.id.buttonGreen,
                R.id.buttonBlue,
                R.id.buttonViolet,
        };

        for(int id : BUTTON_IDS) {
            Button button = (Button)findViewById(id);

            if(id == selectedColor)
            {
                button.setEnabled(false);

                // Notify the selected item text
                Toast toast = Toast.makeText(getApplicationContext(), "Selected color: " + selectedColorHex, Toast.LENGTH_SHORT);
                toast.show();

            } else {
                button.setEnabled(true);
            }
        }

        Button button = (Button)findViewById(selectedColor);
    }

}

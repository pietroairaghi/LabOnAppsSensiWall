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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    //enum COLOR {BLACK, GREY, BLUE, CLIMBING};

    private static final String TAG = "TestActivityJacopo";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<StringWithTag> listDevices = new ArrayList<>();
    private List<String> listDevicesID = new ArrayList<>();
    private List<StringWithTag> listDivisions = new ArrayList<>();

    private String sessionID;
    private SessionSettings settings;

    // Selected variables for new drawing (used when push button DRAW)
    private String selectedDevice;
    private int selectedDivision;
    private String selectedColorHex;
    private String selectedShape;
    private int positionX, positionY, scale;


    // Seekbars
    private SeekBar seekBarPositionX, seekBarPositionY, seekBarScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // get intent with session id
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        sessionID = extras.getString(SessionActivity.SESSION_ID);

        settings = new SessionSettings(sessionID, getApplicationContext());

        // Get devices from DB and populate spinner
        getSessionDevices();

        // Get divisions from DB and populate spinner
        settings.setListener(new SessionSettings.sessionSettingsListener() {
            @Override
            public void onCompleteLoading() {
                populateSpinnerDivisions();
            }
        });

        // Set dafault selected color
        selectedColorHex = "#FF0000";
        setColor(R.id.buttonRed);

        // Set default shape
        selectedShape = "square";

        // Set seekbars
        seekBarPositionX = (SeekBar) findViewById(R.id.seekBarPositionX);
        seekBarPositionY = (SeekBar) findViewById(R.id.seekBarPositionY);
        seekBarScale = (SeekBar) findViewById(R.id.seekBarScale);
        setSeekBar(seekBarPositionX);
        setSeekBar(seekBarPositionY);
        setSeekBar(seekBarScale);
    }

    private void getSessionDevices() {

        // read database with doc ID, set screen and division spinners
        CollectionReference devices =  db.collection("sessions/"+sessionID+"/devices");

        devices.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String deviceID = document.getId();
                        listDevicesID.add(deviceID);
                    }

                    completeDevicesList(); //TODO: better if there would be a join? dkn

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    private void completeDevicesList(){

        for (String deviceID : listDevicesID){
            final int[] i = {1};
            DocumentReference docRef = db.collection("devices").document(deviceID);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String deviceName = document.get("name").toString();
                            String deviceID = document.getId();
                            Log.d(TAG,deviceID + " - " + deviceName);
                            listDevices.add(new StringWithTag(deviceName, deviceID));

                            if(listDevicesID.size() <= i[0]){
                                populateSpinnerDevices();
                            }else{
                                i[0]++;
                            }

                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
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
                //toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

    }

    private void populateSpinnerDivisions() {

        int divisions = settings.getInt("divisions");

        for(int i = 1; i <= divisions; i++){
            listDivisions.add(new StringWithTag("Division "+i, Integer.toString(i)));
        }

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
                //toast.show();
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
                //toast.show();

            } else {
                button.setEnabled(true);
            }
        }

        Button button = (Button)findViewById(selectedColor);
    }

    public void clickedbuttonSquare(View view) {
        selectedShape = "square";

        // Notify the selected item text
        Toast toast = Toast.makeText(getApplicationContext(), "Selected shape: " + selectedShape, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void clickedbuttonTriangle(View view) {
        selectedShape = "triangle";

        // Notify the selected item text
        Toast toast = Toast.makeText(getApplicationContext(), "Selected shape: " + selectedShape, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void clickedbuttonPentagon(View view) {
        selectedShape = "pentagon";

        // Notify the selected item text
        Toast toast = Toast.makeText(getApplicationContext(), "Selected shape: " + selectedShape, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void clickedbuttonHexagon(View view) {
        selectedShape = "hexagon";

        // Notify the selected item text
        Toast toast = Toast.makeText(getApplicationContext(), "Selected shape: " + selectedShape, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void clickedbuttonOctagon(View view) {
        selectedShape = "octagon";

        // Notify the selected item text
        Toast toast = Toast.makeText(getApplicationContext(), "Selected shape: " + selectedShape, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void clickedbuttonCircle(View view) {
        selectedShape = "circle";

        // Notify the selected item text
        Toast toast = Toast.makeText(getApplicationContext(), "Selected shape: " + selectedShape, Toast.LENGTH_SHORT);
        toast.show();
    }


    private void setSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
               // progress = progresValue;
                //Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
               // Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setText("Covered: " + progress + "/" + seekBar.getMax());

                int progress = seekBar.getProgress();

                if(seekBar == seekBarPositionX){
                    positionX = progress;
                    Toast.makeText(getApplicationContext(), "Position X: " + positionX, Toast.LENGTH_SHORT).show();
                }

                if(seekBar == seekBarPositionY){
                    positionY = progress;
                    Toast.makeText(getApplicationContext(), "Position Y: " + positionY, Toast.LENGTH_SHORT).show();
                }

                if(seekBar == seekBarScale){
                    scale = progress;
                    Toast.makeText(getApplicationContext(), "Scale: " + scale, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public void clickedbuttonDraw(View view) {

        Map<String, Object> newDrawing = new HashMap<>();
        newDrawing.put("shape", selectedShape);
        newDrawing.put("color", selectedColorHex);
        newDrawing.put("division", selectedDivision);
        newDrawing.put("positionx", (float)positionX/10);
        newDrawing.put("positiony", (float)positionY/10);
        newDrawing.put("scale", (float)scale/10);


        db.collection("sessions/"+sessionID+"/devices/"+selectedDevice+"/drawings").document()
                .set(newDrawing)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast toast = Toast.makeText(getApplicationContext(), "Drawing successfully sent! ", Toast.LENGTH_SHORT);
                        toast.show();

                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    public void clickedbuttonClear(View view) {

        // read database with doc ID, set screen and division spinners
        CollectionReference drawings =  db.collection("sessions/"+sessionID+"/devices/jNjsPcCklbvh55hv9pdr/drawings");

        // read documents
        drawings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String drawingID = document.getId();

                        // delete doc
                        db.collection("sessions/"+sessionID+"/devices/jNjsPcCklbvh55hv9pdr/drawings").document(drawingID)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });


                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

}

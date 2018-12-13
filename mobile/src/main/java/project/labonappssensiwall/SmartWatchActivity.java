package project.labonappssensiwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartWatchActivity extends AppCompatActivity {
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
    private int selectedDivisionX;
    private int selectedDivisionY;
    private String selectedColorHex;
    private String selectedShape;
    private float positionX = 0, positionY = 0, scale = 0.3f;


    // Seekbars
    private SeekBar seekBarScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_watch);

        // get intent with session id
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        sessionID = extras.getString(SessionActivity.SESSION_ID);

        startSmartWatch();

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
        seekBarScale = (SeekBar) findViewById(R.id.seekBarScale);
        setSeekBar(seekBarScale);
    }

    private void startSmartWatch(){
        Intent intentWear = new Intent(this,WearService.class);
        intentWear.setAction(WearService.ACTION_SEND.STARTACTIVITY.name());
        intentWear.putExtra(WearService.ACTIVITY_TO_START,BuildConfig.W_drawactivity);
        intentWear.putExtra(WearService.PATH,"prova_path"); //TODO: change path name
        startService(intentWear);

        setSmartWatchListener();
    }

    private void setSmartWatchListener(){
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView textView = findViewById(R.id.textViewRes);

                float gX = intent.getFloatExtra("gX",0f);
                float gY = intent.getFloatExtra("gY",0f);
                float gZ = intent.getFloatExtra("gZ",0f);
                String shape = intent.getStringExtra("shape");

                float signY = (gX / Math.abs(gX));
                float signX = (gY / Math.abs(gY));

                double coordY = (Math.acos((9.81f - gZ)/9.81f * signY) - Math.PI/2)/(Math.PI/2);
                double coordX = (Math.asin(gY/9.81f)/(Math.PI/2))*(-1);

                if(Double.isNaN(coordY)){
                    coordY = -1*signY;
                }

                if(Double.isNaN(coordX)){
                    coordX = -1*signX;
                }


                Log.d(TAG,"coords: gX " + Float.toString(gX)
                        + " gY " + Float.toString(gY)
                        + " gZ " + Float.toString(gZ)
                        + " shape: " + shape
                        + " coordY: " + Double.toString(coordY)
                        + " coordX: " + Double.toString(coordX));

                selectedShape = shape;
                positionX = (float)(coordX+1)/2;
                positionY = (float)(coordY+1)/2;



            }
        }, new IntentFilter("STARTDRAW"));
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

        final int[] i = {1};
        for (String deviceID : listDevicesID){
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
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                    if(listDevicesID.size() <= i[0]){
                        populateSpinnerDevices();
                    }else{
                        i[0]++;
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
        Spinner spinnerDivisionsX = findViewById(R.id.spinnerDivisionsX);
        Spinner spinnerDivisionsY = findViewById(R.id.spinnerDivisionsY);
        ArrayAdapter<StringWithTag> adapter = new ArrayAdapter<> (getApplicationContext(), android.R.layout.simple_spinner_item, listDivisions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivisionsX.setAdapter(adapter);
        spinnerDivisionsY.setAdapter(adapter);

        // Set on select functions
        spinnerDivisionsX.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                String tag = s.tag;
                String string = s.string;

                // Set selected for drawing
                selectedDivisionX = Integer.parseInt(tag);

                // Notify the selected item text
                Toast toast = Toast.makeText(getApplicationContext(), "Selected : " + string + ", ID: "+ tag, Toast.LENGTH_SHORT);
                //toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        spinnerDivisionsY.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                String tag = s.tag;
                String string = s.string;

                // Set selected for drawing
                selectedDivisionY = Integer.parseInt(tag);

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


    public void startDraw(View view) {

        long millis = new Date().getTime();

        Map<String, Object> newDrawing = new HashMap<>();
        newDrawing.put("shape", selectedShape);
        newDrawing.put("color", selectedColorHex);
        newDrawing.put("divisionx", selectedDivisionX);
        newDrawing.put("divisiony", selectedDivisionY);
        newDrawing.put("positionx", positionX);
        newDrawing.put("positiony", positionY);
        newDrawing.put("scale", scale/10);
        newDrawing.put("timestamp",Long.toString(millis));


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

                if(seekBar == seekBarScale){
                    scale = progress;
                    Toast.makeText(getApplicationContext(), "Scale: " + scale, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}

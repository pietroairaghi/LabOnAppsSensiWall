package project.labonappssensiwall;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    enum COLOR {BLACK, GREY, BLUE, CLIMBING};

    private static final String TAG = "TestActivityJacopo";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<StringWithTag> list = new ArrayList<>();
    private List<StringWithTag> listDivisions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // get intent with session id
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String sessionID = extras.getString(SessionActivity.SESSION_ID);

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
                        list.add(new StringWithTag(deviceName, deviceID));
                    }

                    populateSpinnerScreen();

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        // read
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

                        // Create new spinner for devices selection
                        Spinner spinnerDivisions = findViewById(R.id.spinnerDivisions);
                        ArrayAdapter<StringWithTag> adapter = new ArrayAdapter<> (getApplicationContext(), android.R.layout.simple_spinner_item, listDivisions);
                        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                        //   R.array.planets_array, android.R.layout.simple_spinner_item);

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerDivisions.setAdapter(adapter);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        // on create call setColor setShape setScale
    }


    private void populateSpinnerScreen(){
        // Create new spinner for devices selection
        Spinner spinnerDevices = findViewById(R.id.spinnerDevices);
        ArrayAdapter<StringWithTag> adapter = new ArrayAdapter<> (this, android.R.layout.simple_spinner_item, list);
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        //   R.array.planets_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevices.setAdapter(adapter);

        spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                String tag = s.tag;
                String selectedItemText = s.string;

                // Notify the selected item text
                 Toast.makeText
                         (getApplicationContext(), "Selected : " + selectedItemText + " and ID: "+tag, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "nothing");
            }
        });

    }


    // Set color functions
    public void clickedbuttonColor(View view) {

        // call set color
    }

    public void setColor(COLOR color) {

        switch(color) {
            case BLACK:
                break;
            case GREY:
                break;
            case BLUE:
                break;
        }
    }
}

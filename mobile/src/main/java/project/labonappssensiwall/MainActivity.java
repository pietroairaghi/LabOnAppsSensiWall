package project.labonappssensiwall;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String SESSION_NAME = "Session";
    public static final String SESSION_ID = "Session ID";
    public static final String OWNER_ID = "Owner ID";
    private HashMap<String,HashMap<String,String>> sessionsList = new HashMap<>();
    private static final String TAG = "MainActivity";
    private Device device;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private RadioGroup sessionsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();

        device = new Device(this);
        device.initDeviceFS();

        device.setListener(new Device.deviceListener(){
            @Override
            public void onCompleteLoading() {
                initMain();
            }
        });
    }


    private void initMain(){
        sessionsRadioGroup = findViewById(R.id.sessionRadioGroup);

        refreshSessionList();

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSessionList();
            }
        });

        TextView tv1 = findViewById(R.id.textView2);
        tv1.setText(device.getDeviceID());

        // TODO: listener
        TextView selectSessionLabel = findViewById(R.id.selectSessionLabel);
        selectSessionLabel.setText("Welcome back " + device.getName() + "!\n Please, select a session:");
    }

    private void addRadioButtonToGroup(String text, String sessionName, String sessionID,String ownerID) {
        final int nButtons = sessionsRadioGroup.getChildCount();

        if (text == null){
            text = "new session";
        }

        RadioButton button;
        button = new RadioButton(getApplicationContext());
        button.setId(nButtons+1);
        button.setText(text);
        button.setTag(R.id.sessionID,sessionID);
        button.setTag(R.id.sessionName,sessionName);
        button.setTag(R.id.ownerID,ownerID);
        if(ownerID.equals(device.getDeviceID())){
            button.setTypeface(null,Typeface.BOLD);
        }

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);
        button.setLayoutParams(params);

        sessionsRadioGroup.addView(button);
    }

    private void removeCurrentSessions(){
        sessionsRadioGroup.clearCheck();
        sessionsRadioGroup.removeAllViews();
        sessionsList.clear();
    }


    public void refreshSessionList() {
        removeCurrentSessions();
        addRadioButtonToGroup("Create new session...","newSession","newSession","");

        CollectionReference sessions =  db.collection("sessions");

        sessions.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HashMap<String,String> currentDocument = new HashMap<>();

                        String sessionID = document.getId();
                        String ownerID = document.getString("owner");

                        currentDocument.put("sessionID",sessionID);
                        currentDocument.put("ownerID",ownerID);

                        sessionsList.put(sessionID,currentDocument);

                    }
                    completeSessionList();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    public void completeSessionList(){

        for (Map.Entry session : sessionsList.entrySet()) {
            HashMap<String,String> currentDocument = (HashMap<String, String>) session.getValue();
            final String ownerID = currentDocument.get("ownerID");
            final String sessionID = (String) session.getKey();
            DocumentReference docRef = db.collection("sessions/"+sessionID+"/settings").document("sessionName");

            // Get the document, forcing the SDK to use the default cache
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        // Document found in the offline cache
                        DocumentSnapshot document = task.getResult();
                        String sessionName = document.getString("value");
                        addRadioButtonToGroup(sessionName,sessionName,sessionID,ownerID);
                    } else {
                        String sessionName = "unknownSession";
                        addRadioButtonToGroup(sessionName,sessionName,sessionID,ownerID);
                    }
                }
            });

        }



    }

    public void startSession(View view){
        final int selectedID = sessionsRadioGroup.getCheckedRadioButtonId();
        if(selectedID == -1){
            Toast toast = Toast.makeText(this, "please select a session", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        View radioButton = sessionsRadioGroup.findViewById(selectedID);
        String sessionName = (String) radioButton.getTag(R.id.sessionName);
        String sessionID = radioButton.getTag(R.id.sessionID).toString();
        String ownerID = radioButton.getTag(R.id.ownerID).toString();

        if(ownerID.equals(device.getDeviceID()) || sessionID.equals("newSession")){
            Intent intent = new Intent(this, SessionActivity.class);
            intent.putExtra(SESSION_NAME, sessionName);
            intent.putExtra(SESSION_ID, sessionID);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, WallActivity.class);
            intent.putExtra(SESSION_ID, sessionID);
            startActivity(intent);
        }

    }
}

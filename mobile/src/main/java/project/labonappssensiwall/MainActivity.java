package project.labonappssensiwall;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    public static final String SESSION_NAME = "Session";
    public static final String SESSION_ID = "Session ID";
    public static final String OWNER_ID = "Owner ID";
    private static final String TAG = "MainActivity";
    private Device device;


    private RadioGroup sessionsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        device = new Device(this);
        device.initDeviceFS();

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


    }

    private void addRadioButtonToGroup(String text, String sessionName, String sessionID,String ownerID) {
        final int nButtons = sessionsRadioGroup.getChildCount();

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
        sessionsRadioGroup.addView(button);
    }

    private void removeCurrentSessions(){
        sessionsRadioGroup.clearCheck();
        sessionsRadioGroup.removeAllViews();
    }


    public void refreshSessionList() {
        removeCurrentSessions();
        addRadioButtonToGroup("Create new session...","newSession","newSession","");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference sessions =  db.collection("sessions");

        sessions.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String sessionID = document.getId();
                        String sessionName = document.getString("name");
                        String ownerID = document.getString("owner");
                        addRadioButtonToGroup(sessionName,sessionName,sessionID,ownerID);
                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
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

        if(ownerID.equals(device.getDeviceID())){
            Intent intent = new Intent(this, SessionActivity.class);
            intent.putExtra(SESSION_NAME, sessionName);
            intent.putExtra(SESSION_ID, sessionID);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, DisplayActivity.class);
            intent.putExtra(SESSION_ID, sessionID);
            startActivity(intent);
        }

    }

    public void provaPietro_onClick(View view) {
        Intent intent = new Intent(this, testGLPietro.class);
        startActivity(intent);
    }
}

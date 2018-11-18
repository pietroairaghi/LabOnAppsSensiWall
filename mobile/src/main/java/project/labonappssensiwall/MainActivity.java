package project.labonappssensiwall;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private static final String TAG = "MainActivity";


    private RadioGroup sessionsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionsRadioGroup = findViewById(R.id.sessionRadioGroup);

        refreshSessionList();

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSessionList();
            }
        });


    }

    private void addRadioButtonToGroup(String text, String sessionName, String sessionID) {
        final int nButtons = sessionsRadioGroup.getChildCount();

        RadioButton button;
        button = new RadioButton(getApplicationContext());
        button.setId(nButtons+1);
        button.setText(text);
        button.setTag(R.id.sessionID,sessionID);
        button.setTag(R.id.sessionName,sessionName);
        sessionsRadioGroup.addView(button);
    }

    private void removeCurrentSessions(){
        sessionsRadioGroup.clearCheck();
        sessionsRadioGroup.removeAllViews();
    }


    public void refreshSessionList() {
        removeCurrentSessions();
        addRadioButtonToGroup("Create new session...","newActivity","0");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("sessions").get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String sessionID = document.getId();
                        String sessionName = document.get("name").toString();
                        addRadioButtonToGroup(sessionName,sessionName,sessionID);
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


        Intent intent = new Intent(this, SessionActivity.class);
        intent.putExtra(SESSION_NAME, sessionName);
        intent.putExtra(SESSION_ID, sessionID);
        startActivity(intent);

    }
    

}

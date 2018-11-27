package project.labonappssensiwall;

import android.content.Intent;
import android.media.MediaCodec;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SessionActivity extends AppCompatActivity {

    public static final String SESSION_ID = "Session ID";
    private static final String TAG = "SessionActivity";

    private String sessionID; // global to be send to other activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String sessionName = extras.getString(MainActivity.SESSION_NAME);
        sessionID = extras.getString(MainActivity.SESSION_ID);

        if(sessionID.equals("newSession")) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> newSession = new HashMap<>();
            newSession.put("name", "New Session");

            db.collection("sessions").document()
                    .set(newSession)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
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

        TextView session = findViewById(R.id.sessionName);
        session.setText("Welcome to " + sessionName + "!");
    }

    public void clickedbuttonSetting(View view) {
        Intent intent = new Intent(this, SessionSettingsActivity.class);
        intent.putExtra(SESSION_ID, sessionID); // put the session ID to be read from test activty
        startActivity(intent);
    }

    public void clickedbuttonTest(View view) {
        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra(SESSION_ID, sessionID); // put the session ID to be read from test activty
        startActivity(intent);
    }

    public void clickedbuttonStart(View view) {
    }

    public void clickedbuttonDraw(View view) {
    }

    public void clickedbuttonDisplay(View view) {
        Intent intent = new Intent(this, DisplayActivity.class);
        intent.putExtra(SESSION_ID, sessionID); // put the session ID to be read from test activty
        startActivity(intent);
    }
}

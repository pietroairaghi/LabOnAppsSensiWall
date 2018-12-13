package project.labonappssensiwall;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimonGame {

    private String deviceID;
    private String sessionID;
    private gameListener listener;
    private static final String TAG = "simonGame";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SimonGame(String deviceID, String sessionID) {
        this.deviceID = deviceID;
        this.sessionID = sessionID;
        registerOnGame();
        addGameRTU();
    }

    public interface gameListener {
        void onDrawRequest();
    }

    private void registerOnGame() {
        // Update one field, creating the document if it does not already exist.
        Map<String, Object> data = new HashMap<>();
        data.put("on", false);

        db.collection("sessions/" + sessionID + "/simonGame").document(deviceID)
                .set(data, SetOptions.merge());
    }

    // Assign the listener implementing events interface that will receive the events
    public void setListener(gameListener listener) {
        this.listener = listener;
    }

    private void drawMe() {
        if (listener != null) {
            //listener.onSessionLoaded();
            listener.onDrawRequest();
        }
    }

    private String getRandomColor() {
        // create random object - reuse this as often as possible
        Random random = new Random(stringToSeed(deviceID));

        // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
        int nextInt = random.nextInt(0xffffff + 1);

        // format it as hexadecimal string (with hashtag and leading zeros)
        String colorCode = String.format("#%06x", nextInt);

        return colorCode;
    }

    static long stringToSeed(String s) {
        if (s == null) {
            return 0;
        }
        long hash = 0;
        for (char c : s.toCharArray()) {
            hash = 31L * hash + c;
        }
        return hash;
    }


    private void addGameRTU() {
        // Listen for metadata changes to the document.
        DocumentReference docRef = db.collection("session/" + sessionID + "/simonGame").document(deviceID);
        docRef.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, source + " data: " + snapshot.getData());
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });

    }

}

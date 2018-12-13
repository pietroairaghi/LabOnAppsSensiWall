package project.labonappssensiwall;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SimonGame {

    private String deviceID;
    private String sessionID;
    private gameListener listener;
    private static final String TAG = "simonGame";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> listDevicesID = new ArrayList<>();
    private int sequenceLength = 10;
    private String color;
    private List<String> sequence = new ArrayList<>();
    public boolean isPlaying = false;
    public int nTouch = 1;

    public SimonGame(String deviceID, String sessionID) {
        this.deviceID = deviceID;
        this.sessionID = sessionID;
        this.color = getRandomColor();

        registerOnGame();
        addGameRTU();
    }

    public void startGame() {
        triggerBlink();
    }

    private void triggerBlink() {
        if(nTouch <= sequence.size()) {
            String deviceToStart = sequence.get(nTouch - 1);
            setBlinkDevice(deviceToStart);
            increaseNLight();
        }
    }


    private void getSessionDevices() {

        // read database with doc ID, set screen and division spinners
        CollectionReference devices = db.collection("sessions/" + sessionID + "/devices");

        devices.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String deviceID = document.getId();
                        listDevicesID.add(deviceID);
                    }
                    createRandomList();

                    if (listener != null) {
                        //listener.onSessionLoaded();
                        listener.thereWeGo();
                    }
                }
            }
        });
    }

    private void createRandomList() {
        sequence.clear();
        int maxN = listDevicesID.size();
        for (int i = 0; i < sequenceLength; i++) {
            int randIndex=new Random().nextInt(maxN);
            sequence.add(listDevicesID.get(randIndex));
        }
        String sequenceString = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            sequenceString = String.join(",", sequence);
        }
        setSequenceOnFS(sequenceString);
    }

    private void setSequenceOnFS(String sequenceString) {
        Map<String, Object> data = new HashMap<>();
        data.put("sequence", sequenceString);
        data.put("nLight", "1");

        db.collection("sessions/" + sessionID + "/simonGame").document("gameInfo")
                .set(data, SetOptions.merge());
    }

    private void setBlinkDevice(String deviceID){
        Map<String, Object> data = new HashMap<>();
        data.put("blink", true);

        db.collection("sessions/" + sessionID + "/simonGame").document(deviceID)
                .set(data, SetOptions.merge());
    }

    public void touched() {

    }

    public interface gameListener {
        void onDrawRequest(boolean active);

        void thereWeGo();
    }

    private void registerOnGame() {
        // Update one field, creating the document if it does not already exist.
        Map<String, Object> data = new HashMap<>();
        data.put("on", true);
        data.put("blink",false);

        db.collection("sessions/" + sessionID + "/simonGame").document(deviceID)
                .set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getSessionDevices();
            }
        });
    }

    // Assign the listener implementing events interface that will receive the events
    public void setListener(gameListener listener) {
        this.listener = listener;
    }

    private void drawMe() {
        if (listener != null) {
            //listener.onSessionLoaded();
            listener.onDrawRequest(true);
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
        final DocumentReference docRef = db.collection("sessions/" + sessionID + "/simonGame").document(deviceID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    if (snapshot.getBoolean("blink")) {
                        TimerTask taskOn = new TimerTask() {
                            public void run() {
                                if (listener != null) {
                                    //listener.onSessionLoaded();
                                    listener.onDrawRequest(true);
                                }
                            }
                        };
                        TimerTask taskOnAgain = new TimerTask() {
                            public void run() {
                                if (listener != null) {
                                    listener.onDrawRequest(true);
                                }
                            }
                        };
                        TimerTask taskOff = new TimerTask() {
                            public void run() {
                                if (listener != null) {
                                    listener.onDrawRequest(false);
                                }
                            }
                        };
                        TimerTask sendToNext = new TimerTask() {
                            public void run() {
                                if (listener != null) {
                                    getNextToBlink();
                            }
                            }
                        };
                        Timer timer = new Timer("Timer");

                        long delay = 1000L;
                        timer.schedule(taskOn, delay);
                        timer.schedule(taskOff, delay*2);
                        timer.schedule(taskOnAgain, delay*3); //TODO: improve this
                        timer.schedule(sendToNext, delay*4); //TODO: improve this
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void getNextToBlink(){
        DocumentReference docRef = db.collection("sessions/" + sessionID + "/simonGame").document("gameInfo");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    nTouch = Integer.parseInt(document.getString("nLight"))+1;
                    triggerBlink();
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void increaseNLight(){
        Map<String, Object> data = new HashMap<>();
        data.put("nLight", Integer.toString(nTouch+1));

        db.collection("sessions/" + sessionID + "/simonGame").document("gameInfo")
                .set(data, SetOptions.merge());
    }

    public String getColor() {
        return color;
    }

    public void triggerDraw() {
        drawMe();
    }
}

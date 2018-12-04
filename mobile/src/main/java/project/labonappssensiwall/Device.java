package project.labonappssensiwall;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class Device {

    private static final String DEVICE_PREFS = "DevicePrefsFile";
    private Context applicationContext;
    private SharedPreferences prefs;
    private String deviceID;
    private String currentSession;
    private String name = "";
    private String type = "";
    private Boolean isOnFireStore = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "PietroActivity";


    public Device(Context applicationContext){
        this.applicationContext = applicationContext;
        prefs = applicationContext.getSharedPreferences(DEVICE_PREFS, MODE_PRIVATE);
        updateDeviceInfo();
    }

    public void initDeviceFS() {
        DocumentReference docRef = db.collection("devices").document(deviceID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        isOnFireStore = true;
                        name = document.getString("name");
                        type = document.getString("type");
                        setPrefs();
                    } else {
                        addDeviceFS();
                    }
                } else {
                }
            }
        });
    }

    private void addDeviceFS(){
        Map<String, Object> data = new HashMap<>();
        name = tmpGetRandomName();
        type = "smart phone";
        data.put("name", name); //TODO: aggiungiamo un nome?
        data.put("type",type); //TODO: aggiungere gli altri tipi

        db.collection("devices").document(deviceID).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isOnFireStore = true;
                updateDeviceInfo(); //TODO: improve this...
            }
        });
    }

    public void updateDeviceInfo() {
        deviceID = prefs.getString("deviceID", null);
        if (deviceID == null) {
            // get device android ID
            deviceID = Secure.getString(applicationContext.getContentResolver(),Secure.ANDROID_ID);
        }
    }

    private void setPrefs(){
        SharedPreferences.Editor editor = applicationContext.getSharedPreferences(DEVICE_PREFS, MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("type", type);
        editor.putString("deviceID", deviceID);
        editor.apply();
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void registerDeviceOnSession(final String sessionID){
        DocumentReference docRef = db.collection("sessions/"+sessionID+"/devices").document(deviceID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        currentSession = sessionID;
                    } else {
                        //insert device
                        insertNewDeviceOnSession(sessionID);
                    }
                } else {
                    //TODO: toast to notify connection error
                }
            }
        });
    }

    private void insertNewDeviceOnSession(String sessionID){
        Map<String, Object> data = new HashMap<>();

        db.collection("sessions/" + sessionID + "/devices").document(this.deviceID).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e); //TODO: toast to notify error
                    }
                });
    }

    private String tmpGetRandomName(){
        List<String> names = Arrays.asList("Maragret Steves","Margurite Cokley","Markus Voigt","Mack Breslin","Daniella Trabert","Silvana Hinchman","Myron Orsborn","Royal Stufflebeam","Elwanda Sabine","Alysa Reneau","Genevive Reno","Bettie Olivarria","Carlo Sisneros","Estell Hanselman","Sook Carnegie","Arnita Galvin","Camellia Balderrama","Vertie Necaise","Elda Foard","Margret Gean","Shaniqua Flinchbaugh","Viola Turcotte","Galina Pruneda","Berneice Woldt","Adriene Whitman","Eryn Silsby","Kittie Ovalle","Yukiko Ledoux","Ike Farnham","Lamar Forshey","Tamar Heishman","Brittany Shehane","Janeen Deem","Delbert Rumore","Nakisha Sok","Dixie Redmond","Raleigh Cumbo","Caitlyn Spicer","Meghan Vanzandt","Lahoma Satterfield","Suzi Pfaff","Dorthy Stefanski","Marshall Treaster","Tamra Valez","Thalia Sidney","Dominica Newborn","Cedrick Kautz","Rachel Boughton","Malka Hayner","Sana Piccard");
        Random rand = new Random();
        return names.get(rand.nextInt(names.size()));
    }
}

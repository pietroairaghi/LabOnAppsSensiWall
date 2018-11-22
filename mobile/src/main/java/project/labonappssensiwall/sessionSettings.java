package project.labonappssensiwall;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class sessionSettings {

    private String sessionID;
    private static final String TAG = "PietroActivity";
    private Map<String, HashMap<String,Object>> allSettings = new HashMap<>();
    private Map<String, List<HashMap<String,Object>>> settingsByCategory = new HashMap<>();
    private HashMap<String,String> allSettingsTypes = new HashMap<>();
    private HashMap<String,String> sessionSettings = new HashMap<>();
    private FirebaseFirestore db;

    public sessionSettings(String sessionID){
        this.sessionID = sessionID;

        Log.d(TAG,"start retriving session");
        db = FirebaseFirestore.getInstance();

        loadSessionSettings();
        Log.d(TAG,"loaded settings sessions");
        loadDefaultSettings();
        Log.d(TAG,"loaded settings default");
    }

    protected void loadSessionSettings(){
        CollectionReference sessionsSettings =  db.collection("sessions/"+sessionID+"/settings");

        sessionsSettings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String settingID  = document.getId();
                        String value = document.getString("value");
                        sessionSettings.put(settingID,value);
                    }

                } else {
                    Log.d(TAG,"error retrieving session settings");
                }
            }
        });
    }

    protected void loadDefaultSettings(){
        CollectionReference settings =  db.collection("settings");

        final CountDownLatch done = new CountDownLatch(1);
        settings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String categoryID = document.getString("category");
                        String settingID  = document.getId();
                        String settingType  = document.getString("type");
                        List<HashMap<String,Object>> currentCatData = settingsByCategory.get(categoryID);
                        if (currentCatData==null) {
                            currentCatData = new ArrayList<>();
                        }
                        HashMap<String,Object> currentData = (HashMap<String,Object>)document.getData();
                        allSettings.put(settingID,currentData);
                        currentData.put("id",settingID);
                        currentCatData.add(currentData);
                        settingsByCategory.put(categoryID,currentCatData);
                        allSettingsTypes.put(settingID,settingType);
                    }
                    done.countDown();
                } else {
                    Log.d(TAG,"error retrieving default settings");
                }
            }
        });

        try {
            done.await(); //it will wait till the response is received from firebase.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Object getSetting(String key){
        Object value = sessionSettings.get(key);

        if(value != null){
            return value;
        }

        value = allSettings.get(key).get("default value");

        return value;
    }

    public int getInt(String key){
        Object value = getSetting(key);

        if(value == null){
            return 0;
        }

        int intValue = Integer.valueOf(value.toString());

        return intValue;
    }

    public String getString(String key){
        Object value = getSetting(key);

        if(value == null){
            return "";
        }

        return value.toString();
    }

    public Map<String, HashMap<String, Object>> getAllSettings() {
        return allSettings;
    }

    public HashMap<String, String> getAllSettingsTypes() {
        return allSettingsTypes;
    }

    public Map<String, List<HashMap<String, Object>>> getSettingsByCategory() {
        return settingsByCategory;
    }
}

package project.labonappssensiwall;

import android.support.annotation.NonNull;
import android.util.Log;

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

public class SessionSettings {

    private String sessionID;
    private static final String TAG = "PietroActivity";
    private Map<String, HashMap<String,Object>> allSettings = new HashMap<>();
    private Map<String, List<HashMap<String,Object>>> settingsByCategory = new HashMap<>();
    private HashMap<String,String> allSettingsTypes = new HashMap<>();
    private HashMap<String,String> sessionSettings = new HashMap<>();
    private FirebaseFirestore db;
    private boolean sessionSettingsLoaded = false; private boolean defaultSettingsLoaded = false;
    private sessionSettingsListener listener;

    public SessionSettings(String sessionID){
        this.sessionID = sessionID;
        this.listener = null;

        db = FirebaseFirestore.getInstance();

        loadSessionSettings();
        loadDefaultSettings();
    }

    public interface sessionSettingsListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        //void onSessionLoaded();
        //void onDefaultSessionLoaded();
        // or when data has been loaded
        void onCompleteLoading();
    }

    // Assign the listener implementing events interface that will receive the events
    public void setListener(sessionSettingsListener listener) {
        this.listener = listener;
    }

    protected void checkForOnComplete(){
        if(sessionSettingsLoaded && defaultSettingsLoaded){
            listener.onCompleteLoading();
        }
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
                    sessionSettingsLoaded = true;
                    if (listener != null) {
                        //listener.onSessionLoaded();
                        checkForOnComplete();
                    }
                } else {
                    Log.d(TAG,"error retrieving session settings");
                }
            }
        });
    }

    protected void loadDefaultSettings(){
        CollectionReference settings =  db.collection("settings");

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
                    defaultSettingsLoaded = true;
                    if (listener != null) {
                        //listener.onDefaultSessionLoaded();
                        checkForOnComplete();
                    }
                } else {
                    Log.d(TAG,"error retrieving default settings");
                }
            }
        });
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

    public HashMap<String, String> getSessionSettings() {
        return sessionSettings;
    }
}

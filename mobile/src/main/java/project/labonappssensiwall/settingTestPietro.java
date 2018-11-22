package project.labonappssensiwall;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class settingTestPietro extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "PietroActivity";
    private SessionSettings sessionSettings;
    private static ContextThemeWrapper contextThemeWrapper = null;
    private String sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get intent with session id
        //Intent intent = getIntent();
        //Bundle extras = intent.getExtras();
        //String sessionID = extras.getString(SessionActivity.SESSION_ID);

        sessionID = "q6DOpI3PtoiLOAYkA1kZ";

        sessionSettings = new SessionSettings(sessionID);

        sessionSettings.setListener(new SessionSettings.sessionSettingsListener() {
            @Override
            public void onCompleteLoading() {
                removeAllLocalPreferences();
                populateSettings();
                addPreferenceListener();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        removePreferenceListener();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        addPreferenceListener();
    }

    protected void populateSettings() {

        //rimuovere le shared preferences
        //PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().apply();
        Context activityContext = this;

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(activityContext);
        setPreferenceScreen(preferenceScreen);


        TypedValue themeTypedValue = new TypedValue();
        contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);


        for (Map.Entry<String, List<HashMap<String, Object>>> categoryMap : sessionSettings.getSettingsByCategory().entrySet()) {

            String categoryName = categoryMap.getKey();
            List<HashMap<String, Object>> datas = categoryMap.getValue();

            PreferenceCategory preferenceCategory = newPreferenceCategory(categoryName, contextThemeWrapper);

            // First we add the category to the root PreferenceScreen
            getPreferenceScreen().addPreference(preferenceCategory);

            // Then their child to it
            for (HashMap<String, Object> data : datas) {
                addPreference(data, preferenceCategory);
            }
        }


    }

    protected void removeAllLocalPreferences() {
        for (Map.Entry<String, String> entry : sessionSettings.getAllSettingsTypes().entrySet()) {
            String settingID = entry.getKey();

            SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.remove(settingID);
            editor.apply();
        }
    }

    protected void removePreferenceListener(){
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    protected void addPreferenceListener() {
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }


    protected PreferenceCategory newPreferenceCategory(String categoryName, ContextThemeWrapper contextThemeWrapper) {
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        preferenceCategory.setTitle(categoryName);

        return preferenceCategory;
    }

    protected void addPreference(HashMap<String, Object> data, PreferenceCategory preferenceCategory) {
        String preferenceType = data.get("type").toString();

        switch (preferenceType) {
            case "checkbox":
                addCheckboxPreference(data, preferenceCategory);
                break;
            case "text":
                addTextPreference(data, preferenceCategory);
                break;
        }


    }

    protected Object getDefaultOrSessionValue(String key, HashMap<String, Object> data) {
        Object sessionVal = sessionSettings.getSessionSettings().get(key);

        if (sessionVal != null) {
            return sessionVal;
        }

        return data.get("default value");
    }

    protected void addCheckboxPreference(HashMap<String, Object> data, PreferenceCategory pc) {
        String preferenceName = data.get("name").toString();
        String preferenceKey = data.get("id").toString();

        Object value = getDefaultOrSessionValue(preferenceKey, data);
        int preferenceValue = 0;

        if (value != null) {
            preferenceValue = Integer.parseInt(value.toString());
        }

        CheckBoxPreference preference = new CheckBoxPreference(contextThemeWrapper);
        preference.setTitle(preferenceName);
        preference.setKey(preferenceKey);

        if (preferenceValue == 1) {
            preference.setChecked(true);
        } else {
            preference.setChecked(false);
        }

        pc.addPreference(preference);
    }

    protected void addTextPreference(HashMap<String, Object> data, PreferenceCategory pc) {
        String preferenceName = data.get("name").toString();
        String preferenceKey = data.get("id").toString();

        EditTextPreference preference = new EditTextPreference(contextThemeWrapper);
        preference.setTitle(preferenceName);
        preference.setKey(preferenceKey);

        Object value = getDefaultOrSessionValue(preferenceKey, data);
        String preferenceValue = "";

        if (value != null) {
            preferenceValue = value.toString();
        }
        preference.setText(preferenceValue);

        pc.addPreference(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        String settingType = sessionSettings.getAllSettingsTypes().get(key);
        Preference pref = getPreferenceManager().findPreference(key);

        String currentValue = "";

        switch (settingType) {
            case "checkbox":
                boolean isChecked = ((CheckBoxPreference) pref).isChecked();
                currentValue = isChecked ? "1" : "0";
                break;
            case "text":
                currentValue = ((EditTextPreference) pref).getText();
                break;
        }

        saveSettingOnFirebase(sessionID, key, currentValue);

    }

    private void saveSettingOnFirebase(String sessionID, String key, String value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> settingMap = new HashMap<>();
        settingMap.put("value", value);

        db.collection("sessions/" + sessionID + "/settings").document(key)
                .set(settingMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Settings updated", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
    }

}

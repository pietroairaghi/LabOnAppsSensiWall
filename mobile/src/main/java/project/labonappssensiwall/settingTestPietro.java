package project.labonappssensiwall;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
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

public class settingTestPietro extends PreferenceActivity {

    private static final String TAG = "PietroActivity";
    private static Map<String, List<Object>> settingsByCategory = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadAndPopulateFireBaseSettings();

    }


    protected void loadAndPopulateFireBaseSettings(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference sessions =  db.collection("settings");

        sessions.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String categoryID = document.get("category").toString();
                        List<Object> currentCatData = settingsByCategory.get(categoryID);
                        if (currentCatData==null) {
                            currentCatData = new ArrayList<>();
                        }
                        currentCatData.add(document.getData());
                        settingsByCategory.put(categoryID,currentCatData);
                    }
                    populateSettings();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    protected void viewCosi(){
        for (Map.Entry<String, List<Object>> entry : settingsByCategory.entrySet()) {

            String categoryName = entry.getKey();

            Log.d(TAG,"trovata categoria" + categoryName);


            List<Object> datas = entry.getValue();

            for (Object data : datas) {

                Log.d(TAG,"---------------");

                HashMap<String,Object> mapData = (HashMap<String, Object>)data;

                for (Map.Entry me : mapData.entrySet()) {
                    Log.d(TAG,"Key: "+me.getKey() + " & Value: " + me.getValue().toString());
                }

            }
        }
    }

    protected void populateSettings(){

        //rimuovere le shared preferences
        //PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().clear().apply();

        Context activityContext = this;

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(activityContext);
        setPreferenceScreen(preferenceScreen);

        TypedValue themeTypedValue = new TypedValue();
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);


        for (Map.Entry<String, List<Object>> categoryMap : settingsByCategory.entrySet()) {

            String categoryName = categoryMap.getKey();
            List<Object> datas = categoryMap.getValue();

            PreferenceCategory preferenceCategory = newPreferenceCategory(categoryName, contextThemeWrapper);

            // First we add the category to the root PreferenceScreen
            getPreferenceScreen().addPreference(preferenceCategory);

            // Then their child to it
            for (Object data : datas) {
                addPreference(data,preferenceCategory,contextThemeWrapper);
            }

        }
    }


    protected PreferenceCategory newPreferenceCategory(String categoryName, ContextThemeWrapper contextThemeWrapper){
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        preferenceCategory.setTitle(categoryName);

        return preferenceCategory;
    }

    protected void addPreference(Object data,PreferenceCategory preferenceCategory,ContextThemeWrapper contextThemeWrapper){
        HashMap<String,Object> mapData = (HashMap<String, Object>)data;
        String preferenceName = mapData.get("name").toString();

//        if (getPreferenceScreen().findPreference(preferenceName) != null){
//            return;
//        }else{
//            Log.d(TAG,getPreferenceScreen().findPreference(preferenceName).toString());
//        }

        CheckBoxPreference checkBoxPreference = new CheckBoxPreference(contextThemeWrapper);
        checkBoxPreference.setTitle(preferenceName);
        checkBoxPreference.setKey("checkbox");
        checkBoxPreference.setChecked(true);

        preferenceCategory.addPreference(checkBoxPreference);

    }


    protected void testPop(){
        Context activityContext = this;

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(activityContext);
        setPreferenceScreen(preferenceScreen);

        TypedValue themeTypedValue = new TypedValue();
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);

        // We instance each Preference using our ContextThemeWrapper object
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        preferenceCategory.setTitle("Category test");

        EditTextPreference editTextPreference = new EditTextPreference(contextThemeWrapper);
        editTextPreference.setKey("edittext");
        editTextPreference.setTitle("EditText test");

        CheckBoxPreference checkBoxPreference = new CheckBoxPreference(contextThemeWrapper);
        checkBoxPreference.setTitle("Checkbox test");
        checkBoxPreference.setKey("checkbox");
        checkBoxPreference.setChecked(true);

        // It's REALLY IMPORTANT to add Preferences with child Preferences to the Preference Hierarchy first
        // Otherwise, the PreferenceManager will fail to load their keys

        // First we add the category to the root PreferenceScreen
        getPreferenceScreen().addPreference(preferenceCategory);

        // Then their child to it
        preferenceCategory.addPreference(editTextPreference);
        preferenceCategory.addPreference(checkBoxPreference);
    }

}

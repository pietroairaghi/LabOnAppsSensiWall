package project.labonappssensiwall;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

public class settingTestPietro extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

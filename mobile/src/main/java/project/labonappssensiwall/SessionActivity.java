package project.labonappssensiwall;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String sessionName = extras.getString(MainActivity.SESSION_NAME);

        TextView session = findViewById(R.id.sessionName);
        session.setText("Welcome to " + sessionName + "!");
    }

    public void clickedbuttonSetting(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
       // intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void clickedbuttonTest(View view) {
        Intent intent = new Intent(this, TestActivity.class);
        // intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void clickedbuttonStart(View view) {
    }

    public void clickedbuttonDraw(View view) {
    }
}

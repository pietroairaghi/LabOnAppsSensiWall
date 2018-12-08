package project.labonappssensiwall;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WearTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_test);
    }

    public void inviaDatoEx(View view) {

        Intent intentWear = new Intent(this,WearService.class);
        intentWear.setAction(WearService.ACTION_SEND.STARTACTIVITY.name());
        intentWear.putExtra(WearService.ACTIVITY_TO_START,BuildConfig.W_drawactivity);
        intentWear.putExtra(WearService.PATH,"prova_path");
        startService(intentWear);
    }


    public void inviaDatoEx2(View view) {

        Intent intentWear = new Intent(this,WearService.class);
        intentWear.setAction(WearService.ACTION_SEND.STARTACTIVITY.name());
        intentWear.putExtra(WearService.ACTIVITY_TO_START,BuildConfig.W_sensoractivity);
        intentWear.putExtra(WearService.PATH,"prova_path 2");
        startService(intentWear);

    }
}

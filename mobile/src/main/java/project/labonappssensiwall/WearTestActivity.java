package project.labonappssensiwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class WearTestActivity extends AppCompatActivity {

    public static final String MESSAGE = "MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_test);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView textView = findViewById(R.id.textViewRes);

                float gX = intent.getFloatExtra("gX",0f);
                float gY = intent.getFloatExtra("gY",0f);
                float gZ = intent.getFloatExtra("gZ",0f);
                String shape = intent.getStringExtra("shape");

                float signY = (gX / Math.abs(gX));
                float signX = (gY / Math.abs(gY));

                double coordY = (Math.acos((9.81f - gZ)/9.81f * signY) - Math.PI/2)/(Math.PI/2);
                double coordX = (Math.asin(gY/9.81f)/(Math.PI/2))*(-1);

                if(Double.isNaN(coordY)){
                    coordY = -1*signY;
                }

                if(Double.isNaN(coordX)){
                    coordX = -1*signX;
                }


                textView.setText("coords: gX " + Float.toString(gX)
                        + " gY " + Float.toString(gY)
                        + " gZ " + Float.toString(gZ)
                        + " shape: " + shape
                        + " coordY: " + Double.toString(coordY)
                        + " coordX: " + Double.toString(coordX));

            }
        }, new IntentFilter("STARTDRAW"));


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

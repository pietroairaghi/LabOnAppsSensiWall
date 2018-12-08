package project.labonappssensiwall;


import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

public class DrawActivity extends WearableActivity {

    public static final String MESSAGE = "MESSAGE";
    private ConstraintLayout mLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        mLayout = findViewById(R.id.container);
        // Enables Always-on
        setAmbientEnabled();


    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateDisplay();
    }

    private void updateDisplay() {
        if(isAmbient()){
            mLayout.setBackgroundColor(getResources().getColor(android.R.color.black,getTheme()));
        }else{
            mLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark,getTheme()));
        }
    }

    public void sendDraw(View view) {
        Intent intentWear = new Intent(this,WearService.class);
        intentWear.setAction(WearService.ACTION_SEND.MESSAGE.name());
        intentWear.putExtra(WearService.MESSAGE,"ciao bello");
        intentWear.putExtra(WearService.PATH,"prova_path");
        startService(intentWear);
    }
}

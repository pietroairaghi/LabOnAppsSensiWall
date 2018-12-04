package project.labonappssensiwall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WallActivity extends AppCompatActivity {

    private OpenGLView openGLView;
    private DrawingHandler drawingHandler;

    private String sessionID;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);

        // get intent with session id
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        sessionID = extras.getString(SessionActivity.SESSION_ID);

        device = new Device(this);
        device.registerDeviceOnSession(sessionID);

        openGLView = findViewById(R.id.openGLViewID);

        uiReset();

        drawingHandler = new DrawingHandler(sessionID,device.getDeviceID());
        drawingHandler.setListener(new DrawingHandler.drawingHandlerListener() {
            @Override
            public void onUpdate() {
                dacciDentro();
            }
        });


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            uiReset();
        }
    }

    private void uiReset(){
            hideSystemUI();

            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            openGLView.getLayoutParams().height = height + 150;
            openGLView.getLayoutParams().width = width;
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openGLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        openGLView.onPause();
    }

    public void dacciDentro() {
        HashMap<String,Drawing> drawingList = drawingHandler.getDrawingsList();
        List<HashMap<String,Object>> shapes = new ArrayList<>();
        for (Drawing drawing : drawingList.values()) {
            HashMap<String,Object> currentDrawing = new HashMap<>();
            currentDrawing.put("coords",drawing.getOpenGLCoords());
            currentDrawing.put("order",drawing.getOpenGLOrder());
            currentDrawing.put("color",drawing.getOpenGLColor());
            shapes.add(currentDrawing);
        }

        openGLView.changeCoso(shapes);
    }


}

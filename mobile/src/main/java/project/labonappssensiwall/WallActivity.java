package project.labonappssensiwall;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
        HashMap<Long,String> drawingOrders = drawingHandler.getDrawingOrders();
        //Set<Long> keys = drawingOrders.keySet();

        List<Long> keyList = new ArrayList(drawingOrders.keySet());
       // List<String> valueList = new ArrayList(drawingOrders.values());

     //   long max = keyList.get(keyList.size()-1);
        int totalSize = keyList.size();

        float i = -0.9f;

        List<HashMap<String,Object>> shapes = new ArrayList<>();
        for (String drawingID : drawingOrders.values()){
           // Log.d("handlerTAG",drawingID);
            Drawing drawing = drawingHandler.getDrawing(drawingID);
            drawing.normalizeZindex(i);
            float zindexnorm = drawing.getZ_index();


            //Log.d("DisplayActivityAAA", "index max: " + max);
           // Log.d("DisplayActivityAAA", "index dopo norm: " + zindexnorm);

            HashMap<String,Object> currentDrawing = new HashMap<>();
            currentDrawing.put("coords",drawing.getOpenGLCoords());
            currentDrawing.put("order",drawing.getOpenGLOrder());
            currentDrawing.put("color",drawing.getOpenGLColor());
            shapes.add(currentDrawing);

            i += (float)1/totalSize;
        }

        openGLView.changeCoso(shapes);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {


        if(e.getAction() == e.ACTION_DOWN) {

            float x = e.getX();
            float y = e.getY();

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float screenWidth = metrics.widthPixels;
            float screenHeight= metrics.heightPixels;

            float sceneX = (x / screenWidth) * 2.0f - 1.0f;
            float sceneY = (y / screenHeight) * -2.0f + 1.0f; //if bottom is at -1. Otherwise same as X

            // remove shape
            drawingHandler.deleteDrawingOnTouch(sceneX, sceneY);

            //Log.d("ontouch", sceneX + " " + sceneY);
        }

        return true;
    }

}

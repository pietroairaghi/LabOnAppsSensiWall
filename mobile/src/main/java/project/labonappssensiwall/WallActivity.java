package project.labonappssensiwall;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WallActivity extends AppCompatActivity {

    private static final String TAG = "WallActivity";

    private OpenGLView openGLView;
    private DrawingHandler drawingHandler;
    private SimonGame simonGame;
    private boolean simonLauncher = false;
    private FirebaseFirestore db;

    private String sessionID;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);

        db = FirebaseFirestore.getInstance();

        // get intent with session id
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        sessionID = extras.getString(SessionActivity.SESSION_ID);

        device = new Device(this);
        device.registerDeviceOnSession(sessionID);

        openGLView = findViewById(R.id.openGLViewID);
        drawingHandler = new DrawingHandler(sessionID, device.getDeviceID());
        drawingHandler.setListener(new DrawingHandler.drawingHandlerListener() {
            @Override
            public void onUpdate() {
                triggerOpenGLView();
            }
        });


        if (intent.hasExtra("simonGame")) { //TODO: chose better when trigger simon game
            simonLauncher = true;
        }

        simonGame = new SimonGame(device.getDeviceID(),sessionID,simonLauncher);
        simonGame.isPlaying = true;

        simonGame.setListener(new SimonGame.gameListener() {
            @Override
            public void onDrawRequest(boolean active) {
                String color = simonGame.getColor();
                if(!active){
                    color = "#FFFFFF";
                }
                Drawing draw = new Drawing("bgr", 0.5f, 0.5f, 1, color);
                drawingHandler.addDrawingToList(draw,1,"bgr");
                triggerOpenGLView();
            }

            @Override
            public void thereWeGo() {
                if(simonLauncher) {
                    simonGame.startGame();
                }else{
                    simonGame.waitForStart();
                }
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

    public void triggerOpenGLView() {
        HashMap<Long, String> drawingOrders = drawingHandler.getDrawingOrders();
        //Set<Long> keys = drawingOrders.keySet();

        List<Long> keyList = new ArrayList(drawingOrders.keySet());
        // List<String> valueList = new ArrayList(drawingOrders.values());

        //   long max = keyList.get(keyList.size()-1);
        int totalSize = keyList.size();

        float i = -0.9f;

        List<HashMap<String, Object>> shapes = new ArrayList<>();
        for (String drawingID : drawingOrders.values()) {
            // Log.d("handlerTAG",drawingID);
            Drawing drawing = drawingHandler.getDrawing(drawingID);
            drawing.normalizeZindex(i);
            float zindexnorm = drawing.getZ_index();


            //Log.d("DisplayActivityAAA", "index max: " + max);
            // Log.d("DisplayActivityAAA", "index dopo norm: " + zindexnorm);

            HashMap<String, Object> currentDrawing = new HashMap<>();
            currentDrawing.put("coords", drawing.getOpenGLCoords());
            currentDrawing.put("order", drawing.getOpenGLOrder());
            currentDrawing.put("color", drawing.getOpenGLColor());
            shapes.add(currentDrawing);

            i += (float) 1 / totalSize;
        }

        openGLView.changeCoso(shapes);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (e.getAction() == e.ACTION_DOWN) {

            if(simonGame.isPlaying){
                simonGame.touched();
            }else {
                float x = e.getX();
                float y = e.getY();

                DisplayMetrics metrics = getResources().getDisplayMetrics();
                float screenWidth = metrics.widthPixels;
                float screenHeight = metrics.heightPixels;

                float sceneX = (x / screenWidth) * 2.0f - 1.0f;
                float sceneY = (y / screenHeight) * -2.0f + 1.0f; //if bottom is at -1. Otherwise same as X

                // remove shape
                drawingHandler.deleteDrawingOnTouch(sceneX, sceneY);

                //Log.d("ontouch", sceneX + " " + sceneY);
            }
        }

        return true;
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                WallActivity.this);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // remove device from db
                db.collection("sessions/"+sessionID+"/devices").document(device.getDeviceID())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });

                // finish app
                finish();
            }
        });

        alertDialog.setNegativeButton("No", null);
        alertDialog.setMessage("Do you want to exit?");
        alertDialog.setTitle("Display session");
        alertDialog.show();
    }


}
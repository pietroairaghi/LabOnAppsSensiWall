package project.labonappssensiwall;

import android.se.omapi.Session;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DrawingHandler {

    private drawingHandlerListener listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String sessionID;
    private String deviceID;
    private SessionSettings settings;

    private HashMap<String,Drawing> drawingsList= new HashMap<>();
    private HashMap<Long,String> drawingOrders = new HashMap();

    private static final String TAG = "handlerTAG";

    public DrawingHandler(String sessionID, String deviceID) {
        this.listener = null;
        this.sessionID = sessionID;
        this.deviceID = deviceID;
        settings = new SessionSettings(sessionID);

        // Get divisions from DB and populate spinner
        settings.setListener(new SessionSettings.sessionSettingsListener() {
            @Override
            public void onCompleteLoading() {
                drawBackgoundDivisions();
            }
        });

        // add drawings real time update
        addDrawingsRTU();
    }

    public interface drawingHandlerListener {
        void onUpdate();
    }

    // Assign the listener implementing events interface that will receive the events
    public void setListener(drawingHandlerListener listener) {
        this.listener = listener;
    }


    private void addDrawingsRTU() {

        CollectionReference collection = db.collection("sessions/"+sessionID+"/devices/"+deviceID+"/drawings");
                collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                // Update shapes
                for (DocumentChange dc : snapshots.getDocumentChanges()) {

                            String ID = dc.getDocument().getId();
                            String shape = dc.getDocument().get("shape").toString();
                            float positionX = Float.parseFloat(dc.getDocument().get("positionx").toString());
                            float positionY = Float.parseFloat(dc.getDocument().get("positiony").toString());
                            float scale = Float.parseFloat(dc.getDocument().get("scale").toString());
                            int division = Integer.parseInt(dc.getDocument().get("division").toString());
                            long order = Long.parseLong(dc.getDocument().get("timestamp").toString());
                            String color = dc.getDocument().get("color").toString();

                    Drawing tmp = new Drawing(ID, shape, positionX, positionY, scale, division, color);

                            switch (dc.getType()) {
                                case ADDED:
                                    // add the drawing to the list
                                    drawingsList.put(ID,tmp);
                                    drawingOrders.put(order,ID);

                                    Log.d(TAG, "added: " + tmp.getID());
                                    break;
                                case MODIFIED:
                                    // modify the drawing to the list
                                    drawingsList.put(ID,tmp);
                                    drawingOrders.put(order,ID);

                                    Log.d(TAG, "Modified: " + tmp.getID());
                                    break;
                                case REMOVED:
                                    // remove the drawing from the list
                                    drawingsList.remove(ID);
                                    drawingOrders.remove(order);

                            Log.d(TAG, "Removed: " + tmp.getID());
                            break;
                    }
                }

                //TODO: magari spostare fuori dal for
                if (listener != null) {
                    //listener.onSessionLoaded();
                    listener.onUpdate();
                }
            }
        });
    }

    public Drawing getDrawing(String key) {
        return drawingsList.get(key);
    }

    public HashMap<String, Drawing> getDrawingsList() {
        return drawingsList;
    }


    public HashMap<Long, String> getDrawingOrders() {
        return drawingOrders;
    }


    public void deleteDrawingOnTouch(float x, float y) {

        String ID;
        Drawing draw;

        for (Map.Entry<String, Drawing> entry : drawingsList.entrySet()) {

            ID = entry.getKey();
            draw = entry.getValue();

            if (Math.pow(x - draw.getPositionX0(), 2) + Math.pow(y - draw.getPositionY0(), 2) <= Math.pow(1.5 * draw.getScale(), 2)) {

                db.collection("sessions/" + sessionID + "/devices/" + deviceID + "/drawings").document(ID)
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

            }

        }
    }

    public void drawBackgoundDivisions() {

        // take division x, division y, divide de screen


        int divisions = settings.getInt("divisions");

        String color = "#FF0000";

        float w = (float) 1 / divisions;
        float scale =  w/(float)Math.sqrt(2);

        // columns
        for (int i = 0; i < divisions; i++) {

            // rows
            for (int j = 0; j < divisions; j++) {

                float centerX = w / 2 + j * w;
                float centerY = w / 2 + i * w;

                Drawing division = new Drawing(centerX, centerY, scale, color);

                String ID = Integer.toString(j) + Integer.toString(i);
                drawingsList.put(ID, division);

                if (color.equals("#FF0000")) {
                    color = "#696969";
                } else if (color.equals("#696969")) {
                    color = "#FF0000";
                }


                Log.d(TAG, "centerX: " + centerX);
                Log.d(TAG, "centerY: " + centerY);

            }

            if(divisions % 2 == 0)
                if (color.equals("#FF0000")) {
                    color = "#696969";
                } else if (color.equals("#696969")) {
                    color = "#FF0000";
                }

        }
        //TODO: magari spostare fuori dal for
        if (listener != null) {
            //listener.onSessionLoaded();
            listener.onUpdate();
        }


    }

}

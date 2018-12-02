package project.labonappssensiwall;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class DrawingHandler {

    private drawingHandlerListener listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String sessionID;
    private String deviceID;

    private HashMap<String,Drawing> drawingsList= new HashMap<>();

    private static final String TAG = "handlerTAG";

    public DrawingHandler(String sessionID, String deviceID){
        this.listener = null;
        this.sessionID = sessionID;
        this.deviceID = deviceID;

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


    private void addDrawingsRTU(){

        db.collection("sessions/"+sessionID+"/devices/"+deviceID+"/drawings")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            String color = dc.getDocument().get("color").toString();

                            Drawing tmp = new Drawing(ID, shape, positionX, positionY, scale, division, color);

                            switch (dc.getType()) {
                                case ADDED:
                                    // add the drawing to the list
                                    drawingsList.put(ID,tmp);

                                    Log.d(TAG, "added: " + tmp.getID());
                                    break;
                                case MODIFIED:
                                    // modify the drawing to the list
                                    drawingsList.put(ID,tmp);

                                    Log.d(TAG, "Modified: " + tmp.getID());
                                    break;
                                case REMOVED:
                                    // remove the drawing from the list
                                    drawingsList.remove(ID);

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

    public Drawing getDrawing(String key){
        return drawingsList.get(key);
    }

    public HashMap<String,Drawing> getDrawingsList(){
        return drawingsList;
    }


}

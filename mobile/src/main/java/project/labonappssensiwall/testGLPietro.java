package project.labonappssensiwall;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class testGLPietro extends AppCompatActivity {

    private OpenGLView openGLView;
    private DrawingHandler drawingHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_glpietro);
        openGLView = findViewById(R.id.openGLViewID);

        drawingHandler = new DrawingHandler("q6DOpI3PtoiLOAYkA1kZ","jNjsPcCklbvh55hv9pdr");


        drawingHandler.setListener(new DrawingHandler.drawingHandlerListener() {
            @Override
            public void onUpdate(String drawingID) {
                dacciDentro();
            }
        });


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

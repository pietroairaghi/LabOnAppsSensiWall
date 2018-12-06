package project.labonappssensiwall;

import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;

public class Drawing {

    private static final String TAG = "DisplayActivityAAA";

    private String ID;
    private String shape;
    private float positionX;
    private float positionY;
    private float scale;
    private int division;
    private String color;

    private float[] openGLCoords;
    private short[] openGLOrder;          // Order to draw vertex
    private float[] openGLColor = new float[4];


    public Drawing(String ID, String shape, float positionX, float positionY, float scale, int division, String color){
        this.ID = ID;
        this.shape = shape;
        this.positionX = positionX;         // x0
        this.positionY = positionY;         // y0
        this. scale = scale;                // radius
        this.division = division;
        this.color = color;

        // set OpenGL parameters
        setOpenGlParameters();
    }

    public Drawing(float positionX, float positionY, float scale, String color){
        //this.ID = ID;
        this.shape = "square";
        this.positionX = positionX;         // x0
        this.positionY = positionY;         // y0
        this. scale = scale;                // radius
        //this.division = division;
        this.color = color;

        // set OpenGL parameters
        setOpenGlParameters();
    }

    public String getID(){
        return ID;
    }

    private void setOpenGlParameters() {

        float angle = 0;
        int nbrTriang = 0;

        if (shape.equals("square")) {
            nbrTriang = 4;
            angle = 45;
        } else if (shape.equals("triangle")) {
            nbrTriang = 3;
            angle = 90;
        } else if (shape.equals("pentagon")) {
            nbrTriang = 5;
            angle = 18;
        } else if (shape.equals("hexagon")) {
            nbrTriang = 6;
        } else if (shape.equals("octagon")) {
            nbrTriang = 8;
        } else if (shape.equals("circle")) {
            nbrTriang = 40;
        }

        this.openGLCoords = computePolygonCoords(positionX, positionY, scale, nbrTriang);
        this.openGLOrder = computePolygonOrder(nbrTriang);
        if(angle != 0)
            this.openGLCoords = computeRotation(positionX, positionY, angle, openGLCoords);
        this.openGLCoords = toOpenGlCoords(openGLCoords);


        // set color
        int col, r, g, b;
        col = Color.parseColor(this.color);
        r = Color.red(col);
        g = Color.green(col);
        b = Color.blue(col);

        openGLColor[0] = (float)r/255;
        openGLColor[1] = (float)g/255;
        openGLColor[2] = (float)b/255;
        openGLColor[3] = 0.0f;

        Log.d(TAG, "Color: " + Arrays.toString(openGLColor));

    }


    // coords shift for OpenGL
    public float[] toOpenGlCoords(float[] coords) {

        float[] coordsOpenGL = new float[coords.length];

        for (int i = 0; i < coords.length/3; i++) {
            coordsOpenGL[i * 3] = coords[i * 3] * 2 - 1;
            coordsOpenGL[i * 3 + 1] = coords[i * 3 + 1] * 2 - 1;
            coordsOpenGL[i * 3 + 2] = (float) 0.0;
        }

        return coordsOpenGL;
    }

    // rotation about x0, y0
    public float[] computeRotation(float x0, float y0, float angle, float[] vertex){

        float[] rotVertex = new float[vertex.length];
        float angleRad = (float) Math.toRadians(angle);

        for(int i = 0; i < vertex.length/3; i++) {

            // move to origin
            vertex[i * 3] = vertex[i * 3] - x0;
            vertex[i * 3 + 1] = vertex[i * 3 + 1] - y0;
            vertex[i * 3 + 2] = (float) 0.0;

            // rotate
            rotVertex[i * 3] =  (float)( vertex[i * 3] * Math.cos(angleRad) - vertex[i * 3 + 1] * Math.sin(angleRad) );
            rotVertex[i * 3 + 1] =  (float)( vertex[i * 3] * Math.sin(angleRad) + vertex[i * 3 + 1] * Math.cos(angleRad) );

            // move to x0, y0
            rotVertex[i * 3] = rotVertex[i * 3] + x0;
            rotVertex[i * 3 + 1] = rotVertex[i * 3 + 1] + y0;
            rotVertex[i * 3 + 2] = (float) 0.0;

        }

        return rotVertex;
    }

    // polygons from triangles x0 = [0, 1], RADIUS = SCALE
    public float[] computePolygonCoords(float x0, float y0, float radius, int nbrTriang){

        // Number of vertex
        int nbrVertex = nbrTriang + 1;
        float[] vertex = new float[3*nbrVertex];    // x,y,z for each vertex
        float angle = (float) Math.toRadians(360/nbrTriang);

        // add origin
        vertex[0] = x0;
        vertex[1] = y0;
        vertex[2] = (float) 0.0;

        for(int i = 1; i < nbrVertex; i++) {
            vertex[i * 3] = (float) (x0 + radius*Math.cos(angle*i));        // x
            vertex[i * 3 + 1] = (float) (y0 + radius*Math.sin(angle*i));    // y
            vertex[i * 3 + 2] = (float) 0.0;                                // z
        }

        return vertex;
    }

    public short[] computePolygonOrder(int nbrTriang){

        short[] order = new short[3*nbrTriang];

        for (int i = 0; i < nbrTriang; i++){

            order[i * 3] = 0;
            order[i * 3 + 1] = (short)(i + 1);

            if(i == nbrTriang-1)
                order[i * 3 + 2] = 1;
            else
                order[i * 3 + 2] = (short)(i + 2);

        }

        return order;
    }

    public float[] getOpenGLCoords() {
        return openGLCoords;
    }

    public float[] getOpenGLColor() {
        return openGLColor;
    }

    public short[] getOpenGLOrder() {
        return openGLOrder;
    }

    public float getPositionX0() {
        return openGLCoords[0];
    }

    public float getPositionY0() {
        return openGLCoords[1];
    }

    public float getScale(){
        return scale;
    }
}

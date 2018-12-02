package project.labonappssensiwall;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private List<HashMap<String,Object>> shapes = new ArrayList();

    private GeneralShape jacopo;

    private boolean stocazzo = false;
    private float[] coordsStocazzo;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0,0,0,1f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawList();
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void addDraw(){

    }

    public void setJacopo(float[] coords){
        stocazzo = true;
        coordsStocazzo = coords;
    }

    public void setDrawingList(List<HashMap<String,Object>> shapes){
        this.shapes = shapes;
    }

    public void drawList(){
        for(HashMap<String,Object> shape : shapes){
            float[] coords = (float[])shape.get("coords");
            short[] order = (short[])shape.get("order");
            float[] color = (float[])shape.get("color");
            GeneralShape tmpShape = new GeneralShape(coords,order,color);
            tmpShape.draw();
        }
    }

}

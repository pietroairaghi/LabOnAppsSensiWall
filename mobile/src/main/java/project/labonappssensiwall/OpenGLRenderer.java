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

    private List<HashMap<String,Object>> shapesData = new ArrayList();
    private List<GeneralShape> shapes = new ArrayList();

    private boolean updated = false;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0,0,0,1f);
        GLES20.glClearDepthf(-1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glDepthFunc(GLES20.GL_GEQUAL);
        //GLES20.glCullFace(GLES20.GL_BACK);
        //GLES20.glEnable(GLES20.GL_CULL_FACE);

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

    public void setDrawingList(List<HashMap<String,Object>> shapesData){
        this.shapesData = shapesData;
        updated = true;
    }

    public void drawList(){
        if(updated) {
            shapes.clear();
            for (HashMap<String, Object> shape : shapesData) {
                float[] coords = (float[]) shape.get("coords");
                short[] order = (short[]) shape.get("order");
                float[] color = (float[]) shape.get("color");
                shapes.add(new GeneralShape(coords, order, color));
            }
        }
        updated = false;

        for (GeneralShape shape : shapes){
            shape.draw();
        }

    }

}

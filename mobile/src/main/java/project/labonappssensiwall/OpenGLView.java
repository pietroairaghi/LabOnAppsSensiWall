package project.labonappssensiwall;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.List;

public class OpenGLView extends GLSurfaceView {

    private OpenGLRenderer mRenderer;

    public OpenGLView(Context context) {
        super(context);
        init();
    }

    public OpenGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        mRenderer = new OpenGLRenderer();

        setRenderer(mRenderer);
        //if not using an on demand requestRender();
        //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }
    public void changeCoso(List<HashMap<String,Object>> shapes){
        mRenderer.setDrawingList(shapes);
        requestRender();
    }

}

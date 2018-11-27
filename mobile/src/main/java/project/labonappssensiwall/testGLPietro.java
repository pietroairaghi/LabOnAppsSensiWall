package project.labonappssensiwall;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class testGLPietro extends AppCompatActivity {

    private OpenGLView openGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_glpietro);
        openGLView = (OpenGLView) findViewById(R.id.openGLViewID);
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
}

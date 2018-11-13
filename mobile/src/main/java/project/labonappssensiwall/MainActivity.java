package project.labonappssensiwall;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    private RadioGroup sessionsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionsRadioGroup = findViewById(R.id.sessionRadioGroup);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRadioButtons();
            }
        });


    }

    private void removeCurrentSessions(){
        //sessionsRadioGroup.removeAllViews(); questo era per rimuoverle tutte, potrebbe servire
        final int childCount = sessionsRadioGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = sessionsRadioGroup.getChildAt(i);
            // Eliminare V, come?
            // R.removeView(v);
        }
    }


    public void addRadioButtons() {
        removeCurrentSessions();
        RadioButton button;
        for(int i = 1; i < 10; i++) {
            button = new RadioButton(getApplicationContext());
            button.setId(i);
            button.setText("Button prova");
            sessionsRadioGroup.addView(button);
        }
    }
}

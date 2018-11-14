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

    public static final String SESSION_NAME = "Session";
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
        sessionsRadioGroup.clearCheck();
        //sessionsRadioGroup.removeAllViews(); questo era per rimuoverle tutte, potrebbe servire
        final int nButtons = sessionsRadioGroup.getChildCount();
        for (int i = 1; i < nButtons; i++) {
            sessionsRadioGroup.removeViewAt(1);
        }
    }


    public void addRadioButtons() {
        removeCurrentSessions();
        RadioButton button;
        for(int i = 1; i < 10; i++) {
            button = new RadioButton(getApplicationContext());
            button.setId(i);
            button.setText("Button prova" + i);
            button.setTag(R.id.sessionID,i);
            button.setTag(R.id.sessionName,"Nome Sessione " + i);
            sessionsRadioGroup.addView(button);
        }
    }
    

}

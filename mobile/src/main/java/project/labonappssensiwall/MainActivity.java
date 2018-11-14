package project.labonappssensiwall;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String SESSION_NAME = "Session";
    public static final String SESSION_ID = "Session ID";

    private RadioGroup sessionsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionsRadioGroup = findViewById(R.id.sessionRadioGroup);

        refreshSessionList();

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSessionList();
            }
        });


    }

    private void addRadioButtonToGroup(String text, String sessoinName, int sessoinID) {
        final int nButtons = sessionsRadioGroup.getChildCount();

        RadioButton button;
        button = new RadioButton(getApplicationContext());
        button.setId(nButtons+1);
        button.setText(text);
        button.setTag(R.id.sessionID,sessoinID);
        button.setTag(R.id.sessionName,sessoinName);
        sessionsRadioGroup.addView(button);
    }

    private void removeCurrentSessions(){
        sessionsRadioGroup.clearCheck();
        sessionsRadioGroup.removeAllViews();
    }


    public void refreshSessionList() {
        removeCurrentSessions();
        addRadioButtonToGroup("Create new session...","newActivity",0);
        for(int i = 1; i < 10; i++) {
            addRadioButtonToGroup("Button prova" + i,"Nome Sessione " + i,i);
        }
    }

    public void startSession(View view){
        final int selectedID = sessionsRadioGroup.getCheckedRadioButtonId();
        if(selectedID == -1){
            Toast toast = Toast.makeText(this, "please select a session", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        View radioButton = sessionsRadioGroup.findViewById(selectedID);
        String sessionName = (String) radioButton.getTag(R.id.sessionName);
        int sessionID = (int) radioButton.getTag(R.id.sessionID);




        Intent intent = new Intent(this, SessionActivity.class);
        intent.putExtra(SESSION_NAME, sessionName);
        intent.putExtra(SESSION_ID, sessionID);
        startActivity(intent);

    }
    

}

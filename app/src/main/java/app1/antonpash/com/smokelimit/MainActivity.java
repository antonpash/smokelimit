package app1.antonpash.com.smokelimit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editLimit;
    Button btnOk;
    SharedPreferences preferences;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editLimit = (EditText) findViewById(R.id.edit_limit);
        btnOk = (Button) findViewById(R.id.btn_ok);

        btnOk.setOnClickListener(this);

        preferences = getSharedPreferences("LIMIT", MODE_PRIVATE);

        int limit = preferences.getInt("limit", -1);

        if(limit != -1){
            toActivity();
        }

    }

    private void toActivity(){
        intent = new Intent(this, ManageActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_ok:

                String txtLimit = String.valueOf(editLimit.getText());

                if (!txtLimit.isEmpty()) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("limit", Integer.parseInt(txtLimit));
                    editor.apply();

                    toActivity();
                }

                break;
        }
    }
}

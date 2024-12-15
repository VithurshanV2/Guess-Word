package com.mad.guessword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText edit_name;
    private SharedPreferences sharedPreferences;

    private static final String SHARED_PREF_NAME = "mypref";    // Create shared preference name
    private static final String KEY_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, (imeInsets.bottom != 0) ? imeInsets.bottom : systemBars.bottom);
            return insets;
        });

        edit_name = findViewById(R.id.edit_name);
        Button btn_save = findViewById(R.id.btn_save);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        String name = sharedPreferences.getString(KEY_NAME, null);  //Activity open check whether shared preference data is available or not

        // If data available call HomeActivity
        if (name != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        btn_save.setOnClickListener(view -> {
            String name_input = edit_name.getText().toString().trim();
            if (name_input.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your name to proceed", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit(); //button will add data on shared preferences
                editor.putString(KEY_NAME, name_input);
                editor.apply();

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
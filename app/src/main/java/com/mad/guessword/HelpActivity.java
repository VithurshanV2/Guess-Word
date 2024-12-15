package com.mad.guessword;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HelpActivity extends AppCompatActivity {

    TextView tv_title, tv_details, tv_details2, tv_heading, tv_heading2;
    ImageView back_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_title = findViewById(R.id.tv_title);
        back_icon = findViewById(R.id.back_icon);
        tv_details = findViewById(R.id.tv_details);
        tv_details2 = findViewById(R.id.tv_details2);
        tv_heading = findViewById(R.id.tv_heading);
        tv_heading2 = findViewById(R.id.tv_heading2);

        tv_title.setText("HELP");
        back_icon.setVisibility(View.VISIBLE);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        String heading = "How to Play";
        String details = "This is a word guessing game where you are provided with 100 points at the beginning of the game, and you have 10 attempts, each costing 10 points. The user may get a hint if he/she fails to guess correctly during the first five attempts. The challenge is to guess the word correctly, fast, and with as few attempts as possible. Your personal best score will be recorded in the global leaderboard.\n\n"
                + " * Letter Count Button: You can get the letter count of the secret word at the cost of 5 points.\n"
                + " * Check Letter Occurrences: You can pick a letter at the cost of 5 points, and the game will tell you how many instances of that letter are present in the secret word.\n"
                + " * Guess Button: Each guess costs 10 points, and the timer for the game will start with your first guess.\n"
                + " * Game Start: By pressing the Start button, the game will begin, and a word will be available for guessing.\n"
                + " * Restart: When a game is running, you can restart it by pressing the Restart button.\n\n";


        String heading2 = "Score Details:";

        String details2 = " * Attempts: -10 points for each incorrect guess.\n"
                + " * Letter Count: -5 points for using the letter count button.\n"
                + " * Check Letter Occurrences: -5 points for checking letter occurrences.\n"
                + " * Timer: The timer will start after the first guess.";


        tv_heading.setText(heading);
        tv_details.setText(details);
        tv_heading2.setText(heading2);
        tv_details2.setText(details2);
    }
}
package com.mad.guessword;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private static final String SHARED_PREF_NAME = "mypref";    //Create shared preference name
    private static final String KEY_NAME = "name";  //Shared preference saved name
    private static final String DREAMLO_PRIVATE_KEY = BuildConfig.DREAMLO_PRIVATE_KEY;   //Dremlo api private url api key
    private static final String API_KEY = BuildConfig.API_KEY;   //Api key for thesaurus ninja api
    private int score = 100;
    private int attempts = 10;
    private boolean hint_used = false;
    private ProgressDialog progressDialog;
    private TextView data, tv_score, tv_letter_count, tv_letter_count_detail, tv_letter_occurrence_detail, tv_hint;
    private TextView[] attempt_boxes;
    private EditText edit_guess;
    private Button btn_guess, btn_letter_count, btn_check_occurrence, btn_start_stop;
    private Chronometer timer;
    private String thesaurus_url;
    private ImageView menu_icon;
    private SharedPreferences sharedPreferences;
    private String current_word = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, (imeInsets.bottom != 0) ? imeInsets.bottom : systemBars.bottom);
            return insets;
        });

        //Initialize the layout components
        tv_score = findViewById(R.id.tv_score);
        tv_hint = findViewById(R.id.tv_hint);
        tv_letter_count = findViewById(R.id.tv_letter_count);
        tv_letter_count_detail = findViewById(R.id.tv_letter_count_detail);
        tv_letter_occurrence_detail = findViewById(R.id.tv_letter_occurrence_detail);
        edit_guess = findViewById(R.id.edit_guess);
        btn_guess = findViewById(R.id.btn_guess);
        btn_letter_count = findViewById(R.id.btn_letter_count);
        btn_start_stop = findViewById(R.id.btn_start_stop);
        btn_check_occurrence = findViewById(R.id.btn_check_occurrence);
        timer = findViewById(R.id.timer);
        //data = findViewById(R.id.data);
        menu_icon = findViewById(R.id.menu_icon);
        attempt_boxes = new TextView[]{
                findViewById(R.id.box1), findViewById(R.id.box2), findViewById(R.id.box3), findViewById(R.id.box4), findViewById(R.id.box5),
                findViewById(R.id.box6), findViewById(R.id.box7), findViewById(R.id.box8), findViewById(R.id.box9), findViewById(R.id.box10)
        };

        disableInputs();
        menu_icon.setVisibility(View.VISIBLE);
        progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setMessage("Searching for a word");
        progressDialog.setCancelable(false);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE); //From MainActivity getting the saved name for the app
        String name = sharedPreferences.getString(KEY_NAME, null);


        //Buttons setOnClickListeners
        btn_start_stop.setOnClickListener(view -> {
            if (btn_start_stop.getText().toString().equals("Start")) {
                resetGame();
                btn_start_stop.setText("Stop");
                btn_start_stop.setBackgroundColor(Color.parseColor("#922b21"));
            } else {
                timer.stop();
                disableInputs();
                btn_start_stop.setText("Start");
                btn_start_stop.setBackgroundColor(Color.parseColor("#227825"));
            }
        });

        btn_guess.setOnClickListener(view -> {
            String user_guess_input = edit_guess.getText().toString().toLowerCase().trim();
            if (user_guess_input.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Please enter a word your guessing", Toast.LENGTH_SHORT).show();
            } else {
                if (user_guess_input.equals(current_word)) {
                    correctGuess(current_word, score);
                } else {
                    score -= 10;
                    attempts--;
                    scoreUpdate();
                    attemptBoxes();

                    if (attempts <= 0) {
                        incorrectGuess(current_word);
                    }
                }

                if (attempts == 5) {
                    giveTip();
                }

            }
        });

        btn_letter_count.setOnClickListener(view -> {
            if (attempts <= 0) {
                incorrectGuess(current_word);
                return;
            }

            if (score >= 5) {
                score -= 5;
                if (!hint_used && attempts > 0) {
                    attempts--;
                    hint_used = true;
                }

                int letter_count = current_word.length();
                tv_letter_count_detail.setText("Letter Count: " + letter_count);

                StringBuilder dash_lines = new StringBuilder();
                for (int i = 0; i < letter_count; i++) {
                    dash_lines.append("_ ");
                }
                tv_letter_count.setText(dash_lines.toString().trim());
                scoreUpdate();

                if (attempts <= 0 || score <= 0){
                    incorrectGuess(current_word);
                    return;
                }

                btn_letter_count.setEnabled(false);
                btn_letter_count.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808b96")));
                attemptBoxes();
            } else {
                Toast.makeText(HomeActivity.this, "Not enough points", Toast.LENGTH_SHORT).show();
            }
        });

        btn_check_occurrence.setOnClickListener(view -> {
            if (attempts <= 0) {
                incorrectGuess(current_word);
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Enter a letter");

            final EditText input = new EditText(HomeActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setMaxLines(1);
            input.setHint("One letter only");
            builder.setView(input);

            builder.setPositiveButton("Ok", (new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String letter = input.getText().toString().toLowerCase().trim();

                    if (letter.length() != 1) {
                        Toast.makeText(HomeActivity.this, "Please enter a valid single letter", Toast.LENGTH_SHORT).show();
                    } else if (score >= 5) {
                        score -= 5;
                        if (!hint_used) {
                            attempts--;
                            hint_used = true;
                        }

                        int occurrences = countOccurrences(current_word, letter.charAt(0));
                        tv_letter_occurrence_detail.setText("Occurrences of '" + letter + "' : " + occurrences);
                        scoreUpdate();

                        if (attempts <= 0 || score <= 0){
                            incorrectGuess(current_word);
                            return;
                        }

                        btn_check_occurrence.setEnabled(false);
                        btn_check_occurrence.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808b96")));
                        attemptBoxes();
                    } else {
                        Toast.makeText(HomeActivity.this, "Not enough points", Toast.LENGTH_SHORT).show();
                    }
                }
            }));

            builder.setNegativeButton("Cancel", (new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }));
            builder.show();
        });

        menu_icon.setOnClickListener(view -> {
            showMenu(view);
        });
    }


    //Methods
    private void randomNewWord() {
        progressDialog.show();
        disableInputs();

        //Two random word apis for any issues
        String random_word_url = "https://random-word-api.herokuapp.com/word";
        //String random_word_url = "https://random-word.ryanrk.com/api/en/word/random";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, random_word_url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    current_word = response.getString(0);
                    //data.setText(current_word); //For demonstration only
                    checkHintAvailability();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                timer.stop();
                disableInputs();
                btn_start_stop.setText("Start");
                btn_start_stop.setBackgroundColor(Color.parseColor("#227825"));
                Toast.makeText(HomeActivity.this, "Error loading new word", Toast.LENGTH_SHORT).show();

            }
        });
        Volley.newRequestQueue(this).add(request);
    }

    private void checkHintAvailability() {
        thesaurus_url = "https://api.api-ninjas.com/v1/thesaurus?word=" + current_word;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, thesaurus_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray synonyms = response.getJSONArray("synonyms");
                    if (synonyms.length() > 0) {
                        progressDialog.dismiss();
                        enableInputs();
                        timer.setBase(SystemClock.elapsedRealtime());
                        timer.start();
                        if (attempts == 5) {
                            giveTip();
                        }
                    } else {
                        randomNewWord();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(HomeActivity.this, "Error loading word", Toast.LENGTH_SHORT).show();
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Api-Key", API_KEY);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void giveTip() {
        thesaurus_url = "https://api.api-ninjas.com/v1/thesaurus?word=" + current_word;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, thesaurus_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray synonyms = response.getJSONArray("synonyms");
                    if (synonyms.length() > 0) {
                        String similar_word = synonyms.getString(0);
                        tv_hint.setText("Hint(Similar Word): " + similar_word);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this, "Error loading hint", Toast.LENGTH_SHORT).show();
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Api-Key", API_KEY);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void correctGuess(String correct_word, int score) {
        timer.stop();
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Correct Guess");

        long time_taken = SystemClock.elapsedRealtime() - timer.getBase();
        int time = (int) (time_taken / 1000);
        String player_name = sharedPreferences.getString(KEY_NAME, "player");
        addScore(player_name, score, time);

        String msg = "Congrats! The correct word was: " + correct_word +
                "\n\nScore: " + score +
                "\nTime: " + timer.getText();

        builder.setMessage(msg);

        builder.setPositiveButton("New Word", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                resetGame();
            }
        });

        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                timer.stop();
                disableInputs();
                btn_start_stop.setText("Start");
                btn_start_stop.setBackgroundColor(Color.parseColor("#227825"));

            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void incorrectGuess(String correct_word) {
        timer.stop();
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Incorrect Guess");

        String msg = "The correct word was: " + correct_word +
                "\nBetter luck next time";

        builder.setMessage(msg);

        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                resetGame();
            }
        });

        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                timer.stop();
                disableInputs();
                btn_start_stop.setText("Start");
                btn_start_stop.setBackgroundColor(Color.parseColor("#227825"));
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void enableInputs() {
        btn_guess.setEnabled(true);
        btn_letter_count.setEnabled(true);
        btn_check_occurrence.setEnabled(true);
        edit_guess.setEnabled(true);
    }

    private void disableInputs() {
        btn_guess.setEnabled(false);
        btn_letter_count.setEnabled(false);
        btn_check_occurrence.setEnabled(false);
        edit_guess.setEnabled(false);
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(HomeActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        String name = sharedPreferences.getString(KEY_NAME, "name");
        MenuItem name_item = popupMenu.getMenu().findItem(R.id.menu_name);
        name_item.setTitle(name);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_leaderboard) {
                    Intent leaderboardIntent = new Intent(HomeActivity.this, LeaderboardActivity.class);
                    startActivity(leaderboardIntent);
                    return true;
                } else if (menuItem.getItemId() == R.id.menu_help) {
                    Intent helpIntent = new Intent(HomeActivity.this, HelpActivity.class);
                    startActivity(helpIntent);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void resetGame() {
        score = 100;
        attempts = 10;
        scoreUpdate();
        randomNewWord();
        timer.setBase(SystemClock.elapsedRealtime());
        timer.stop();
        btn_letter_count.setEnabled(true);
        btn_letter_count.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#34495e")));
        btn_check_occurrence.setEnabled(true);
        btn_check_occurrence.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#34495e")));
        resetAttemptBoxes();
        edit_guess.setText("");
        tv_letter_count_detail.setText("Letter Count: ");
        tv_letter_occurrence_detail.setText("Occurrences of ' ' : ");
        tv_hint.setText("Hint(Similar Word): ");
        tv_letter_count.setText("");
        hint_used = false;
    }

    private int countOccurrences(String word, char letter) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                count++;
            }
        }
        return count;
    }

    private void attemptBoxes() {
        int failed_attempts = 10 - attempts;
        for (int i = 0; i < failed_attempts; i++) {
            attempt_boxes[i].setBackgroundResource(R.drawable.baseline_indeterminate_check_box_24);
        }
    }

    private void resetAttemptBoxes() {
        for (TextView box : attempt_boxes) {
            box.setBackgroundResource(R.drawable.baseline_indeterminate_check_box_24_green);
        }
    }

    private void scoreUpdate() {
        tv_score.setText("Score: " + score);
    }

    private void addScore(String player_name, int score, int time) {
        new addScoreMethod(player_name, score, time).execute();
    }

    private class addScoreMethod extends AsyncTask<Void, Void, Boolean> {
        private final String player_name;
        private final int score;
        private final int time;
        private int best_time;
        private int best_score;

        public addScoreMethod(String player_name, int score, int time) {
            this.player_name = player_name;
            this.score = score;
            this.time = time;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String url_string = "http://dreamlo.com/lb/" + DREAMLO_PRIVATE_KEY + "/pipe-get/" + player_name;
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url_string).openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line = in.readLine();
                in.close();

                if (line != null && !line.isEmpty()) {
                    String[] player_data = line.split("\\|");
                    if (player_data.length > 2) {
                        best_score = Integer.parseInt(player_data[1]);
                        best_time = Integer.parseInt(player_data[2]);
                        return (score > best_score) || (score == best_score && time < best_time);
                    }
                } else {
                    return true;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return false;
        }

        protected void onPostExecute(Boolean new_high_score) {
            if (new_high_score) {
                new Thread(() -> {
                    try {
                        String url_string = "http://dreamlo.com/lb/" + DREAMLO_PRIVATE_KEY + "/add/" + player_name + "/" + score + "/" + time;
                        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url_string).openConnection();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String serverResponse = reader.readLine();
                        reader.close();

                        runOnUiThread(() -> {
                            Toast.makeText(HomeActivity.this, "New High Score", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "Not your personal best", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}

package com.mad.guessword;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String DREAMLO_PUBLIC_KEY = BuildConfig.DREAMLO_PUBLIC_KEY;
    private ListView lv_leaderboard;
    private ArrayList<String> leaderboardEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        lv_leaderboard = findViewById(R.id.lv_leaderboard);
        leaderboardEntries = new ArrayList<>();
        TextView tv_title = findViewById(R.id.tv_title);
        ImageView back_icon = findViewById(R.id.back_icon);

        tv_title.setText("LEADERBOARD");
        back_icon.setVisibility(View.VISIBLE);
        back_icon.setOnClickListener(view -> onBackPressed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        if (isOnline()){
            new getLeaderboard().execute();
        } else {
            Toast.makeText(this, "No network connection please try again",Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isOnline(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private class getLeaderboard extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String response = null;
            try {
                String urlString = "http://dreamlo.com/lb/" + DREAMLO_PUBLIC_KEY + "/json"; //data get as json format
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlString).openConnection();

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                in.close();
                response = stringBuilder.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                addLeaderboard(result);
            } else {
                Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addLeaderboard(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject dreamlo = jsonObject.getJSONObject("dreamlo");
            JSONObject leaderboard = dreamlo.getJSONObject("leaderboard");
            JSONArray entryArray = leaderboard.getJSONArray("entry");

            leaderboardEntries.clear();

            for (int i = 0; i < entryArray.length(); i++) {
                JSONObject scoreEntry = entryArray.getJSONObject(i);
                String player_name = scoreEntry.getString("name");
                int player_score = scoreEntry.getInt("score");
                int player_time = scoreEntry.getInt("seconds");

                leaderboardEntries.add(player_name + "\n" + " SCORE: " + player_score + "pts       TIME: " + player_time + "s");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboardEntries);
            lv_leaderboard.setAdapter(adapter);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static class LeaderboardEntry {
        String player_name;
        int score;
        int time;

        LeaderboardEntry(String player_name, int score, int time) {
            this.player_name = player_name;
            this.score = score;
            this.time = time;
        }
    }
}
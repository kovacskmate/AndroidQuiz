package com.example.quizjava;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    QuizManager qm = QuizManager.getInstance();
    //min sdk version has to be set to 24
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qm.ReadQuizQuestions(getApplicationContext());
        checkFirstRun();

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent gameActivity = new Intent(MainActivity.this, Game.class);
            startActivity(gameActivity);
        });

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent settingsActivity = new Intent(MainActivity.this, Settings.class);
            startActivity(settingsActivity);
        });

        Button quitButton = findViewById(R.id.quitButton);
        quitButton.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });
    }

    //This code is from https://stackoverflow.com/questions/7217578/check-if-application-is-on-its-first-run by the users Squonk and Suragch
    private void checkFirstRun() {
        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            qm.ReadPreferencesFile(getApplicationContext());
            qm.UpdatePreferences();
            qm.ReadShowStatsPreferences(getApplicationContext());
            qm.SavePreferences(getApplicationContext());
            qm.ReadAnsweredIds(getApplicationContext());
            qm.ReadFirebaseAnswers(getApplicationContext());
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {
            //This is a new install (or the user cleared the shared preferences)
            qm.CreatePreferencesFile(getApplicationContext());
            qm.ReadPreferencesFile(getApplicationContext());
            qm.SetShowStatsPreferences(getApplicationContext(), true);
            qm.CreateAnsweredIdsFile(getApplicationContext());
            qm.CreateFirebaseAnswersFile(getApplicationContext());

        } else if (currentVersionCode > savedVersionCode) {
            //This is an upgrade, nothing to do here
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }
}

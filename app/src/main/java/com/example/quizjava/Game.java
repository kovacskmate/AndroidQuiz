package com.example.quizjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    QuizManager qm;

    public ArrayList<Question> activeQuestions;
    Integer questionNumber = 0;
    Integer correctlyAnswered = 0;

    ConstraintLayout pieLayout, constraintLayout;
    RelativeLayout relativeLayout;
    PieChart pieChart;
    TextView questionText, popUp, statsText;
    ArrayList<Button> buttons = new ArrayList<>();
    Button a;
    Button b;
    Button c;
    Button d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        qm = QuizManager.getInstance();
        constraintLayout = findViewById(R.id.constraintLayout);
        relativeLayout = findViewById(R.id.relativeLayout);

        activeQuestions = new ArrayList<>();
        questionText = findViewById(R.id.questionText);
        popUp = findViewById(R.id.popUp);
        statsText = findViewById(R.id.statsText);

        a = findViewById(R.id.aButton);
        b = findViewById(R.id.bButton);
        c = findViewById(R.id.cButton);
        d = findViewById(R.id.dButton);
        buttons.add(a);
        buttons.add(b);
        buttons.add(c);
        buttons.add(d);

        a.setOnClickListener(myClickListener);
        b.setOnClickListener(myClickListener);
        c.setOnClickListener(myClickListener);
        d.setOnClickListener(myClickListener);

        pieLayout = findViewById(R.id.pieLayout);
        pieChart = findViewById(R.id.piechart);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                CreateCircle();
            }
        }, 0, 8000);
        PrepareGame();
    }

    public void CreateCircle(){
        runOnUiThread(() -> {
            Circle circle = new Circle();
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageResource(R.drawable.circle);

            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(circle.fadeDuration);

            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setStartOffset(circle.fadeDuration + circle.lifeTime);
            fadeOut.setDuration(circle.fadeDuration);

            AnimationSet animation = new AnimationSet(false);
            animation.addAnimation(fadeIn);
            animation.addAnimation(fadeOut);
            animation.setRepeatCount(1);
            imageView.setAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener(){
                public void onAnimationStart(Animation animation) {
                }
                public void onAnimationRepeat(Animation animation) {
                }
                public void onAnimationEnd(Animation animation) {
                    //Should the imageviews be deleted somehow or is View.GONE enough?
                    imageView.setVisibility(View.GONE);
                }
            });

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(circle.size,circle.size);
            imageView.setX(circle.xPos);
            imageView.setY(circle.yPos);
            relativeLayout.addView(imageView, params);

            AnimatorSet mover = new AnimatorSet();
            int maxY = Resources.getSystem().getDisplayMetrics().heightPixels + (circle.size * 2);
            int maxX = Resources.getSystem().getDisplayMetrics().widthPixels + (circle.size * 2);
            Random r = new Random();
            ObjectAnimator moveX = ObjectAnimator.ofFloat(imageView, "translationX", r.nextInt(maxX + (circle.size * 2)) - (circle.size * 2));
            ObjectAnimator moveY = ObjectAnimator.ofFloat(imageView, "translationY", r.nextInt(maxY + (circle.size * 2)) - (circle.size * 2));
            mover.play(moveX);
            mover.play(moveY);
            moveX.setDuration(circle.lifeTime*4);
            moveY.setDuration(circle.lifeTime*4);
            mover.start();

            imageView.startAnimation(animation);
        });
    }

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Integer id = activeQuestions.get(questionNumber).getId();
            Button button = findViewById(v.getId());
            CreateFireBaseDocument(id);
            CheckAnswer(button, id);
            UpdateFireBaseAnswersTXT();
            button.setEnabled(false);
            WhatNext();
        }
    };

    public void PrepareGame(){
        //If the question has not been answered yet (its id is not in the answered.txt) and the question's category is
        //selected (its value is 1) then add that question into the activeQuestions
        PrepareQuestions();
        Collections.shuffle(activeQuestions);
        if(activeQuestions.size() != 0){
            AskQuestion();
        } else{
            EnableButtons(false);
            popUp.setVisibility(View.VISIBLE);
            popUp.setText("There are no more questions");
        }
    }

    public void PrepareQuestions(){
        activeQuestions = new ArrayList<>();
        for (Question question: qm.quizQuestions) {
            if(!qm.answeredIds.contains(question.getId()) && qm.categories.get(question.getCategory()) == 1){
                activeQuestions.add(question);
            }
        }
    }

    //resets the look of the buttons to the original then assigns the current question to the UI elements
    public void AskQuestion(){
        EnableButtons(true);
        ConstraintLayout rootLinearLayout = findViewById(R.id.constraintLayout);
        int count = rootLinearLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = rootLinearLayout.getChildAt(i);
            if (view instanceof Button) {
                view.setBackgroundResource(R.drawable.button_manager);
            }
        }
        questionText.setText(activeQuestions.get(questionNumber).getQuestion());
        a.setText(activeQuestions.get(questionNumber).getA());
        b.setText(activeQuestions.get(questionNumber).getB());
        c.setText(activeQuestions.get(questionNumber).getC());
        d.setText(activeQuestions.get(questionNumber).getD());
    }

    //checks if the given answer is correct
    public void CheckAnswer(Button button, int id){
        EnableButtons(false);
        if(button.getText().equals(activeQuestions.get(questionNumber).getCorrect())){
            correctlyAnswered++;
            qm.answeredIds.add(activeQuestions.get(questionNumber).getId());
            qm.SaveAnsweredIds(getApplicationContext());
            button.setBackgroundResource(R.drawable.correct_answer);
            IncrementFireBaseStatistics(id, 1);
        } else{
            button.setBackgroundResource(R.drawable.wrong_answer);
            //find correct button and highlight it green
            ConstraintLayout rootLinearLayout = findViewById(R.id.constraintLayout);
            int count = rootLinearLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = rootLinearLayout.getChildAt(i);
                if (view instanceof Button) {
                    if(((Button) view).getText().equals(activeQuestions.get(questionNumber).getCorrect())){
                        view.setBackgroundResource(R.drawable.correct_answer);
                    }
                }
            }
            IncrementFireBaseStatistics(id, 0);
        }
    }

    public void EnableButtons(boolean value){
        for (Button button: buttons) {
            button.setEnabled(value);
        }
    }

    //Creates a FireBase document with the title of the question's id
    //If the document already exists, the document is not overwritten with the 1-1 values
    //because the SetOptions.mergeFields() only merges the fields
    public void CreateFireBaseDocument(Integer id){
        Map<String, Integer> question = new HashMap<>();
        question.put("attempts", 1);
        question.put("correctAttempts", 1);
        db.collection("questions").document(id.toString())
                .set(question, SetOptions.mergeFields());
    }

    //To make it so that only the first attempt of answering a question counts to the statistics
    //the attempted question's id is added to the firebaseAnswers.txt
    //This method does not connect to FireBase
    public void UpdateFireBaseAnswersTXT(){
        if(!qm.firebaseAnswers.contains(activeQuestions.get(questionNumber).getId())){
            qm.firebaseAnswers.add(activeQuestions.get(questionNumber).getId());
            qm.SaveFirebaseAnswers(getApplicationContext());
        }
    }

    //increments the statistics of the current question in the FireBase database
    //if that question has not been answered yet
    //if the document exists but the "attempts" and "correctAttempts" fields do not, it will create the fields
    public void IncrementFireBaseStatistics(Integer id, int value){
        DocumentReference ref = db.collection("questions").document(id.toString());
        if(!qm.firebaseAnswers.contains(activeQuestions.get(questionNumber).getId())){
            ref.update("attempts", FieldValue.increment(1));
            ref.update("correctAttempts", FieldValue.increment(value));
        }
    }

    public void ShowPieChart(int correctAttempts, int attempts){
        ArrayList<PieEntry> values = new ArrayList<PieEntry>();
        values.add(new PieEntry(correctAttempts, ""));
        values.add(new PieEntry(attempts - correctAttempts, ""));
        pieChart.setUsePercentValues(true);
        pieChart.setTouchEnabled(false);
        pieChart.setHoleColor(R.color.russianGreen);
        pieChart.setHoleRadius(0.0f);
        pieChart.setTransparentCircleRadius(0.0f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.russianGreen));
        PieDataSet pieDataSet = new PieDataSet(values, "");
        pieDataSet.setColors(new int[] { R.color.correct, R.color.wrong}, getApplicationContext());
        pieDataSet.setDrawValues(false);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        double percentage = (double)correctAttempts/(double)attempts * 100;
        double scale = Math.pow(10, 0);
        percentage = Math.round(percentage * scale) / scale;
        statsText.setText( (int)percentage + "% of users get this question right on the first try");
        pieLayout.setVisibility(View.VISIBLE);
    }

    public void WhatNext(){
        if(qm.showStats){
            (new Handler()).postDelayed(this::ShowStatistics, 1500);
        } else{
            (new Handler()).postDelayed(this::NextQuestion, 1500);
        }
    }

    public void ShowStatistics(){
        Integer id = activeQuestions.get(questionNumber).getId();
        DocumentReference docRef = db.collection("questions").document(id.toString());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String correctAttempts = document.getData().get("correctAttempts").toString();
                    String attempts = document.getData().get("attempts").toString();
                    ShowPieChart(Integer.parseInt(correctAttempts), Integer.parseInt(attempts));
/*                                popUp.setVisibility(View.VISIBLE);
                            popUp.setText(document.getData().get("correctAttempts") + " out of " + document.getData().get("attempts") +
                                    " users got this question right on the first try.");*/
                } else {

                    Log.d("", "No such document");
                }
            } else {
                Log.d("", "get failed with ", task.getException());
            }
        });
        (new Handler()).postDelayed(this::NextQuestion, 5000);
    }

    public void NextQuestion(){
        questionNumber++;
        pieLayout.setVisibility(View.INVISIBLE);
        if(correctlyAnswered == activeQuestions.size()){
            popUp.setVisibility(View.VISIBLE);
            popUp.setText("There are no more questions");
        } else{
            if(questionNumber == activeQuestions.size()){
                PrepareQuestions();
                questionNumber = 0;
                correctlyAnswered = 0;
            }
            AskQuestion();
        }
    }
}

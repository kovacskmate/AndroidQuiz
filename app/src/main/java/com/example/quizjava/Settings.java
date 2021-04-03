package com.example.quizjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Settings extends AppCompatActivity {
    QuizManager qm = QuizManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Button button = findViewById(R.id.resetButton);
        button.setOnClickListener(v -> {
            qm.CreateAnsweredIdsFile(getApplicationContext());
            qm.answeredIds.clear();
        });
        final CheckBox showStats = findViewById(R.id.showStatsChckbx);
        if(qm.showStats){
            showStats.setChecked(true);
        }else{
            showStats.setChecked(false);
        }
        showStats.setOnClickListener(v -> {
            if(showStats.isChecked()){
                qm.SetShowStatsPreferences(getApplicationContext(), true);
            } else{
                qm.SetShowStatsPreferences(getApplicationContext(), false);
            }
        });
        CreateCategories();
    }

    private View.OnClickListener myClickListener = v -> {
        CheckBox checkBox = findViewById(v.getId());
        int value;
        if(checkBox.isChecked()){
            value = 1;
        } else{
            value = 0;
        }
        String tag = (String) v.getTag();
        qm.categories.replace(tag, value);
        qm.SavePreferences(getApplicationContext());
    };

    public void CreateCategories(){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0,0,20);
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        for (Map.Entry<String, Integer> entry : qm.categories.entrySet()) {
            LinearLayout row = new LinearLayout(getApplicationContext());
            row.setLayoutParams(layoutParams);
            //CreateCategoryText(row, entry.getKey());
            CreateCheckBox(row, entry);
            row.setGravity(Gravity.CENTER);
            linearLayout.addView(row);
        }
    }

    public void CreateCheckBox(LinearLayout row, Map.Entry<String, Integer> entry){
        CheckBox checkBox = new CheckBox(this);
        if(entry.getValue() == 1){
            checkBox.setChecked(true);
        } else{
            checkBox.setChecked(false);
        }
        checkBox.setTag(entry.getKey());
        checkBox.setOnClickListener(myClickListener);
        //checkBox has to get an automatically generated id otherwise v.getId() can't find it
        checkBox.setId(View.generateViewId());
        checkBox.setTextSize(34);
        checkBox.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.whiteLike));
        checkBox.setText(entry.getKey());
        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {ContextCompat.getColor(getApplicationContext(), R.color.sacramentoGreen),
                ContextCompat.getColor(getApplicationContext(), R.color.sacramentoGreen)};
        CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));
        checkBox.setScaleX(1.4f);
        checkBox.setScaleY(1.4f);
        //downscale the text only because setScale scales the text too
        checkBox.setTextSize(34/1.6f);
        row.addView(checkBox);
    }

    public void CreateCategoryText(LinearLayout row, String text){
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(34);
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.whiteLike));
        row.addView(textView);
    }
}
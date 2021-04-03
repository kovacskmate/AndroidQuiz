package com.example.quizjava;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class QuizManager extends Application{

    private static QuizManager Instace = null;

    //list of all questions
    public  ArrayList<Question> quizQuestions = new ArrayList<Question>();
    //list of all existing categories
    public  ArrayList<String> quizCategories = new ArrayList<String>();

    //list of categories in the user preferences file
    public  Map<String, Integer> categories = new HashMap<String, Integer>();

    //list of the ids of questions that have been answered - can be reset - once an id is added to it, it can be removed so that question can be answered again
    public ArrayList<Integer> answeredIds = new ArrayList<Integer>();
    //list of the ids of questions that have been answered - can not be reset - once an id is added to it, it can not be removed to make the firebase statistics accurate
    public ArrayList<Integer> firebaseAnswers = new ArrayList<Integer>();

    public boolean showStats = true;

    private QuizManager() {
    }

    public static QuizManager getInstance() {
        if (Instace == null) {
            Instace = new QuizManager();
        }
        return(Instace);
    }


    public void CreatePreferencesFile(Context ctx){
        try {
            FileOutputStream fileout = ctx.openFileOutput("preferences.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);

            for(int i = 0; i < quizQuestions.size(); i++){
                outputWriter.write(quizQuestions.get(i).getCategory() + ";1\n");
            }
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SavePreferences(Context ctx){
        try {
            FileOutputStream fileout = ctx.openFileOutput("preferences.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);

            for (Map.Entry mapElement : categories.entrySet()) {
                String key = (String)mapElement.getKey();
                int value = (int)mapElement.getValue();
                outputWriter.write(key + ";" + value + "\n");
            }
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ReadPreferencesFile(Context ctx){
        String s="";
        try {
            FileInputStream fileIn = ctx.openFileInput("preferences.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[100];

            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] lines = s.split("\n");
        String[] line;
        for(int i = 0; i < lines.length; i++){
            line = lines[i].split(";");
            if(quizCategories.contains(line[0])){
                categories.put(line[0], Integer.parseInt(line[1]));
            }
        }
    }


    //checks if there are any categories in the quizQuestions that do not exist in the categories/preferences
    //if there are, add them with the value of 1(true)
    public void UpdatePreferences(){
        for(int i = 0; i < quizQuestions.size(); i++){
            if(!categories.containsKey(quizQuestions.get(i).getCategory())){
                categories.put(quizQuestions.get(i).getCategory(), 1);
            }
        }
    }

    public void SetShowStatsPreferences(Context ctx, boolean value){
        showStats = value;
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean("showStats", value).apply();
    }

    public void ReadShowStatsPreferences(Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        showStats = prefs.getBoolean("showStats", true);
    }

    public void CreateAnsweredIdsFile(Context ctx){
        try {
            FileOutputStream fileout = ctx.openFileOutput("answered.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SaveAnsweredIds(Context ctx){
        try {
            FileOutputStream fileout = ctx.openFileOutput("answered.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);

            for (Integer id : answeredIds) {
                outputWriter.write(id + ";");

            }
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ReadAnsweredIds(Context ctx){
        String s="";
        try {
            FileInputStream fileIn = ctx.openFileInput("answered.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[100];

            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                String readstring = String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] line = s.split(";");

        for(int i = 0; i < line.length; i++){
            //check for empty string to prevent parsing an empty string to int (when the answers.txt is empty)
            if(!line[i].isEmpty()){
                answeredIds.add(Integer.parseInt(line[i]));
            }
        }
    }

    public void CreateFirebaseAnswersFile(Context ctx){
        try {
            FileOutputStream fileout = ctx.openFileOutput("firebaseAnswers.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SaveFirebaseAnswers(Context ctx){
        try {
            FileOutputStream fileout = ctx.openFileOutput("firebaseAnswers.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);

            for (Integer id : firebaseAnswers) {
                outputWriter.write(id + ";");

            }
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ReadFirebaseAnswers(Context ctx){
        String s="";
        try {
            FileInputStream fileIn = ctx.openFileInput("firebaseAnswers.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[100];

            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                String readstring = String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] line = s.split(";");

        for(int i = 0; i < line.length; i++){
            //check for empty string to prevent parsing an empty string to int (when the answers.txt is empty)
            if(!line[i].isEmpty()){
                firebaseAnswers.add(Integer.parseInt(line[i]));
            }
        }

    }

    public void ReadQuizQuestions(Context ctx){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ctx.getAssets().open("quizQuestions")));

            String mLine;
            String[] line;
            //skip first line
            reader.readLine();

            while ((mLine = reader.readLine()) != null) {
                line = mLine.split(";");
                quizCategories.add(line[7]);
                quizQuestions.add(new Question(Integer.parseInt(line[0]), line[1], line[2], line[3], line[4], line[5], line[6], line[7]));
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }
}

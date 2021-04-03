package com.example.quizjava;
import android.content.res.Resources;
import java.util.Random;

public class Circle {
    int size;
    int xPos;
    int yPos;
    int fadeDuration;
    int lifeTime;

    public Circle(){
        //navbar size is not taken into consideration
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        Random r = new Random();
        size = r.nextInt(height/3-height/8) + height/8;
        xPos = r.nextInt((width-size) - 0) + 0;
        yPos = r.nextInt((height-size) - 0) + 0;
        fadeDuration = r.nextInt(5000-3000) + 3000;
        lifeTime = r.nextInt(30000-10000) + 10000;
    }
}

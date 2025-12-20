package com.megaproject.passwordgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Simple Fade In Animation for Content
        View content = findViewById(R.id.center_content);
        
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(800);
        
        Animation slideUp = new TranslateAnimation(0, 0, 50, 0);
        slideUp.setInterpolator(new DecelerateInterpolator());
        slideUp.setDuration(800);
        
        android.view.animation.AnimationSet set = new android.view.animation.AnimationSet(false);
        set.addAnimation(fadeIn);
        set.addAnimation(slideUp);
        
        content.startAnimation(set);

        // Delay and launch Main
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3000); // 3 seconds
    }
}

package com.example.proyecto_hospital;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo); // Asegúrate de que este sea el ID de tu ImageView

        // Cargar y aplicar la animación
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        logo.startAnimation(scaleAnimation);

        // Inicializa y reproduce el sonido
        mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound);
        mediaPlayer.start();

        // Delay para mostrar el splash screen
        new Handler().postDelayed(() -> {
            mediaPlayer.stop(); // Detén la música
            mediaPlayer.release(); // Libera recursos

            // Inicia la actividad de Login
            Intent intent = new Intent(splash.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Finaliza la actividad del splash
        }, 4000); // 2 segundos
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Asegúrate de liberar recursos al destruir la actividad
        }
    }
}


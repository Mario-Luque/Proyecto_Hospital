package com.example.proyecto_hospital;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;



public class menu_areas extends AppCompatActivity {

    private ImageButton btnCardiologia, btnPediatria, btnGinecologia, btnNeurologia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu_areas);

        btnCardiologia = findViewById(R.id.btnCardiologia);
        btnPediatria = findViewById(R.id.btnPediatria);
        btnGinecologia = findViewById(R.id.btnGinecologia);
        btnNeurologia = findViewById(R.id.btnNeurologia);

        btnCardiologia.setOnClickListener(v -> mostrarMedicosPorArea("Cardiologia"));
        btnPediatria.setOnClickListener(v -> mostrarMedicosPorArea("Pediatria"));
        btnGinecologia.setOnClickListener(v -> mostrarMedicosPorArea("Ginecologia"));
        btnNeurologia.setOnClickListener(v -> mostrarMedicosPorArea("Neurologia"));
    }

    private void mostrarMedicosPorArea(String area) {
        Intent intent = new Intent(this, listar_medicos.class);
        intent.putExtra("area", area);
        startActivity(intent);
    }
}
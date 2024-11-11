package com.example.proyecto_hospital;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class menu extends AppCompatActivity {
    private String dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);

        // Obtener el DNI del Intent
        dni = getIntent().getStringExtra("dni");

        ImageButton btnListarHistorial = findViewById(R.id.btn_listar_historial);
        btnListarHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(menu.this, listar_historial.class);
                startActivity(intent);
            }
        });

        ImageButton btnEditUser = findViewById(R.id.btn_edit_user);
        btnEditUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(menu.this, EditarUsuario.class);
                intent.putExtra("dni", dni);
                startActivity(intent);
            }
        });

        // Cambiar el tipo a ImageButton
        ImageButton btnRegisterLab = findViewById(R.id.btn_register_lab);
        btnRegisterLab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(menu.this, listar_prueba_laboratorio.class);
                startActivity(intent);
            }
        });

        ImageButton btnCitaMedica = findViewById(R.id.btn_cita_medica);
        btnCitaMedica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(menu.this, menu_areas.class);
                startActivity(intent);
            }
        });

        ImageButton btnComprarMedicina = findViewById(R.id.btn_comprar_medicina);
        btnComprarMedicina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(menu.this, listar_medicina.class);
                startActivity(intent);
            }
        });

        ImageButton btnUbicacionHospital = findViewById(R.id.btn_ubicacion_hospital);
        btnUbicacionHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(menu.this, Localizacion_Hospital.class);
                startActivity(intent);
            }
        });

        // Configuración del botón de Cerrar sesión
        Button btnLogout = findViewById(R.id.btn_Cerrar); // Asegúrate de que el ID es el correcto
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes borrar cualquier información guardada, como el DNI o tokens de sesión.
                // Ejemplo: Usar SharedPreferences para borrar los datos de la sesión
                SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear(); // Elimina todos los datos guardados
                editor.apply();

                // Redirigir al usuario a la pantalla de inicio de sesión
                Intent intent = new Intent(menu.this, LoginActivity.class); // Asegúrate de que tu clase de Login se llama así
                startActivity(intent);

                // Cierra la actividad actual
                finish();
            }
        });
    }
}
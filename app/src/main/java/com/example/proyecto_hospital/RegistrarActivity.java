package com.example.proyecto_hospital;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegistrarActivity extends AppCompatActivity {

    // Componentes de la interfaz de usuario
    EditText t_nombres, t_apellidos, t_dni, t_email, t_direccion, t_celular, t_password;
    Button b_registrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar el layout de la actividad
        setContentView(R.layout.activity_registrar);

        // Inicializar los campos de texto y el botón de registro
        t_nombres = findViewById(R.id.nombres);
        t_apellidos = findViewById(R.id.apellidos);
        t_dni = findViewById(R.id.dni);
        t_email = findViewById(R.id.email);
        t_password = findViewById(R.id.password);
        t_direccion = findViewById(R.id.direccion);
        t_celular = findViewById(R.id.celular);

        b_registrar = findViewById(R.id.btnregistrar);

        // Configurar el clic del botón para llamar al método de registro
        b_registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrar(); // Llamar al método de registro
            }
        });
    }

    // Método para registrar un nuevo usuario
    private void registrar() {
        // Obtener los datos ingresados en los campos de texto
        final String nombres = t_nombres.getText().toString().trim();
        final String apellidos = t_apellidos.getText().toString().trim();
        final String dni = t_dni.getText().toString().trim();
        final String email = t_email.getText().toString().trim();
        final String direccion = t_direccion.getText().toString().trim();
        final String celular = t_celular.getText().toString().trim();
        final String password = t_password.getText().toString().trim();

        // Mostrar un ProgressDialog mientras se hace la solicitud
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando");

        // Validar que los campos obligatorios no estén vacíos
        if (nombres.isEmpty()) {
            t_nombres.setError("Complete los campos");
            return;
        } else if (email.isEmpty()) {
            t_email.setError("Complete los campos");
            return;
        } else {
            // Mostrar el ProgressDialog mientras se realiza la solicitud
            progressDialog.show();

            // Crear la solicitud POST para registrar al usuario
            StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.0.9:80/crud/registrar_usuario.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Verificar la respuesta del servidor
                            if (response.equalsIgnoreCase("Registro Correcto")) {
                                Toast.makeText(RegistrarActivity.this, "Datos Registrados", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                                // Redirigir al login después de un registro exitoso
                                Intent intent = new Intent(RegistrarActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                // Mostrar el mensaje de error si el registro falla
                                Toast.makeText(RegistrarActivity.this, response, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                Toast.makeText(RegistrarActivity.this, "No se pudo registrar", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Manejar errores de conexión o de la solicitud
                    Toast.makeText(RegistrarActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Enviar los datos del formulario al servidor
                    Map<String, String> params = new HashMap<>();
                    params.put("nombres", nombres);
                    params.put("apellidos", apellidos);
                    params.put("dni", dni);
                    params.put("email", email);
                    params.put("direccion", direccion);
                    params.put("celular", celular);
                    params.put("password", password);

                    return params;
                }
            };

            // Crear la cola de solicitudes y agregar la solicitud a la cola
            RequestQueue requestQueue = Volley.newRequestQueue(RegistrarActivity.this);
            requestQueue.add(request);
        }
    }

    @Override
    public void onBackPressed() {
        // Permitir que el usuario regrese a la pantalla anterior
        super.onBackPressed();
        finish();
    }

    // Método para ir a la pantalla de login
    public void login(View view) {
        // Iniciar la actividad de login
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish(); // Finalizar la actividad actual
    }
}
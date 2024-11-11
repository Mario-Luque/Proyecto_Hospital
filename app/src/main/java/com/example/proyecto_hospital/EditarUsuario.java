package com.example.proyecto_hospital;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditarUsuario extends AppCompatActivity {

    // Declaración de las vistas para los campos de entrada y el botón
    EditText t_nombres, t_apellidos, t_email, t_direccion, t_celular, t_password;
    Button b_guardar;

    // Variables para almacenar el DNI del usuario y la contraseña original
    String dni, pass_original;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el layout de la actividad
        setContentView(R.layout.activity_editar_usuario);

        // Inicializar las vistas
        t_nombres = findViewById(R.id.nombres);
        t_apellidos = findViewById(R.id.apellidos);
        t_email = findViewById(R.id.email);
        t_direccion = findViewById(R.id.direccion);
        t_celular = findViewById(R.id.celular);
        t_password = findViewById(R.id.password);
        b_guardar = findViewById(R.id.btn_guardar);

        // Obtener el DNI del usuario desde el Intent (se pasa como extra)
        dni = getIntent().getStringExtra("dni");

        // Cargar los datos del usuario para mostrarlos en los campos
        cargarDatos(dni);

        // Configurar el listener para el botón "guardar"
        b_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarDatos();  // Llamar a la función para guardar los datos
            }
        });
    }

    // Método para cargar los datos del usuario desde el servidor
    private void cargarDatos(String dni) {

        // URL para hacer la solicitud al servidor
        String url = "http://192.168.0.9:80/crud/mostrar_usuario.php";

        // Crear la solicitud POST usando Volley
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Convertir la respuesta en formato JSON
                    JSONObject jsonObject = new JSONObject(response);

                    // Rellenar los campos de entrada con los datos obtenidos
                    t_nombres.setText(jsonObject.getString("nombres"));
                    t_apellidos.setText(jsonObject.getString("apellidos"));
                    t_email.setText(jsonObject.getString("email"));
                    t_direccion.setText(jsonObject.getString("direccion"));
                    t_celular.setText(jsonObject.getString("celular"));
                    pass_original = jsonObject.getString("password");  // Almacenar la contraseña original
                    t_password.setText(pass_original);  // Mostrar la contraseña original en el campo de texto
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Mostrar un mensaje de error si la solicitud falla
                Toast.makeText(EditarUsuario.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            // Pasar el DNI como parámetro en la solicitud
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("dni", dni);  // Enviar el DNI para obtener los datos del usuario
                return params;
            }
        };

        // Crear una cola de solicitudes y añadir la solicitud
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Método para guardar los datos del usuario en el servidor
    private void guardarDatos() {
        // Obtener los valores de los campos de entrada
        String nombres = t_nombres.getText().toString().trim();
        String apellidos = t_apellidos.getText().toString().trim();
        String email = t_email.getText().toString().trim();
        String direccion = t_direccion.getText().toString().trim();
        String celular = t_celular.getText().toString().trim();
        String password = t_password.getText().toString().trim();

        // URL para hacer la solicitud de actualización de datos
        String url = "http://192.168.0.9:80/crud/actualizar_usuario.php";

        // Crear la solicitud POST para actualizar los datos
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Mostrar el mensaje de respuesta del servidor
                Toast.makeText(EditarUsuario.this, response, Toast.LENGTH_SHORT).show();

                // Verificar si la contraseña fue modificada
                if (!password.equals(pass_original)) {
                    // Si la contraseña fue cambiada, cerrar sesión automáticamente
                    cerrarSesion();
                } else {
                    // Si no hubo cambios en la contraseña, simplemente cerrar la actividad
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Mostrar un mensaje de error si la solicitud falla
                Toast.makeText(EditarUsuario.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            // Pasar los parámetros a la solicitud POST
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("dni", dni);  // Enviar el DNI para identificar al usuario
                params.put("nombres", nombres);  // Enviar los nuevos valores de los campos
                params.put("apellidos", apellidos);
                params.put("email", email);
                params.put("direccion", direccion);
                params.put("celular", celular);

                // Solo enviar la nueva contraseña si el campo no está vacío
                if (!password.isEmpty()) {
                    params.put("password", password);
                }

                return params;
            }
        };

        // Crear una cola de solicitudes y añadir la solicitud
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Método para cerrar sesión cuando se cambia la contraseña
    private void cerrarSesion() {
        // Eliminar la información de la sesión guardada en SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();  // Eliminar todos los datos de la sesión
        editor.apply();  // Aplicar los cambios

        // Regresar a la actividad de inicio de sesión
        Intent intent = new Intent(EditarUsuario.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);  // Limpiar la pila de actividades
        startActivity(intent);
        finish();  // Finalizar la actividad actual
    }
}

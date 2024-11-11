package com.example.proyecto_hospital;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity {

    // Componentes de la interfaz de usuario
    EditText t_dni, t_password;

    // Variables para almacenar los datos de login
    String str_dni, str_password;

    // URL del servidor para la autenticación
    String url = "http://192.168.0.9:80/crud/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Configurar el layout de la actividad
        setContentView(R.layout.activity_login);

        // Inicializar los campos de texto
        t_dni = findViewById(R.id.dni);
        t_password = findViewById(R.id.password);
    }

    // Método para manejar el proceso de login
    public void login(View view){
        // Validar si los campos están vacíos
        if(t_dni.getText().toString().equals("")){
            Toast.makeText(this,"Ingrese DNI",Toast.LENGTH_SHORT).show();
        }else if(t_password.getText().toString().equals("")){
            Toast.makeText(this,"Ingrese la Contraseña",Toast.LENGTH_SHORT).show();
        }else {
            // Mostrar un ProgressDialog mientras se hace la solicitud
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Espere por favor");
            progressDialog.show();

            // Obtener los valores ingresados en los campos
            str_dni = t_dni.getText().toString().trim();
            str_password = t_password.getText().toString().trim();

            // Crear la solicitud POST para el login
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    // Comprobar si el login fue exitoso
                    if (response.equalsIgnoreCase("Ingreso correcto")) {
                        // Limpiar los campos de texto
                        t_dni.setText("");
                        t_password.setText("");

                        // Guardar el DNI en SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("dni", str_dni);
                        editor.apply(); // Aplicar los cambios

                        // Iniciar la actividad del menú
                        Intent intent = new Intent(getApplicationContext(), menu.class);
                        intent.putExtra("dni", str_dni); // Pasar el DNI a la siguiente actividad
                        startActivity(intent);
                    } else {
                        // Mostrar el mensaje de error en caso de fallo
                        Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    // Mostrar un mensaje de error si ocurre un fallo en la conexión
                    Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Enviar los parámetros (DNI y contraseña) al servidor
                    Map<String, String> params = new HashMap<>();
                    params.put("dni", str_dni);
                    params.put("password", str_password);
                    return params;
                }
            };

            // Crear y agregar la solicitud a la cola de Volley
            RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
            requestQueue.add(request);
        }
    }

    // Método para manejar la acción de registro
    public void registrar(View view){
        // Iniciar la actividad de registro
        startActivity(new Intent(getApplicationContext(), RegistrarActivity.class));
        finish(); // Finalizar la actividad de login
    }
}

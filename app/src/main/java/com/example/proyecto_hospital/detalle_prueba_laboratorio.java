package com.example.proyecto_hospital;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class detalle_prueba_laboratorio extends AppCompatActivity {

    // Declaración de vistas de la interfaz de usuario
    private TextView detalleNom, detallePre, detalleDes;
    private ImageView detalleImagen;
    private EditText fechaHoraInput;
    private Button btnAdquirir;

    // Variables para manejar la solicitud de red con Volley y almacenar la fecha, hora y DNI
    private RequestQueue rq;
    private String fechaSeleccionada;
    private String horaSeleccionada;
    private String dni;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_prueba_laboratorio);

        // Inicializar vistas desde el XML
        detalleNom = findViewById(R.id.detalleNom);
        detallePre = findViewById(R.id.detallePrecio);
        detalleDes = findViewById(R.id.detalleDes);
        detalleImagen = findViewById(R.id.detalleImage);
        fechaHoraInput = findViewById(R.id.fechaHoraInput);
        btnAdquirir = findViewById(R.id.btn_adquirir);

        // Inicializar la cola de solicitudes Volley para manejar imágenes
        rq = Volley.newRequestQueue(this);

        // Obtener el DNI desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        dni = prefs.getString("dni", null);

        // Obtener la posición de la prueba seleccionada desde el Intent
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);

        // Cargar los datos de la prueba seleccionada si la posición es válida
        if (position != -1) {
            detalleNom.setText(listar_prueba_laboratorio.listaUsuarios.get(position).getNombre());
            detallePre.setText(String.valueOf(listar_prueba_laboratorio.listaUsuarios.get(position).getPrecio()));
            detalleDes.setText(String.valueOf(listar_prueba_laboratorio.listaUsuarios.get(position).getDescripcion()));
            cargarImagenUrl(listar_prueba_laboratorio.listaUsuarios.get(position).getUrlimagen());
        }

        // Configurar el listener para el campo de fecha y hora (EditText)
        fechaHoraInput.setOnClickListener(v -> mostrarDatePicker());

        // Configurar el listener para el botón de adquirir
        btnAdquirir.setOnClickListener(v -> enviarDatos());
    }

    // Método para cargar la imagen desde una URL usando Volley
    private void cargarImagenUrl(String url) {
        ImageRequest request = new ImageRequest(url,
                bitmap -> detalleImagen.setImageBitmap(bitmap), 0, 0, ImageView.ScaleType.CENTER_CROP, null, volleyError -> {});
        rq.add(request);  // Añadir la solicitud a la cola de Volley
    }

    // Muestra un DatePickerDialog para seleccionar una fecha
    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear y mostrar el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    fechaSeleccionada = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    mostrarTimePicker();  // Después de elegir la fecha, mostrar el TimePicker
                }, year, month, day);
        datePickerDialog.show();
    }

    // Muestra un TimePickerDialog para seleccionar una hora
    private void mostrarTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Crear y mostrar el TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    horaSeleccionada = String.format("%02d:%02d:%02d", selectedHour, selectedMinute, 0);
                    // Actualizar el campo EditText con la fecha y hora seleccionada
                    fechaHoraInput.setText(fechaSeleccionada + " " + horaSeleccionada);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    // Método para validar si la fecha y hora seleccionadas son posteriores a la fecha y hora actual
    private boolean validarFechaHora() {
        try {
            // Obtener la fecha y hora actual
            Calendar calendarActual = Calendar.getInstance();
            SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            // Formatear la fecha seleccionada para comparar
            String fechaHoraSeleccionadaStr = fechaSeleccionada + " " + horaSeleccionada;
            Date fechaHoraSeleccionada = formatoFechaHora.parse(fechaHoraSeleccionadaStr);

            // Comparar la fecha y hora seleccionadas con la actual
            if (fechaHoraSeleccionada != null && fechaHoraSeleccionada.before(calendarActual.getTime())) {
                Toast.makeText(this, "La fecha y hora seleccionadas deben ser posteriores a la fecha y hora actual.", Toast.LENGTH_LONG).show();
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Método para validar los campos antes de enviar los datos
    private boolean validarCampos() {
        if (fechaHoraInput.getText().toString().isEmpty()) {
            fechaHoraInput.setError("Debe seleccionar fecha y hora");
            return false;
        }
        return true;
    }

    // Método para enviar los datos de la prueba seleccionada al servidor
    private void enviarDatos() {
        // Validar que los campos no estén vacíos antes de continuar
        if (!validarCampos()) {
            return;  // Si la validación falla, no continuar con el envío de datos
        }

        // Validar si la fecha y hora seleccionadas son válidas (posteriores a la fecha actual)
        if (!validarFechaHora()) {
            return;  // Si la validación falla, no continuar con el envío de datos
        }

        // Recuperar los datos de la prueba desde las vistas
        String nombre = detalleNom.getText().toString();
        String precio = detallePre.getText().toString();
        String tipo = "Prueba de laboratorio";
        String descripcion = nombre;

        // Convertir la fecha seleccionada al formato YYYY-MM-DD
        String[] partesFecha = fechaSeleccionada.split("/");
        String fechaFormateada = partesFecha[2] + "-" + partesFecha[1] + "-" + partesFecha[0];

        // Log para depuración (opcional)
        Log.d("Datos Enviados", "DNI: " + dni + ", Tipo: " + tipo + ", Descripción: " + descripcion +
                ", Precio: " + precio + ", Fecha: " + fechaFormateada + ", Hora: " + horaSeleccionada);

        // Ejecutar la solicitud de red en un hilo separado para evitar bloquear el hilo principal
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                // Conectar al servidor y preparar la solicitud HTTP POST
                URL url = new URL("http://192.168.0.9:80/crud/registrar_historial.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Preparar los datos a enviar (parámetros de la solicitud)
                String postData = "dni=" + URLEncoder.encode(dni, "UTF-8") +
                        "&tipo=" + URLEncoder.encode(tipo, "UTF-8") +
                        "&descripcion=" + URLEncoder.encode(descripcion, "UTF-8") +
                        "&precio=" + URLEncoder.encode(precio, "UTF-8") +
                        "&fecha=" + URLEncoder.encode(fechaFormateada, "UTF-8") +
                        "&hora=" + URLEncoder.encode(horaSeleccionada, "UTF-8");

                // Enviar los datos al servidor
                try (OutputStream os = conn.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                    writer.flush();
                }

                // Comprobar la respuesta del servidor
                int responseCode = conn.getResponseCode();
                Log.d("Response Code", "Código de respuesta: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Mostrar mensaje de éxito
                    runOnUiThread(() -> Toast.makeText(detalle_prueba_laboratorio.this, "Datos enviados exitosamente", Toast.LENGTH_LONG).show());
                } else {
                    // Mostrar mensaje de error si la respuesta del servidor no es exitosa
                    runOnUiThread(() -> Toast.makeText(detalle_prueba_laboratorio.this, "Error al enviar los datos: Código " + responseCode, Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Mostrar mensaje de error si ocurre una excepción
                runOnUiThread(() -> Toast.makeText(detalle_prueba_laboratorio.this, "Error al enviar los datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) {
                    conn.disconnect(); // Asegurarse de cerrar la conexión
                }
            }
        }).start();
    }
}


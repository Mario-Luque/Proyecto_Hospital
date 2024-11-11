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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.ImageRequest;

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

public class detalle_medicos extends AppCompatActivity {

    // Elementos de la interfaz de usuario
    TextView detalleNom, detalleApellido, detallePrecio, detalleArea, detalleDes;
    ImageView detalleImagen;
    EditText fechaHoraInput;
    Button btnAdquirir;

    private RequestQueue rq;
    private String dni; // Variable para almacenar el DNI
    private String fechaSeleccionada;
    private String horaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalle_medicos);

        // Inicializar las vistas
        detalleNom = findViewById(R.id.detalleNom);
        detalleApellido = findViewById(R.id.detalleApellido);
        detallePrecio = findViewById(R.id.detallePrecio);
        detalleArea = findViewById(R.id.detalleArea);
        detalleDes = findViewById(R.id.detalleDes);
        detalleImagen = findViewById(R.id.detalleImage);
        fechaHoraInput = findViewById(R.id.fechaHoraInput);
        btnAdquirir = findViewById(R.id.btn_adquirir);

        // Inicializar la cola de peticiones Volley
        rq = Volley.newRequestQueue(this);

        // Obtener el DNI desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        dni = prefs.getString("dni", null); // Recuperar el DNI

        Intent intent = getIntent();
        int position = intent.getExtras().getInt("position");

        // Cargar los datos del médico
        cargarDatosMedico(position);

        // Configurar el listener para el EditText
        fechaHoraInput.setOnClickListener(v -> mostrarDatePicker());

        // Configurar el botón de adquirir
        btnAdquirir.setOnClickListener(v -> {
            if (validarCampos() && validarFechaHora()) {
                enviarDatos();
            }
        });
    }

    // Método para cargar los datos del médico
    private void cargarDatosMedico(int position) {
        detalleNom.setText(listar_medicos.listaUsuarios.get(position).getNombres());
        detalleApellido.setText(listar_medicos.listaUsuarios.get(position).getApellidos());
        detallePrecio.setText(String.valueOf(listar_medicos.listaUsuarios.get(position).getPrecio()));
        detalleArea.setText(listar_medicos.listaUsuarios.get(position).getArea());
        detalleDes.setText(listar_medicos.listaUsuarios.get(position).getDescripcion());
        cargarImagenUrl(listar_medicos.listaUsuarios.get(position).getUrlimagen());
    }

    // Método para cargar la imagen desde una URL
    private void cargarImagenUrl(String url) {
        ImageRequest request = new ImageRequest(url,
                bitmap -> detalleImagen.setImageBitmap(bitmap),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                volleyError -> Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show());
        rq.add(request);
    }

    // Método para mostrar el DatePicker
    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    fechaSeleccionada = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    mostrarTimePicker();
                }, year, month, day);
        datePickerDialog.show();
    }

    // Método para mostrar el TimePicker
    private void mostrarTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    horaSeleccionada = String.format("%02d:%02d:%02d", selectedHour, selectedMinute, 0);
                    fechaHoraInput.setText(fechaSeleccionada + " " + horaSeleccionada);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    // Validar los campos antes de enviar los datos
    private boolean validarCampos() {
        if (fechaHoraInput.getText().toString().isEmpty()) {
            fechaHoraInput.setError("Debe seleccionar fecha y hora");
            return false;
        }
        return true;
    }

    // Validar si la fecha y hora seleccionadas son posteriores a la actual
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

    // Enviar los datos al servidor
    private void enviarDatos() {
        String nombre = detalleNom.getText().toString();
        String apellido = detalleApellido.getText().toString();
        String precio = detallePrecio.getText().toString();
        String area = detalleArea.getText().toString();
        String tipo = "Consulta Médica"; // Tipo de consulta
        String descripcion = nombre + " " + apellido + " - " + area;

        // Convertir la fecha al formato YYYY-MM-DD
        String[] partesFecha = fechaSeleccionada.split("/");
        String fechaFormateada = partesFecha[2] + "-" + partesFecha[1] + "-" + partesFecha[0];

        // Llamada de log para depuración
        Log.d("Datos Enviados", "DNI: " + dni + ", Tipo: " + tipo + ", Descripción: " + descripcion +
                ", Precio: " + precio + ", Fecha: " + fechaFormateada + ", Hora: " + horaSeleccionada);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                // Conectar al servidor
                URL url = new URL("http://192.168.0.9:80/crud/registrar_historial.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Preparar los datos para enviar
                String postData = "dni=" + URLEncoder.encode(dni, "UTF-8") +
                        "&tipo=" + URLEncoder.encode(tipo, "UTF-8") +
                        "&descripcion=" + URLEncoder.encode(descripcion, "UTF-8") +
                        "&precio=" + URLEncoder.encode(precio, "UTF-8") +
                        "&fecha=" + URLEncoder.encode(fechaFormateada, "UTF-8") +
                        "&hora=" + URLEncoder.encode(horaSeleccionada, "UTF-8");

                // Enviar los datos
                try (OutputStream os = conn.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                    writer.flush();
                }

                // Comprobar el código de respuesta del servidor
                int responseCode = conn.getResponseCode();
                Log.d("Response Code", "Código de respuesta: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(detalle_medicos.this, "Datos enviados exitosamente", Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(detalle_medicos.this, "Error al enviar los datos: Código " + responseCode, Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(detalle_medicos.this, "Error al enviar los datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) {
                    conn.disconnect(); // Asegúrate de desconectar
                }
            }
        }).start();
    }
}

package com.example.proyecto_hospital;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class detalle_medicina extends AppCompatActivity {

    // Definición de los elementos de la interfaz de usuario
    private TextView detalleNom, detallePre, detalleDes, precioTotal;
    private ImageView detalleImagen;
    private EditText fechaHoraInput, cantidadInput;
    private Button btnAdquirir;

    private RequestQueue rq;
    private int position;
    private double precio;

    private String fechaSeleccionada;
    private String horaSeleccionada;
    private String dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_medicina);

        // Inicializar las vistas
        detalleNom = findViewById(R.id.detalleNom);
        detallePre = findViewById(R.id.detallePrecio);
        detalleDes = findViewById(R.id.detalleDes);
        detalleImagen = findViewById(R.id.detalleImage);
        fechaHoraInput = findViewById(R.id.fechaHoraInput);
        cantidadInput = findViewById(R.id.detalleCantidad);
        precioTotal = findViewById(R.id.detallePrecioTotal);
        btnAdquirir = findViewById(R.id.btn_adquirir);

        // Inicializar la cola de peticiones Volley
        rq = Volley.newRequestQueue(this);

        // Obtener el DNI del usuario desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        dni = prefs.getString("dni", null);

        // Obtener la posición de la medicina seleccionada desde el Intent
        Intent intent = getIntent();
        position = intent.getExtras().getInt("position");

        // Cargar los datos de la medicina seleccionada
        medicina medicina = listar_medicina.listaMedicinas.get(position);
        detalleNom.setText(medicina.getNombre());
        precio = medicina.getPrecio();
        detallePre.setText(String.valueOf(precio));
        detalleDes.setText(medicina.getDescripcion());
        cargarImagenUrl(medicina.getUrlimagen());

        // Establecer un listener para calcular el precio total cuando se modifique la cantidad
        cantidadInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularPrecioTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Mostrar el DatePicker al hacer clic en el campo de fecha y hora
        fechaHoraInput.setOnClickListener(v -> mostrarDatePicker());

        // Enviar los datos al servidor cuando el usuario haga clic en "Adquirir"
        btnAdquirir.setOnClickListener(v -> {
            if (validarCampos() && validarFechaHora()) {
                enviarDatos();
            }
        });
    }

    // Método para cargar la imagen desde una URL utilizando Volley
    private void cargarImagenUrl(String url) {
        ImageRequest request = new ImageRequest(url,
                bitmap -> detalleImagen.setImageBitmap(bitmap),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                volleyError -> { /* Manejar errores de carga de imagen */ });
        rq.add(request);
    }

    // Mostrar el selector de fecha (DatePickerDialog)
    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Guardar la fecha seleccionada y mostrar el TimePicker
                    fechaSeleccionada = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    mostrarTimePicker();
                }, year, month, day);
        datePickerDialog.show();
    }

    // Mostrar el selector de hora (TimePickerDialog)
    private void mostrarTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    // Guardar la hora seleccionada
                    horaSeleccionada = String.format("%02d:%02d:%02d", selectedHour, selectedMinute, 0);
                    // Establecer la fecha y hora seleccionada en el campo correspondiente
                    fechaHoraInput.setText(fechaSeleccionada + " " + horaSeleccionada);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    // Calcular el precio total según la cantidad ingresada
    private void calcularPrecioTotal() {
        String cantidadStr = cantidadInput.getText().toString();
        if (!cantidadStr.isEmpty()) {
            int cantidad = Integer.parseInt(cantidadStr);
            double total = cantidad * precio;
            precioTotal.setText("Precio Total: " + total);
        } else {
            precioTotal.setText("Precio Total: 0.00");
        }
    }

    // Validar los campos antes de enviar los datos
    private boolean validarCampos() {
        String cantidadStr = cantidadInput.getText().toString();
        if (cantidadStr.isEmpty() || Integer.parseInt(cantidadStr) <= 0) {
            Toast.makeText(this, "Por favor, seleccione una cantidad válida (mayor que 0)", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Verificar que la fecha y hora no estén vacíos
        if (fechaHoraInput.getText().toString().isEmpty()) {
            fechaHoraInput.setError("Debe seleccionar una fecha y hora");
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

    // Enviar los datos al servidor mediante HTTP POST
    private void enviarDatos() {
        // Si pasa la validación de fecha y hora, se procede a obtener los valores para enviar
        String nombre = detalleNom.getText().toString();
        String tipo = "Medicina";
        String descripcion = nombre + " - Cantidad: " + cantidadInput.getText().toString();
        String precioTotalStr = precioTotal.getText().toString().replace("Precio Total: ", "");

        // Convertir la fecha seleccionada al formato YYYY-MM-DD
        String[] partesFecha = fechaSeleccionada.split("/");
        String fechaFormateada = partesFecha[2] + "-" + partesFecha[1] + "-" + partesFecha[0];

        Log.d("Datos Enviados", "DNI: " + dni + ", Tipo: " + tipo + ", Descripción: " + descripcion +
                ", Precio Total: " + precioTotalStr + ", Fecha: " + fechaFormateada + ", Hora: " + horaSeleccionada);

        // Enviar los datos a través de una nueva hebra para evitar bloquear la interfaz de usuario
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
                        "&precio=" + URLEncoder.encode(precioTotalStr, "UTF-8") +
                        "&fecha=" + URLEncoder.encode(fechaFormateada, "UTF-8") +
                        "&hora=" + URLEncoder.encode(horaSeleccionada, "UTF-8");

                // Enviar los datos a través del flujo de salida
                try (OutputStream os = conn.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                    writer.flush();
                }

                // Comprobar la respuesta del servidor
                int responseCode = conn.getResponseCode();
                Log.d("Response Code", "Código de respuesta: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(detalle_medicina.this, "Datos enviados exitosamente", Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(detalle_medicina.this, "Error al enviar los datos: Código " + responseCode, Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(detalle_medicina.this, "Error al enviar los datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
}
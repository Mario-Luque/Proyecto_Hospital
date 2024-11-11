package com.example.proyecto_hospital;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyecto_hospital.databinding.ActivityListarHistorialBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class listar_historial extends AppCompatActivity {

    // Enlace de vista usando ViewBinding
    ActivityListarHistorialBinding binding;

    // Lista para almacenar los objetos del historial
    public static ArrayList<historial> listaUsuarios;

    // Cola de solicitudes de Volley
    private RequestQueue rq;

    // Adaptador para el RecyclerView
    private AdaptadorHistorial adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el layout y usar ViewBinding para optimizar la referencia de vistas
        setContentView(R.layout.activity_listar_historial);
        binding = ActivityListarHistorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar el RecyclerView y establecer su layout
        RecyclerView recyclerView = findViewById(R.id.lst1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar la lista de usuarios
        listaUsuarios = new ArrayList<>();

        // Inicializar la cola de solicitudes Volley
        rq = Volley.newRequestQueue(this);

        // Inicializar el adaptador y asignarlo al RecyclerView
        adapter = new AdaptadorHistorial();
        recyclerView.setAdapter(adapter);

        // Cargar los datos del historial del servidor
        cargarPersona();
    }

    // Método para cargar los datos del historial desde el servidor
    private void cargarPersona() {
        // Obtener el DNI almacenado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String dni = prefs.getString("dni", null);


        String url = "http://192.168.0.9:80/crud/mostrar_historial.php?dni=" + dni;

        // Crear la solicitud GET usando Volley
        JsonObjectRequest requerimiento = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener el arreglo "datos" del JSON
                            JSONArray jsonArray = response.getJSONArray("datos");

                            // Limpiar la lista antes de agregar nuevos datos
                            listaUsuarios.clear();

                            // Recorrer el arreglo JSON y agregar cada objeto a la lista
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject objeto = jsonArray.getJSONObject(i);
                                String id = objeto.getString("id");
                                String tipo = objeto.getString("tipo");
                                String descripcion = objeto.getString("descripcion");
                                double precio = objeto.getDouble("precio");
                                String fecha = objeto.getString("fecha");
                                String hora = objeto.getString("hora");

                                // Crear un objeto historial y agregarlo a la lista
                                historial usuario = new historial(id, dni, tipo, descripcion, precio, fecha, hora);
                                listaUsuarios.add(usuario);
                            }

                            // Notificar al adaptador que los datos han cambiado
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejo de errores de la solicitud
                        error.printStackTrace();
                    }
                });

        // Añadir la solicitud al RequestQueue de Volley
        rq.add(requerimiento);
    }

    // Adaptador personalizado para el RecyclerView
    private class AdaptadorHistorial extends RecyclerView.Adapter<AdaptadorHistorial.AdaptadorHistorialHolder> {

        // Método para crear un nuevo ViewHolder
        @NonNull
        @Override
        public AdaptadorHistorialHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflar el layout del ítem del RecyclerView
            return new AdaptadorHistorialHolder(getLayoutInflater().inflate(R.layout.list_item_historial, parent, false));
        }

        // Método para vincular los datos a cada ítem del RecyclerView
        @Override
        public void onBindViewHolder(@NonNull AdaptadorHistorialHolder holder, int position) {
            // Llamar al método para mostrar los datos en el holder
            holder.imprimir(position);

            // Configurar el click del cardView, si es necesario
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Manejo de eventos de clic, si se necesita
                }
            });
        }

        // Método para obtener el número total de ítems en la lista
        @Override
        public int getItemCount() {
            return listaUsuarios.size();
        }

        // ViewHolder que contiene las vistas de cada ítem
        class AdaptadorHistorialHolder extends RecyclerView.ViewHolder {
            // Vistas para mostrar los datos
            TextView tipo, descripcion, precio, fecha, hora;
            Button btnEliminar; // Botón para eliminar
            public CardView cardView; // Contenedor del ítem

            public AdaptadorHistorialHolder(@NonNull View itemView) {
                super(itemView);

                // Inicializar las vistas
                tipo = itemView.findViewById(R.id.tipo);
                descripcion = itemView.findViewById(R.id.descripcion);
                precio = itemView.findViewById(R.id.precio);
                fecha = itemView.findViewById(R.id.fecha);
                hora = itemView.findViewById(R.id.hora);
                btnEliminar = itemView.findViewById(R.id.btnEliminar); // Inicializar botón eliminar
                cardView = itemView.findViewById(R.id.main_container);
            }

            // Método para mostrar los datos de un ítem en las vistas
            public void imprimir(int position) {
                historial item = listaUsuarios.get(position);

                // Asignar los datos del historial a las vistas
                tipo.setText(item.tipo);
                descripcion.setText(item.descripcion);
                precio.setText("Precio: " + item.precio);
                fecha.setText("Fecha: " + item.fecha);
                hora.setText("Hora: " + item.hora);

                // Verificar si la fecha es futura para mostrar el botón eliminar
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date fechaItem = sdf.parse(item.fecha);
                    Date fechaActual = new Date(); // Fecha actual

                    // Mostrar el botón eliminar si la fecha es posterior a la actual
                    if (fechaItem != null && fechaItem.after(fechaActual)) {
                        btnEliminar.setVisibility(View.VISIBLE);
                    } else {
                        btnEliminar.setVisibility(View.GONE); // Ocultar si no es posterior
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    btnEliminar.setVisibility(View.GONE); // Ocultar en caso de error
                }

                // Configurar el evento del botón eliminar
                btnEliminar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Obtener el ID del historial a eliminar
                        String idToDelete = item.id;

                        // URL para eliminar el historial desde el servidor
                        String url = "http://192.168.0.9:80/crud/eliminar_historial.php";

                        // Crear una solicitud POST para eliminar el historial
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            // Verificar si la eliminación fue exitosa
                                            JSONObject jsonResponse = new JSONObject(response);
                                            if (jsonResponse.getString("exito").equals("1")) {
                                                // Eliminar el ítem de la lista local
                                                listaUsuarios.remove(position);
                                                notifyItemRemoved(position); // Notificar al adaptador
                                            } else {
                                                // Mostrar un mensaje de error si la eliminación falló
                                                Toast.makeText(listar_historial.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // Manejar errores de la solicitud
                                        error.printStackTrace();
                                        Toast.makeText(listar_historial.this, "Error en la solicitud", Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                // Pasar el ID del historial a eliminar
                                Map<String, String> params = new HashMap<>();
                                params.put("id", idToDelete);
                                return params;
                            }
                        };

                        // Añadir la solicitud al RequestQueue
                        rq.add(stringRequest);
                    }
                });
            }
        }
    }
}
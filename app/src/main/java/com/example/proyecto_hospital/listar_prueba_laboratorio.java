package com.example.proyecto_hospital;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyecto_hospital.databinding.ActivityListarPruebaLaboratorioBinding;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class listar_prueba_laboratorio extends AppCompatActivity {

    // ViewBinding para la actividad
    ActivityListarPruebaLaboratorioBinding binding;

    // Lista de pruebas de laboratorio que se mostrarán en el RecyclerView
    public static ArrayList<prueba_laboratorio> listaUsuarios;

    // Cola de solicitudes Volley
    private RequestQueue rq;

    // RecyclerView para mostrar las pruebas de laboratorio
    private RecyclerView lst1;

    // Adaptador para el RecyclerView
    private AdaptadorPruebasLaboratorio adaptadorPruebasLaboratorio;

    // Variables para almacenar los datos de las pruebas de laboratorio
    String id, nombre, descripcion, foto;
    double precio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el layout de la actividad
        setContentView(R.layout.activity_listar_prueba_laboratorio);
        binding = ActivityListarPruebaLaboratorioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar RecyclerView
        lst1 = findViewById(R.id.lst1);

        // Inicializar la lista de pruebas de laboratorio
        listaUsuarios = new ArrayList<>();

        // Inicializar la cola de solicitudes Volley
        rq = Volley.newRequestQueue(this);

        // Cargar las pruebas de laboratorio desde el servidor
        cargarPersona();

        // Configurar LayoutManager para RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lst1.setLayoutManager(linearLayoutManager);

        // Inicializar adaptador y asignarlo al RecyclerView
        adaptadorPruebasLaboratorio = new AdaptadorPruebasLaboratorio();
        lst1.setAdapter(adaptadorPruebasLaboratorio);
    }

    // Método para cargar las pruebas de laboratorio desde el servidor
    private void cargarPersona() {

        String url = "http://192.168.0.9:80/crud/mostrar_prueba_laboratorio.php";

        // Solicitud GET usando Volley
        JsonObjectRequest requerimiento = new JsonObjectRequest(Request.Method.GET,
                url,
                null,  // No enviamos parámetros
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Obtener el arreglo "datos" de la respuesta
                            String valor = response.get("datos").toString();
                            JSONArray arreglo = new JSONArray(valor);
                            JSONArray jsonArray = response.getJSONArray("datos");

                            // Recorrer el arreglo y agregar las pruebas a la lista
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject objeto = new JSONObject(arreglo.get(i).toString());

                                // Extraer los datos de cada prueba
                                id = objeto.getString("id");
                                precio = Double.parseDouble(objeto.getString("precio"));
                                nombre = objeto.getString("nombre");
                                foto = objeto.getString("urlimagen");
                                descripcion = objeto.getString("descripcion");

                                // Crear objeto prueba_laboratorio y agregarlo a la lista
                                prueba_laboratorio usuario = new prueba_laboratorio(id, nombre, precio, descripcion, foto);
                                listaUsuarios.add(usuario);

                                // Notificar al adaptador que se ha insertado un nuevo ítem
                                adaptadorPruebasLaboratorio.notifyItemRangeInserted(listaUsuarios.size(), i + 1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace(); // Manejo de error al procesar el JSON
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejo de errores de red
                    }
                });

        // Agregar la solicitud a la cola de Volley
        rq.add(requerimiento);
    }

    // Adaptador para el RecyclerView que muestra las pruebas de laboratorio
    private class AdaptadorPruebasLaboratorio extends RecyclerView.Adapter<AdaptadorPruebasLaboratorio.AdaptadorPruebasLaboratorioHolder> {

        @NonNull
        @Override
        public AdaptadorPruebasLaboratorioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflar el layout de cada ítem
            return new AdaptadorPruebasLaboratorioHolder(getLayoutInflater().inflate(R.layout.list_item_prueba_laboratorio, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AdaptadorPruebasLaboratorioHolder holder, int position) {
            // Llamar al método para mostrar los datos del ítem
            holder.imprimir(position);

            // Configurar listener para el clic en el ítem
            holder.cardView.setOnClickListener(v -> {
                // Iniciar la actividad de detalles pasando la posición del ítem
                startActivity(new Intent(getApplicationContext(), detalle_prueba_laboratorio.class).putExtra("position", position));
            });
        }

        @Override
        public int getItemCount() {
            // Retornar el número de pruebas en la lista
            return listaUsuarios.size();
        }

        // ViewHolder que representa cada ítem del RecyclerView
        class AdaptadorPruebasLaboratorioHolder extends RecyclerView.ViewHolder {
            TextView tvNombre;
            ImageView ivFoto;
            CardView cardView;

            // Constructor que inicializa las vistas del ítem
            public AdaptadorPruebasLaboratorioHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.nombre);
                ivFoto = itemView.findViewById(R.id.listImage);
                cardView = itemView.findViewById(R.id.main_container);
            }

            // Método para mostrar los datos en el ViewHolder
            public void imprimir(int position) {
                // Obtener la prueba de laboratorio correspondiente
                prueba_laboratorio prueba = listaUsuarios.get(position);

                // Establecer el nombre de la prueba
                tvNombre.setText(prueba.getNombre());

                // Cargar la imagen desde la URL
                recuperarImagen(prueba.getUrlimagen(), ivFoto);
            }

            // Método para recuperar la imagen desde la URL
            public void recuperarImagen(String foto, ImageView iv) {
                // Crear la solicitud de imagen usando Volley
                ImageRequest peticion = new ImageRequest(foto,
                        response -> iv.setImageBitmap(response), // Establecer la imagen si la solicitud es exitosa
                        0, 0, null, null,
                        error -> {
                            // Manejo de errores si la imagen no puede ser cargada
                        });
                // Agregar la solicitud de la imagen a la cola de Volley
                rq.add(peticion);
            }
        }
    }
}

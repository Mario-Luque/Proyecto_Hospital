package com.example.proyecto_hospital;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.example.proyecto_hospital.databinding.ActivityListarMedicosBinding;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class listar_medicos extends AppCompatActivity {

    // ViewBinding para la actividad
    ActivityListarMedicosBinding binding;

    // Lista para almacenar los objetos de médicos
    public static ArrayList<medicos> listaUsuarios;

    // Cola de solicitudes Volley
    private RequestQueue rq;

    // RecyclerView para mostrar los médicos
    private RecyclerView lst1;

    // Adaptador para el RecyclerView
    private AdaptadorMedicos adaptadorMedicos;

    // Variables para almacenar temporalmente los datos de cada médico
    String id, nombres, apellidos, area, descripcion, foto;
    double precio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el layout de la actividad
        setContentView(R.layout.activity_listar_medicos);
        binding = ActivityListarMedicosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar RecyclerView
        lst1 = findViewById(R.id.lst1);

        // Inicializar la lista de médicos
        listaUsuarios = new ArrayList<>();

        // Inicializar la cola de solicitudes Volley
        rq = Volley.newRequestQueue(this);

        // Obtener el área seleccionada desde el Intent
        String areaSeleccionada = getIntent().getStringExtra("area");

        // Cargar los médicos filtrados por área
        cargarMedicos(areaSeleccionada);

        // Configurar el LayoutManager para RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lst1.setLayoutManager(linearLayoutManager);

        // Inicializar el adaptador y asignarlo al RecyclerView
        adaptadorMedicos = new AdaptadorMedicos();
        lst1.setAdapter(adaptadorMedicos);
    }

    // Método para cargar los médicos desde el servidor
    private void cargarMedicos(String areaFiltro) {
        // URL del servidor donde están los datos
        String url = "http://192.168.0.9:80/crud/mostrar_medicos.php";

        // Solicitud GET para obtener los datos de los médicos
        JsonObjectRequest requerimiento = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Obtener el arreglo "datos" del JSON
                        JSONArray jsonArray = response.getJSONArray("datos");

                        // Limpiar la lista antes de agregar nuevos médicos
                        listaUsuarios.clear();

                        // Recorrer los médicos y agregarlos a la lista
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject objeto = jsonArray.getJSONObject(i);
                            String area = objeto.getString("area");

                            // Filtrar médicos por el área seleccionada
                            if (areaFiltro == null || area.equals(areaFiltro)) {
                                // Extraer los datos del médico
                                id = objeto.getString("id");
                                nombres = objeto.getString("nombres");
                                apellidos = objeto.getString("apellidos");
                                precio = objeto.getDouble("precio");
                                descripcion = objeto.getString("descripcion");
                                foto = objeto.getString("urlimagen");

                                // Crear un objeto "medico" y agregarlo a la lista
                                medicos usuario = new medicos(id, nombres, apellidos, precio, area, descripcion, foto);
                                listaUsuarios.add(usuario);
                            }
                        }

                        // Notificar al adaptador que los datos han cambiado
                        adaptadorMedicos.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace(); // Manejo de error si la respuesta es inválida
                    }
                },
                error -> {
                    // Manejo de errores de red o solicitud
                    error.printStackTrace();
                });

        // Agregar la solicitud a la cola de Volley
        rq.add(requerimiento);
    }

    // Adaptador para el RecyclerView de médicos
    private class AdaptadorMedicos extends RecyclerView.Adapter<AdaptadorMedicos.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflar el layout de cada ítem del RecyclerView
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_medicos, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Asignar los datos del médico al ViewHolder
            holder.imprimir(position);

            // Configurar el clic en el ítem del RecyclerView
            holder.cardView.setOnClickListener(v -> {
                // Crear un Intent para ver los detalles del médico
                Intent intent = new Intent(holder.itemView.getContext(), detalle_medicos.class);
                intent.putExtra("position", position); // Enviar la posición del ítem
                holder.itemView.getContext().startActivity(intent); // Iniciar la actividad de detalles
            });
        }

        @Override
        public int getItemCount() {
            // Retornar el tamaño de la lista de médicos
            return listaUsuarios.size();
        }

        // ViewHolder que representa cada ítem del RecyclerView
        class ViewHolder extends RecyclerView.ViewHolder {
            // Declaración de vistas para el ítem
            TextView nombre;
            ShapeableImageView listImage;
            CardView cardView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Inicializar las vistas del ítem
                nombre = itemView.findViewById(R.id.nombre);
                listImage = itemView.findViewById(R.id.listImage);
                cardView = itemView.findViewById(R.id.main_container);
            }

            // Método para asignar los datos del médico al ViewHolder
            public void imprimir(int position) {
                // Obtener el médico de la lista en la posición indicada
                medicos medico = listaUsuarios.get(position);

                // Establecer el nombre del médico
                nombre.setText(medico.getNombres() + " " + medico.getApellidos());

                // Cargar la imagen del médico desde la URL
                recuperarImagen(medico.getUrlimagen(), listImage);
            }

            // Método para cargar la imagen del médico
            public void recuperarImagen(String foto, ImageView iv) {
                // Crear la solicitud para obtener la imagen
                ImageRequest peticion = new ImageRequest(foto,
                        response -> iv.setImageBitmap(response), // Asignar la imagen al ImageView
                        0, 0, null, null,
                        error -> {
                            // Manejo de errores al cargar la imagen
                        });
                rq.add(peticion); // Agregar la solicitud de imagen a la cola de Volley
            }
        }
    }
}

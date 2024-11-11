package com.example.proyecto_hospital;

import android.content.Intent;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class listar_medicina extends AppCompatActivity {

    // Declaración de la vista RecyclerView que mostrará la lista de medicinas
    private RecyclerView lst1;

    // Lista estática para almacenar los objetos de tipo medicina
    public static ArrayList<medicina> listaMedicinas;

    // Cola de solicitudes de Volley para manejar las peticiones a la API
    private RequestQueue rq;

    // Adaptador para el RecyclerView que se encargará de mostrar las medicinas
    private AdaptadorMedicina adaptadorMedicina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Establecer el layout de la actividad
        setContentView(R.layout.activity_listar_medicina);

        // Inicialización del RecyclerView
        lst1 = findViewById(R.id.lst1);

        // Inicialización de la lista que contiene las medicinas
        listaMedicinas = new ArrayList<>();

        // Inicialización de la cola de solicitudes de Volley
        rq = Volley.newRequestQueue(this);

        // Llamada al método para cargar las medicinas desde el servidor
        cargarMedicinas();

        // Configuración del RecyclerView con un LayoutManager de tipo lineal
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lst1.setLayoutManager(linearLayoutManager);

        // Inicialización del adaptador del RecyclerView y asignación al RecyclerView
        adaptadorMedicina = new AdaptadorMedicina();
        lst1.setAdapter(adaptadorMedicina);
    }

    // Método para cargar la lista de medicinas desde el servidor
    private void cargarMedicinas() {
        // URL del servidor para obtener las medicinas
        String url = "http://192.168.0.9:80/crud/mostrar_medicina.php";

        // Creación de la solicitud GET usando Volley para obtener los datos en formato JSON
        JsonObjectRequest requerimiento = new JsonObjectRequest(Request.Method.GET,
                url,
                null, // No enviamos parámetros en la solicitud
                response -> {
                    try {
                        // Obtener el arreglo "datos" del JSON
                        JSONArray jsonArray = response.getJSONArray("datos");

                        // Recorrer el arreglo de medicinas y agregar cada medicina a la lista
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject objeto = jsonArray.getJSONObject(i);

                            // Extraer los datos de cada medicina del JSON
                            String id = objeto.getString("id");
                            double precio = objeto.getDouble("precio");
                            String nombre = objeto.getString("nombre");
                            String descripcion = objeto.getString("descripcion");
                            String urlImagen = objeto.getString("urlimagen");

                            // Crear un objeto medicina y agregarlo a la lista
                            medicina medicina = new medicina(id, nombre, precio, descripcion, urlImagen);
                            listaMedicinas.add(medicina);

                            // Notificar al adaptador que se ha insertado un nuevo ítem
                            adaptadorMedicina.notifyItemInserted(listaMedicinas.size() - 1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace(); // Manejar error en la conversión del JSON
                    }
                },
                error -> {
                    // Manejar error de respuesta (por ejemplo, error en la red)
                });
        rq.add(requerimiento); // Agregar la solicitud a la cola de Volley
    }

    // Adaptador personalizado para el RecyclerView
    private class AdaptadorMedicina extends RecyclerView.Adapter<AdaptadorMedicina.AdaptadorMedicinaHolder> {

        @NonNull
        @Override
        public AdaptadorMedicinaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflar el layout de cada ítem del RecyclerView
            return new AdaptadorMedicinaHolder(getLayoutInflater().inflate(R.layout.list_item_medicina, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AdaptadorMedicinaHolder holder, int position) {
            // Asignar los datos del ítem al holder (vista del ítem)
            holder.imprimir(position);

            // Establecer un listener para el clic en el ítem del RecyclerView
            holder.itemView.setOnClickListener(v -> {
                // Crear un intent para abrir una nueva actividad con los detalles de la medicina seleccionada
                Intent intent = new Intent(getApplicationContext(), detalle_medicina.class);
                intent.putExtra("position", position); // Enviar la posición del ítem como extra
                startActivity(intent); // Iniciar la actividad de detalles
            });
        }

        @Override
        public int getItemCount() {
            // Devolver el número total de medicinas en la lista
            return listaMedicinas.size();
        }

        // ViewHolder para representar cada ítem de medicina en el RecyclerView
        class AdaptadorMedicinaHolder extends RecyclerView.ViewHolder {
            // Declaración de las vistas de cada ítem
            TextView tvNombre;
            ImageView ivFoto;
            CardView cardView;

            public AdaptadorMedicinaHolder(@NonNull View itemView) {
                super(itemView);
                // Inicialización de las vistas del ítem
                tvNombre = itemView.findViewById(R.id.nombre);
                ivFoto = itemView.findViewById(R.id.listImage);
                cardView = itemView.findViewById(R.id.main_container);
            }

            // Método para asignar los datos a las vistas de cada ítem
            public void imprimir(int position) {
                // Obtener la medicina de la lista en la posición dada
                medicina item = listaMedicinas.get(position);

                // Establecer el nombre de la medicina en el TextView
                tvNombre.setText(item.getNombre());

                // Cargar la imagen de la medicina usando su URL
                recuperarImagen(item.getUrlimagen(), ivFoto);
            }

            // Método para cargar una imagen a partir de una URL usando Volley
            public void recuperarImagen(String url, ImageView iv) {
                // Crear una solicitud para obtener la imagen
                ImageRequest peticion = new ImageRequest(url,
                        bitmap -> iv.setImageBitmap(bitmap), // En caso de éxito, establecer la imagen en el ImageView
                        0, 0, null, null,
                        error -> {
                            // Manejo de error al cargar la imagen (por ejemplo, mostrar una imagen por defecto)
                        });
                rq.add(peticion); // Agregar la solicitud de imagen a la cola de Volley
            }
        }
    }
}




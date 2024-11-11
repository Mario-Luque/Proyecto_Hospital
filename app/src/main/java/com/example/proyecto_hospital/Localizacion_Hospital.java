package com.example.proyecto_hospital;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class Localizacion_Hospital extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button showLocationButton;
    private Spinner addressSpinner;

    // Direcciones predeterminadas
    private static final String[] DIRECTIONS = {
            "Hospital Nacional Dos De Mayo",
            "Hospital Nacional Arzobispo Loayza",
            "Hospital Nacional Cayetano Heredia"
    };

    private static final double[][] LAT_LNG = {
            {-12.0567316, -77.0161498}, // Hospital Nacional Dos De Mayo
            {-12.0497444, -77.0427967}, // Hospital Nacional Arzobispo Loayza
            {-12.0224852, -77.0554528}  // Hospital Nacional Cayetano Heredia
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizacion_hospital);

        // Obtén el fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Encuentra el Spinner y el botón
        addressSpinner = findViewById(R.id.addressSpinner);
        showLocationButton = findViewById(R.id.showLocationButton);

        // Configura el Spinner con las direcciones predeterminadas
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DIRECTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addressSpinner.setAdapter(adapter);

        // Configura el botón para mostrar la ubicación
        showLocationButton.setOnClickListener(v -> {
            String selectedAddress = addressSpinner.getSelectedItem().toString();
            showLocationOnMap(selectedAddress);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void showLocationOnMap(String address) {
        int index = -1;

        // Busca la dirección seleccionada en el array y obtiene el índice
        for (int i = 0; i < DIRECTIONS.length; i++) {
            if (DIRECTIONS[i].equals(address)) {
                index = i;
                break;
            }
        }

        // Si se encontró la dirección seleccionada, muestra el marcador
        if (index != -1) {
            LatLng location = new LatLng(LAT_LNG[index][0], LAT_LNG[index][1]);
            mMap.clear();  // Limpiar el mapa antes de agregar un nuevo marcador
            mMap.addMarker(new MarkerOptions().position(location).title(address));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }
}

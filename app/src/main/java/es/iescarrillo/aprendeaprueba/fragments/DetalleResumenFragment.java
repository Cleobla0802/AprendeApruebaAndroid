package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Resumen;

public class DetalleResumenFragment extends Fragment {

    private EditText etTitulo, etContenido;
    private MaterialButton btnGuardarCambios, btnVolver;
    private Resumen resumen;
    private DatabaseReference mDatabase;

    private Spinner spinnerCategoria;

    public DetalleResumenFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalles_resumen, container, false);

        // 1. Inicializar Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("resumenes");

        // 2. Inicializar Vistas (Asegúrate de que estos IDs coincidan con tu XML)
        etTitulo = view.findViewById(R.id.etTituloDetalle);
        etContenido = view.findViewById(R.id.etContenidoDetalle);
        btnGuardarCambios = view.findViewById(R.id.btnGuardarCambios);
        btnVolver = view.findViewById(R.id.btnVolverDetalle);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoriaDetalle);

        String[] categorias = {"Matemáticas", "Tecnologia", "Historia", "Ciencias", "Inglés"};

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_blanco, categorias);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterSpinner);

        // 3. Recuperar el objeto Resumen enviado desde el Adapter/Fragment anterior
        if (getArguments() != null) {
            resumen = (Resumen) getArguments().getSerializable("resumen_objeto");

            if (resumen != null) {
                // Rellenar los campos con los datos actuales
                etTitulo.setText(resumen.getTitulo());
                etContenido.setText(resumen.getContenido());
            }
        }

        // 4. Configurar botones
        btnGuardarCambios.setOnClickListener(v -> actualizarResumen());

        btnVolver.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void actualizarResumen() {
        String nuevoTitulo = etTitulo.getText().toString().trim();
        String nuevoContenido = etContenido.getText().toString().trim();

        if (nuevoTitulo.isEmpty() || nuevoContenido.isEmpty()) {
            Toast.makeText(getContext(), "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un mapa con los campos a actualizar
        Map<String, Object> actualizaciones = new HashMap<>();
        actualizaciones.put("titulo", nuevoTitulo);
        actualizaciones.put("contenido", nuevoContenido);
        actualizaciones.put("categoria", spinnerCategoria.getSelectedItem().toString());

        // Actualizar en Firebase usando el ID del resumen
        if (resumen != null && resumen.getId() != null) {
            mDatabase.child(resumen.getId()).updateChildren(actualizaciones)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "¡Cambios guardados!", Toast.LENGTH_SHORT).show();
                        // Volver a la lista después de guardar
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
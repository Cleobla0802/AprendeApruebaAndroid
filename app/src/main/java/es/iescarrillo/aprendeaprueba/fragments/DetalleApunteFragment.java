package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import es.iescarrillo.aprendeaprueba.R;

public class DetalleApunteFragment extends Fragment {

    private TextInputEditText etTitulo, etContenido;
    private AutoCompleteTextView spinnerCategoria;
    private ExtendedFloatingActionButton fabGuardar;
    private ProgressBar pbCargando;
    private String apunteId; // Necesario para actualizar en Firebase

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_apunte, container, false);

        // Inicializar vistas
        etTitulo = view.findViewById(R.id.etTitulo);
        etContenido = view.findViewById(R.id.etContenido);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        fabGuardar = view.findViewById(R.id.fabGuardar);
        pbCargando = view.findViewById(R.id.pbCargando);

        // Configurar el spinner de categorías
        String[] categorias = {"Matemáticas", "Historia", "Ciencias"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, categorias);
        binding.spinnerCategoria.setAdapter(adapter);

        // Recuperar datos del Bundle
        if (getArguments() != null) {
            // Aquí asumo que pasas los strings por separado o el objeto
            apunteId = getArguments().getString("id");
            etTitulo.setText(getArguments().getString("titulo"));
            etContenido.setText(getArguments().getString("contenido"));
            spinnerCategoria.setText(getArguments().getString("categoria"), false);
        }

        fabGuardar.setOnClickListener(v -> actualizarApunte());

        return view;
    }

    private void actualizarApunte() {
        String nuevoTitulo = etTitulo.getText().toString().trim();
        String nuevoContenido = etContenido.getText().toString().trim();
        String nuevaCat = spinnerCategoria.getText().toString();

        if (nuevoTitulo.isEmpty() || nuevoContenido.isEmpty()) {
            Toast.makeText(getContext(), "No dejes campos vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        pbCargando.setVisibility(View.VISIBLE);
        fabGuardar.setEnabled(false);

        // Referencia a Firebase (Ajusta la ruta según tu DB)
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("apuntes").child(apunteId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("titulo", nuevoTitulo);
        updates.put("contenido", nuevoContenido);
        updates.put("categoria", nuevaCat);

        mDatabase.updateChildren(updates).addOnCompleteListener(task -> {
            pbCargando.setVisibility(View.GONE);
            fabGuardar.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Apunte actualizado", Toast.LENGTH_SHORT).show();
                // Opcional: Cerrar fragmento y volver atrás
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
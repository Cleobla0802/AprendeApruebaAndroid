package es.iescarrillo.aprendeaprueba.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import es.iescarrillo.aprendeaprueba.R;

public class DetalleApunteFragment extends Fragment {

    private TextInputEditText etTitulo, etDescripcion, etContenido;
    private AutoCompleteTextView spinnerCategoria;
    private ExtendedFloatingActionButton fabGuardar;
    private ProgressBar pbCargando;
    private String apunteId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_apunte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTitulo = view.findViewById(R.id.etTitulo);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        etContenido = view.findViewById(R.id.etContenido);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        fabGuardar = view.findViewById(R.id.fabGuardar);
        pbCargando = view.findViewById(R.id.pbCargando);

        // 3. Configurar el adaptador (usando un layout estándar para evitar fondos negros)
        String[] categorias = {"Matemáticas", "Historia", "Ciencias", "Ingles", "Tecnologia"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.item_dropdown_blanco, categorias) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE); // Forzamos blanco por código
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE); // Forzamos blanco en la lista desplegable
                text.setBackgroundColor(Color.parseColor("#2B2B2B")); // Fondo oscuro
                return view;
            }
        };

        spinnerCategoria.setAdapter(adapter);

        // 4. Recuperar datos del Bundle
        if (getArguments() != null) {
            apunteId = getArguments().getString("id");
            etTitulo.setText(getArguments().getString("titulo"));
            etDescripcion.setText(getArguments().getString("descripcion"));
            etContenido.setText(getArguments().getString("contenido"));
            // Importante el 'false' para que no filtre la lista al poner el texto inicial
            spinnerCategoria.setText(getArguments().getString("categoria"), false);
        }

        fabGuardar.setOnClickListener(v -> actualizarApunte());
    }

    private void actualizarApunte() {
        // Obtenemos los valores actuales de las vistas
        String nuevoTitulo = etTitulo.getText().toString().trim();
        String nuevaDescripcion = etDescripcion.getText().toString().trim();
        String nuevoContenido = etContenido.getText().toString().trim();
        String nuevaCat = spinnerCategoria.getText().toString();

        if (nuevoTitulo.isEmpty() || nuevoContenido.isEmpty() || nuevaCat.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        pbCargando.setVisibility(View.VISIBLE);
        fabGuardar.setEnabled(false);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("apuntes").child(apunteId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("titulo", nuevoTitulo);
        updates.put("descripcion", nuevaDescripcion);
        updates.put("contenido", nuevoContenido);
        updates.put("categoria", nuevaCat);

        mDatabase.updateChildren(updates).addOnCompleteListener(task -> {
            if (!isAdded()) return;

            pbCargando.setVisibility(View.GONE);
            fabGuardar.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Apunte actualizado", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Error en la actualización", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
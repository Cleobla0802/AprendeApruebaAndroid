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
import es.iescarrillo.aprendeaprueba.models.Resumen;

public class DetalleResumenFragment extends Fragment {

    private TextInputEditText etTitulo, etDescripcion, etContenido;
    private AutoCompleteTextView spinnerCategoria;
    private ExtendedFloatingActionButton fabGuardar;
    private ProgressBar pbCargando;
    private Resumen resumen;

    public DetalleResumenFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalles_resumen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTitulo = view.findViewById(R.id.etTituloDetalle);
        etDescripcion = view.findViewById(R.id.etDescripcionDetalle);
        etContenido = view.findViewById(R.id.etContenidoDetalle);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoriaDetalle);
        fabGuardar = view.findViewById(R.id.fabGuardarCambios);
        pbCargando = view.findViewById(R.id.pbCargando);

        String[] categorias = {"Matemáticas", "Historia", "Ciencias", "Ingles", "Tecnologia"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.item_dropdown_blanco, categorias) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v.findViewById(android.R.id.text1)).setTextColor(Color.WHITE);
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v.findViewById(android.R.id.text1)).setTextColor(Color.WHITE);
                v.setBackgroundColor(Color.parseColor("#2B2B2B"));
                return v;
            }
        };

        spinnerCategoria.setAdapter(adapter);

        if (getArguments() != null) {
            resumen = (Resumen) getArguments().getSerializable("resumen_objeto");
            if (resumen != null) {
                etTitulo.setText(resumen.getTitulo());
                etDescripcion.setText(resumen.getDescripcion());
                etContenido.setText(resumen.getResumenTexto());
                spinnerCategoria.setText(resumen.getCategoria(), false);
            }
        }

        fabGuardar.setOnClickListener(v -> actualizarResumen());
    }

    private void actualizarResumen() {
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

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("resumenes").child(resumen.getId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("titulo", nuevoTitulo);
        updates.put("descripcion", nuevaDescripcion);
        updates.put("resumenTexto", nuevoContenido);
        updates.put("categoria", nuevaCat);

        mDatabase.updateChildren(updates).addOnCompleteListener(task -> {
            if (!isAdded()) return;
            pbCargando.setVisibility(View.GONE);
            fabGuardar.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Resumen actualizado", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Error en la actualización", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

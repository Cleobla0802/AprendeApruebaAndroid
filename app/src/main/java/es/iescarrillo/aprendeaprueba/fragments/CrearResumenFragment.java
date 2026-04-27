package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.api.RetrofitClient;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearResumenFragment extends Fragment {

    private Spinner spinnerApuntes, spinnerCategoria;
    private EditText etTitulo, etDescripcion;
    private MaterialButton btnGenerar;

    // Listas para manejar los datos de los apuntes
    private List<Apuntes> listaApuntesObj = new ArrayList<>();
    private List<String> nombresApuntes = new ArrayList<>();

    private CircularProgressIndicator progressCargando;

    public CrearResumenFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_resumen, container, false);

        // 1. Inicializar vistas
        spinnerApuntes = view.findViewById(R.id.spinnerApuntesExistentes);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoriaResumen);
        etTitulo = view.findViewById(R.id.etTituloResumen);
        etDescripcion = view.findViewById(R.id.etDescripcionResumen);
        btnGenerar = view.findViewById(R.id.btnGenerarResumenIA);
        progressCargando = view.findViewById(R.id.progressCargando);

        // 2. Cargar datos
        cargarApuntesDesdeFirebase();
        configurarSpinnerCategorias();

        // 3. Configurar botón
        btnGenerar.setOnClickListener(v -> {
            if (validarCampos()) {
                ejecutarProcesoIA();
            }
        });

        return view;
    }

    private void setCargando(boolean cargando) {
        if (cargando) {
            btnGenerar.setEnabled(false);
            btnGenerar.setText(""); // Quitamos el texto para que solo se vea el círculo girando
            progressCargando.setVisibility(View.VISIBLE);
        } else {
            btnGenerar.setEnabled(true);
            btnGenerar.setText("Crear Resumen");
            progressCargando.setVisibility(View.GONE);
        }
    }

    private void cargarApuntesDesdeFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("apuntes");

        // Buscamos solo los apuntes de este usuario
        ref.orderByChild("userId").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaApuntesObj.clear();
                nombresApuntes.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Apuntes apunte = ds.getValue(Apuntes.class);
                    if (apunte != null) {
                        listaApuntesObj.add(apunte);
                        nombresApuntes.add(apunte.getTitulo());
                    }
                }

                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            R.layout.spinner_item_blanco, nombresApuntes);

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerApuntes.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void configurarSpinnerCategorias() {
        String[] categorias = {"Matemáticas", "Ciencias", "Historia", "Inglés", "Tecnología"};

        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    R.layout.spinner_item_blanco, categorias);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerCategoria.setAdapter(adapter);
        }
    }

    private boolean validarCampos() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (spinnerApuntes.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Debes tener al menos un apunte", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (titulo.isEmpty()) {
            etTitulo.setError("El título es obligatorio");
            return false;
        }
        if (descripcion.length() > 150) {
            etDescripcion.setError("La descripción es demasiado larga (máx 150)");
            return false;
        }
        return true;
    }

    private void ejecutarProcesoIA() {
        // Obtenemos el apunte seleccionado del Spinner
        int pos = spinnerApuntes.getSelectedItemPosition();
        Apuntes seleccionado = listaApuntesObj.get(pos);

        // Obtenemos los datos personalizados de los campos
        String tituloPersonalizado = etTitulo.getText().toString();
        String categoriaPersonalizada = spinnerCategoria.getSelectedItem().toString();

        // El contenido que enviamos a la IA es el del apunte seleccionado
        // Nota: Asegúrate de que tu modelo Apunte tenga un campo 'descripcion' o 'contenido'
        pedirResumenAlBackend(seleccionado.getDescripcion(), tituloPersonalizado, categoriaPersonalizada);
    }

    private void pedirResumenAlBackend(String textoApunte, String tituloNuevo, String categoriaNueva) {
        setCargando(true); // <--- Empieza el efecto visual

        Map<String, String> payload = new HashMap<>();
        payload.put("texto", textoApunte);

        RetrofitClient.getResumenService().generarResumen(payload).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                setCargando(false); // <--- Termina el efecto visual

                if (response.isSuccessful() && response.body() != null) {
                    String resumenIA = response.body().get("resumen");
                    guardarResumenEnFirebase(resumenIA, tituloNuevo, categoriaNueva);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                setCargando(false); // <--- Termina incluso si hay error
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarResumenEnFirebase(String contenido, String titulo, String categoria) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("resumenes");
        String id = ref.push().getKey();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("id", id);
        resumen.put("userId", uid);
        resumen.put("titulo", titulo); // Usamos el título personalizado del usuario
        resumen.put("contenido", contenido);
        resumen.put("fecha", new java.util.Date().toString());
        resumen.put("categoria", categoria); // Usamos la categoría personalizada

        if (id != null) {
            ref.child(id).setValue(resumen).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "¡Resumen creado con éxito!", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }
    }
}
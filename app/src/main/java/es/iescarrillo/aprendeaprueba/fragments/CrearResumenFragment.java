package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
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
import es.iescarrillo.aprendeaprueba.utils.GenerationStateUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearResumenFragment extends Fragment {

    private Spinner spinnerApuntes, spinnerCategoria;
    private EditText etTitulo, etDescripcion;
    private MaterialButton btnGenerar;

    private final List<Apuntes> listaApuntesObj = new ArrayList<>();
    private final List<String> nombresApuntes = new ArrayList<>();

    private final String[] categoriasValores = {"matematicas", "ciencias", "historia", "ingles", "tecnologia"};
    private final String[] categoriasNombres = {"Matematicas", "Ciencias", "Historia", "Ingles", "Tecnologia"};

    public CrearResumenFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_resumen, container, false);

        spinnerApuntes = view.findViewById(R.id.spinnerApuntesExistentes);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoriaResumen);
        etTitulo = view.findViewById(R.id.etTituloResumen);
        etDescripcion = view.findViewById(R.id.etDescripcionResumen);
        btnGenerar = view.findViewById(R.id.btnGenerarResumenIA);

        cargarApuntesDesdeFirebase();
        configurarSpinnerCategorias();

        btnGenerar.setOnClickListener(v -> {
            if (validarCampos()) ejecutarProcesoIA();
        });

        return view;
    }

    private void cargarApuntesDesdeFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("apuntes");

        ref.orderByChild("userId").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaApuntesObj.clear();
                nombresApuntes.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Apuntes apunte = ds.getValue(Apuntes.class);
                    if (apunte != null) {
                        apunte.setId(ds.getKey());
                        listaApuntesObj.add(apunte);
                        nombresApuntes.add(apunte.getTitulo());
                    }
                }
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            R.layout.spinner_item_blanco, nombresApuntes);
                    adapter.setDropDownViewResource(R.layout.spinner_item_blanco);
                    spinnerApuntes.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void configurarSpinnerCategorias() {
        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    R.layout.spinner_item_blanco, categoriasNombres);
            adapter.setDropDownViewResource(R.layout.spinner_item_blanco);
            spinnerCategoria.setAdapter(adapter);
        }
    }

    private String getCategoriaValor() {
        return categoriasValores[spinnerCategoria.getSelectedItemPosition()];
    }

    private boolean validarCampos() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (spinnerApuntes.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Debes tener al menos un apunte", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (titulo.isEmpty()) {
            etTitulo.setError("El titulo es obligatorio");
            return false;
        }
        if (descripcion.length() > 150) {
            etDescripcion.setError("La descripcion es demasiado larga (max 150)");
            return false;
        }
        return true;
    }

    private void ejecutarProcesoIA() {
        int pos = spinnerApuntes.getSelectedItemPosition();
        Apuntes seleccionado = listaApuntesObj.get(pos);
        if (GenerationStateUtils.isApunteGenerating(seleccionado)) {
            Toast.makeText(getContext(), "Ese apunte aun se esta digitalizando. Espera a que termine.", Toast.LENGTH_LONG).show();
            return;
        }

        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = getCategoriaValor();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("resumenes");
        String id = ref.push().getKey();
        if (id == null) return;

        Map<String, Object> resumenInicial = new HashMap<>();
        resumenInicial.put("id", id);
        resumenInicial.put("userId", uid);
        resumenInicial.put("titulo", titulo);
        resumenInicial.put("descripcion", descripcion);
        resumenInicial.put("resumenTexto", GenerationStateUtils.APUNTE_GENERANDO);
        resumenInicial.put("estado", "generando");
        resumenInicial.put("fecha", System.currentTimeMillis());
        resumenInicial.put("categoria", categoria);

        ref.child(id).setValue(resumenInicial).addOnSuccessListener(aVoid -> {
            btnGenerar.setEnabled(false);
            Toast.makeText(getContext(), "Resumen creado. La IA sigue generando el contenido en segundo plano...", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) getParentFragmentManager().popBackStack();
            }, 700);
        });

        String resumenId = id;
        Map<String, String> payload = new HashMap<>();
        payload.put("contenido", seleccionado.getContenido());

        RetrofitClient.getResumenService().generarResumen(payload).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                String contenidoFinal;
                if (response.isSuccessful() && response.body() != null) {
                    String resumenIA = response.body().get("resumen");
                    contenidoFinal = (resumenIA != null && !resumenIA.trim().isEmpty() && !resumenIA.equals("null"))
                            ? resumenIA
                            : "La IA no pudo generar el resumen. Edita el contenido manualmente.";
                } else {
                    contenidoFinal = "Error al generar con IA. Edita el contenido manualmente.";
                }
                String estado = contenidoFinal.startsWith("Error") || contenidoFinal.startsWith("La IA no pudo")
                        ? "error"
                        : "listo";
                actualizarResumenSiExiste(ref, resumenId, contenidoFinal, estado);
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                actualizarResumenSiExiste(ref, resumenId, "Error de conexion. Edita el contenido manualmente.", "error");
            }
        });
    }

    private void actualizarResumenSiExiste(DatabaseReference ref, String resumenId, String contenido, String estado) {
        ref.child(resumenId).get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) return;

            Map<String, Object> updates = new HashMap<>();
            updates.put("resumenTexto", contenido);
            updates.put("estado", estado);
            ref.child(resumenId).updateChildren(updates);
        });
    }
}

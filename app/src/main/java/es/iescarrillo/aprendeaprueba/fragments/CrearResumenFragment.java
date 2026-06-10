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

    /**
     * Infla el layout del fragmento, inicializa las vistas y configura los spinners,
     * el autorelleno de descripción y el botón de generar.
     */
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
        configurarAutoRellenoDescripcion();

        btnGenerar.setOnClickListener(v -> {
            if (validarCampos()) ejecutarProcesoIA();
        });

        return view;
    }

    /**
     * Carga una sola vez los apuntes del usuario desde Firebase y los muestra en el spinner.
     * Usa addListenerForSingleValueEvent porque no se necesita escucha en tiempo real aquí.
     */
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
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Configura el spinner de categorías con el adaptador y el layout personalizado.
     */
    private void configurarSpinnerCategorias() {
        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    R.layout.spinner_item_blanco, categoriasNombres);
            adapter.setDropDownViewResource(R.layout.spinner_item_blanco);
            spinnerCategoria.setAdapter(adapter);
        }
    }

    /**
     * Rellena automáticamente el campo de descripción con la del apunte seleccionado
     * cada vez que el usuario cambia la selección en el spinner de apuntes.
     */
    private void configurarAutoRellenoDescripcion() {
        spinnerApuntes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position < listaApuntesObj.size()) {
                    String desc = listaApuntesObj.get(position).getDescripcion();
                    etDescripcion.setText(desc != null ? desc : "");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    /**
     * Devuelve el valor interno de la categoría seleccionada en el spinner.
     */
    private String getCategoriaValor() {
        int pos = spinnerCategoria.getSelectedItemPosition();
        if (pos < 0 || pos >= categoriasValores.length) return "";
        return categoriasValores[pos];
    }

    /**
     * Comprueba que el formulario sea válido antes de lanzar la generación.
     * Verifica que haya apuntes disponibles, que el título no esté vacío
     * y que la descripción no supere los 150 caracteres.
     * @return true si todos los campos son válidos, false en caso contrario.
     */
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

    /**
     * Crea el nodo del resumen en Firebase con estado "generando", vuelve atrás
     * y lanza en paralelo la llamada al backend de IA con el contenido del apunte seleccionado.
     * Cuando la IA responde, actualiza el resumen con el texto generado o con un mensaje de error.
     */
    private void ejecutarProcesoIA() {
        int pos = spinnerApuntes.getSelectedItemPosition();
        if (pos < 0 || pos >= listaApuntesObj.size()) return;
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

        ref.child(id).setValue(resumenInicial)
                .addOnSuccessListener(aVoid -> {
                    btnGenerar.setEnabled(false);
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Resumen creado. La IA sigue generando el contenido en segundo plano...", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded()) getParentFragmentManager().popBackStack();
                    }, 700);
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Error al crear el resumen", Toast.LENGTH_SHORT).show();
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

    /**
     * Actualiza el texto y el estado del resumen en Firebase solo si el nodo sigue existiendo.
     * La comprobación previa evita escribir sobre un resumen que el usuario haya borrado mientras se generaba.
     * @param ref       Referencia al nodo "resumenes" en Firebase.
     * @param resumenId ID del resumen a actualizar.
     * @param contenido Texto generado por la IA o mensaje de error.
     * @param estado    Estado final del resumen ("listo" o "error").
     */
    private void actualizarResumenSiExiste(DatabaseReference ref, String resumenId, String contenido, String estado) {
        ref.child(resumenId).get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) return;

            Map<String, Object> updates = new HashMap<>();
            updates.put("resumenTexto", contenido);
            updates.put("estado", estado);
            ref.child(resumenId).updateChildren(updates).addOnFailureListener(e -> {});
        });
    }
}
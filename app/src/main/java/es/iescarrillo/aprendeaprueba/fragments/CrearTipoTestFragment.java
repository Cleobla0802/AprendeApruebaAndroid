package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
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
import es.iescarrillo.aprendeaprueba.models.Pregunta;
import es.iescarrillo.aprendeaprueba.models.Resumen;
import es.iescarrillo.aprendeaprueba.models.Test;
import es.iescarrillo.aprendeaprueba.services.TestService;
import es.iescarrillo.aprendeaprueba.utils.GenerationStateUtils;
import es.iescarrillo.aprendeaprueba.utils.IaTextUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearTipoTestFragment extends Fragment {

    private RadioGroup radioGroupFuente;
    private Spinner spinnerFuente, spinnerPreguntas;
    private MaterialButton btnGenerar, btnCancelar;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private final List<Apuntes> listaApuntes = new ArrayList<>();
    private final List<Resumen> listaResumenes = new ArrayList<>();
    private ArrayAdapter<String> adapterSpinner;
    private final List<String> titulosSpinner = new ArrayList<>();

    private static final int LIMITE_CONTENIDO_TEST = 7000;
    private final String[] opcionesPreguntas = {"5 preguntas", "10 preguntas", "15 preguntas"};
    private final int[] valoresPreguntas = {5, 10, 15};

    public CrearTipoTestFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_tipo_test, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        radioGroupFuente = view.findViewById(R.id.radioGroupFuente);
        spinnerFuente = view.findViewById(R.id.spinnerFuente);
        spinnerPreguntas = view.findViewById(R.id.spinnerPreguntas);
        btnGenerar = view.findViewById(R.id.btnGenerarTest);
        btnCancelar = view.findViewById(R.id.btnCancelarCrear);

        adapterSpinner = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_blanco, titulosSpinner);
        adapterSpinner.setDropDownViewResource(R.layout.spinner_item_blanco);
        spinnerFuente.setAdapter(adapterSpinner);

        ArrayAdapter<String> adapterPreguntas = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_blanco, opcionesPreguntas);
        adapterPreguntas.setDropDownViewResource(R.layout.spinner_item_blanco);
        spinnerPreguntas.setAdapter(adapterPreguntas);
        spinnerPreguntas.setSelection(1);

        cargarApuntes();

        radioGroupFuente.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioApunte) cargarApuntes();
            else cargarResumenes();
        });

        btnGenerar.setOnClickListener(v -> generarTest());
        btnCancelar.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void cargarApuntes() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("apuntes").orderByChild("userId").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaApuntes.clear();
                        titulosSpinner.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Apuntes a = ds.getValue(Apuntes.class);
                            if (a != null) {
                                a.setId(ds.getKey());
                                listaApuntes.add(a);
                                titulosSpinner.add(a.getTitulo());
                            }
                        }
                        adapterSpinner.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void cargarResumenes() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("resumenes").orderByChild("userId").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaResumenes.clear();
                        titulosSpinner.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Resumen r = ds.getValue(Resumen.class);
                            if (r != null) {
                                r.setId(ds.getKey());
                                listaResumenes.add(r);
                                titulosSpinner.add(r.getTitulo());
                            }
                        }
                        adapterSpinner.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void generarTest() {
        if (titulosSpinner.isEmpty()) {
            Toast.makeText(getContext(), "No hay elementos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spinnerFuente.getSelectedItemPosition();
        int cantidadPreguntas = valoresPreguntas[spinnerPreguntas.getSelectedItemPosition()];
        boolean esApunte = radioGroupFuente.getCheckedRadioButtonId() == R.id.radioApunte;

        String contenido, titulo, categoria, materialId;
        String materialTipo = esApunte ? "apuntes" : "resumenes";

        if (esApunte) {
            Apuntes apunte = listaApuntes.get(pos);
            if (GenerationStateUtils.isApunteGenerating(apunte)) {
                Toast.makeText(getContext(), "Ese apunte aun se esta digitalizando. Espera a que termine.", Toast.LENGTH_LONG).show();
                return;
            }
            contenido = apunte.getContenido();
            titulo = apunte.getTitulo();
            categoria = apunte.getCategoria();
            materialId = apunte.getId();
        } else {
            Resumen resumen = listaResumenes.get(pos);
            if (GenerationStateUtils.isResumenGenerating(resumen)) {
                Toast.makeText(getContext(), "Ese resumen aun se esta generando. Espera a que termine.", Toast.LENGTH_LONG).show();
                return;
            }
            contenido = resumen.getResumenTexto();
            titulo = resumen.getTitulo();
            categoria = resumen.getCategoria();
            materialId = resumen.getId();
        }

        String contenidoProcesado = IaTextUtils.prepararContenidoParaIA(contenido, LIMITE_CONTENIDO_TEST);
        if (contenidoProcesado.trim().isEmpty()) {
            Toast.makeText(getContext(), "El material seleccionado no tiene contenido suficiente", Toast.LENGTH_LONG).show();
            return;
        }
        String contenidoHash = IaTextUtils.crearHashContenido(contenidoProcesado);

        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        DatabaseReference testsRef = FirebaseDatabase.getInstance().getReference("tests");
        String nuevoId = testsRef.push().getKey();
        if (nuevoId == null) return;

        Map<String, Object> testInicial = new HashMap<>();
        testInicial.put("id", nuevoId);
        testInicial.put("userId", uid);
        testInicial.put("titulo", "Test de " + titulo);
        testInicial.put("categoria", categoria);
        testInicial.put("preguntas", new ArrayList<>());
        testInicial.put("estado", "generando");
        testInicial.put("completado", false);
        testInicial.put("ultimaNota", 0);
        testInicial.put("cantidadPreguntas", cantidadPreguntas);
        testInicial.put("materialTipo", materialTipo);
        testInicial.put("materialId", materialId != null ? materialId : "");
        testInicial.put("contenidoHash", contenidoHash);
        testInicial.put("fecha", System.currentTimeMillis());

        testsRef.child(nuevoId).setValue(testInicial).addOnSuccessListener(a -> {
            btnGenerar.setEnabled(false);
            Toast.makeText(getContext(), "Test creado. Las preguntas se estan generando en segundo plano...", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) getParentFragmentManager().popBackStack();
            }, 700);
        });

        Map<String, Object> body = new HashMap<>();
        body.put("contenido", contenidoProcesado);
        body.put("userId", uid);
        body.put("titulo", titulo);
        body.put("categoria", categoria);
        body.put("cantidadPreguntas", cantidadPreguntas);

        String testId = nuevoId;
        TestService service = RetrofitClient.getTestService();
        service.generarTest(body).enqueue(new Callback<Test>() {
            @Override
            public void onResponse(@NonNull Call<Test> call, @NonNull Response<Test> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Test test = response.body();
                    if (test.getPreguntas() != null && !test.getPreguntas().isEmpty()) {
                        List<Pregunta> preguntas = test.getPreguntas().size() > cantidadPreguntas
                                ? test.getPreguntas().subList(0, cantidadPreguntas)
                                : test.getPreguntas();
                        Map<String, Object> update = new HashMap<>();
                        update.put("preguntas", preguntas);
                        update.put("estado", "listo");
                        actualizarTestSiExiste(testsRef, testId, update);
                    } else {
                        Map<String, Object> errorUpdate = new HashMap<>();
                        errorUpdate.put("estado", "error");
                        errorUpdate.put("descripcion", "No se pudieron generar preguntas. Vuelve a intentarlo.");
                        actualizarTestSiExiste(testsRef, testId, errorUpdate);
                    }
                } else {
                    Map<String, Object> errorUpdate = new HashMap<>();
                    errorUpdate.put("estado", "error");
                    errorUpdate.put("descripcion", "Error al generar preguntas. Vuelve a intentarlo.");
                    actualizarTestSiExiste(testsRef, testId, errorUpdate);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Test> call, @NonNull Throwable t) {
                Map<String, Object> errorUpdate = new HashMap<>();
                errorUpdate.put("estado", "error");
                errorUpdate.put("descripcion", "Error de conexion. Edita el contenido manualmente.");
                actualizarTestSiExiste(testsRef, testId, errorUpdate);
            }
        });
    }

    private void actualizarTestSiExiste(DatabaseReference testsRef, String testId, Map<String, Object> updates) {
        testsRef.child(testId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                testsRef.child(testId).updateChildren(updates);
            }
        });
    }
}

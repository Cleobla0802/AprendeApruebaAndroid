package es.iescarrillo.aprendeaprueba.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Pregunta;
import es.iescarrillo.aprendeaprueba.models.Test;

public class RealizarTipoTestFragment extends Fragment {

    private TextView tvTitulo, tvContador, tvEnunciado;
    private RadioGroup radioGroup;
    private RadioButton opcionA, opcionB, opcionC, opcionD;
    private MaterialButton btnSiguiente, btnCancelar;

    private Test test;
    private List<Pregunta> preguntas;
    private int preguntaActual = 0;
    private int aciertos = 0;
    private DatabaseReference mDatabase;

    public RealizarTipoTestFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realizar_tipo_test, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference("tests");

        tvTitulo = view.findViewById(R.id.tvTituloTest);
        tvContador = view.findViewById(R.id.tvContadorPreguntas);
        tvEnunciado = view.findViewById(R.id.tvEnunciado);
        radioGroup = view.findViewById(R.id.radioGroupOpciones);
        opcionA = view.findViewById(R.id.opcionA);
        opcionB = view.findViewById(R.id.opcionB);
        opcionC = view.findViewById(R.id.opcionC);
        opcionD = view.findViewById(R.id.opcionD);
        btnSiguiente = view.findViewById(R.id.btnSiguiente);
        btnCancelar = view.findViewById(R.id.btnCancelarTest);

        if (getArguments() != null) {
            test = (Test) getArguments().getSerializable("test_objeto");
        }

        if (test == null || test.getPreguntas() == null) {
            Toast.makeText(getContext(), "Error al cargar el test", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        preguntas = test.getPreguntas();
        tvTitulo.setText(test.getTitulo());
        mostrarPregunta();

        btnSiguiente.setOnClickListener(v -> {
            if (radioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(getContext(), "Selecciona una opción", Toast.LENGTH_SHORT).show();
                return;
            }
            comprobarRespuesta();
        });

        btnCancelar.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void mostrarPregunta() {
        Pregunta p = preguntas.get(preguntaActual);
        tvContador.setText("Pregunta " + (preguntaActual + 1) + " de " + preguntas.size());
        tvEnunciado.setText(p.getEnunciado());
        radioGroup.clearCheck();

        List<String> opciones = p.getOpciones();
        opcionA.setText(opciones.size() > 0 ? opciones.get(0) : "");
        opcionB.setText(opciones.size() > 1 ? opciones.get(1) : "");
        opcionC.setText(opciones.size() > 2 ? opciones.get(2) : "");
        opcionD.setText(opciones.size() > 3 ? opciones.get(3) : "");

        boolean esUltima = preguntaActual == preguntas.size() - 1;
        btnSiguiente.setText(esUltima ? "Finalizar" : "Siguiente");
    }

    private void comprobarRespuesta() {
        Pregunta p = preguntas.get(preguntaActual);
        int seleccionado = -1;

        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.opcionA) seleccionado = 0;
        else if (checkedId == R.id.opcionB) seleccionado = 1;
        else if (checkedId == R.id.opcionC) seleccionado = 2;
        else if (checkedId == R.id.opcionD) seleccionado = 3;

        if (seleccionado == p.getRespuestaCorrecta()) aciertos++;

        preguntaActual++;

        if (preguntaActual < preguntas.size()) {
            mostrarPregunta();
        } else {
            finalizarTest();
        }
    }

    private void finalizarTest() {
        int nota = (aciertos * 100) / preguntas.size();

        Map<String, Object> actualizacion = new HashMap<>();
        actualizacion.put("completado", true);
        actualizacion.put("ultimaNota", nota);

        mDatabase.child(test.getId()).updateChildren(actualizacion)
                .addOnSuccessListener(a -> {
                    Toast.makeText(getContext(),
                            "Test completado: " + aciertos + "/" + preguntas.size() + " (" + nota + "%)",
                            Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar nota", Toast.LENGTH_SHORT).show());
    }
}
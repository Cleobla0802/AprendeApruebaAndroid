package es.iescarrillo.aprendeaprueba.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.iescarrillo.aprendeaprueba.R;
import es.iescarrillo.aprendeaprueba.models.Pregunta;
import es.iescarrillo.aprendeaprueba.models.Test;

public class RealizarTipoTestFragment extends Fragment {

    private TextView tvTitulo, tvContador, tvEnunciado;
    private TextView opcionA, opcionB, opcionC, opcionD;
    private MaterialCardView cardA, cardB, cardC, cardD;
    private MaterialButton btnAnterior, btnSiguiente, btnCancelar, btnFinalizar;
    private ProgressBar progressBar;

    private Test test;
    private List<Pregunta> preguntas;
    private int preguntaActual = 0;
    private int opcionSeleccionada = -1;
    private int[] respuestas;
    private DatabaseReference mDatabase;

    private static final int COLOR_SELECCIONADO = 0xFF2A1F4A;
    private static final int COLOR_NORMAL = 0xFF1E1B2E;

    public RealizarTipoTestFragment() {}

    /**
     * Inicializa la vista del fragmento: enlaza los componentes del layout,
     * carga el test recibido por argumentos y configura los listeners de los botones y opciones.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realizar_tipo_test, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference("tests");

        tvTitulo = view.findViewById(R.id.tvTituloTest);
        tvContador = view.findViewById(R.id.tvContadorPreguntas);
        tvEnunciado = view.findViewById(R.id.tvEnunciado);
        progressBar = view.findViewById(R.id.progressBar);

        cardA = view.findViewById(R.id.cardOpcionA);
        cardB = view.findViewById(R.id.cardOpcionB);
        cardC = view.findViewById(R.id.cardOpcionC);
        cardD = view.findViewById(R.id.cardOpcionD);

        opcionA = view.findViewById(R.id.opcionA);
        opcionB = view.findViewById(R.id.opcionB);
        opcionC = view.findViewById(R.id.opcionC);
        opcionD = view.findViewById(R.id.opcionD);

        btnAnterior = view.findViewById(R.id.btnAnterior);
        btnSiguiente = view.findViewById(R.id.btnSiguiente);
        btnCancelar = view.findViewById(R.id.btnCancelarTest);
        btnFinalizar = view.findViewById(R.id.btnFinalizarTest);

        if (getArguments() != null) {
            test = (Test) getArguments().getSerializable("test_objeto");
        }

        if (test == null || test.getPreguntas() == null || test.getPreguntas().isEmpty()) {
            Toast.makeText(getContext(), "Error al cargar el test", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return view;
        }

        preguntas = test.getPreguntas();
        respuestas = new int[preguntas.size()];
        for (int i = 0; i < respuestas.length; i++) respuestas[i] = -1;

        tvTitulo.setText(test.getTitulo());
        mostrarPregunta();

        cardA.setOnClickListener(v -> seleccionarOpcion(0));
        cardB.setOnClickListener(v -> seleccionarOpcion(1));
        cardC.setOnClickListener(v -> seleccionarOpcion(2));
        cardD.setOnClickListener(v -> seleccionarOpcion(3));

        btnAnterior.setOnClickListener(v -> {
            if (preguntaActual > 0) {
                irAPregunta(preguntaActual - 1);
            }
        });

        btnSiguiente.setOnClickListener(v -> continuar());
        btnFinalizar.setOnClickListener(v -> intentarFinalizar());

        btnCancelar.setOnClickListener(v -> confirmarSalida());

        return view;
    }

    /**
     * Avanza a la siguiente pregunta si hay una opción seleccionada.
     * Si es la última pregunta, intenta finalizar el test.
     * Si no hay opción elegida, muestra un aviso al usuario.
     */
    private void continuar() {
        if (opcionSeleccionada == -1) {
            Toast.makeText(getContext(), "Elige una respuesta o pulsa Omitir.", Toast.LENGTH_SHORT).show();
            return;
        }

        respuestas[preguntaActual] = opcionSeleccionada;
        if (preguntaActual < preguntas.size() - 1) {
            irAPregunta(preguntaActual + 1);
        } else {
            intentarFinalizar();
        }
    }

    /**
     * Registra la opción elegida por el usuario para la pregunta actual
     * y actualiza el estado visual de las tarjetas.
     *
     * @param index Índice de la opción seleccionada (0=A, 1=B, 2=C, 3=D)
     */
    private void seleccionarOpcion(int index) {
        opcionSeleccionada = index;
        respuestas[preguntaActual] = index;
        actualizarEstadoVisual();
    }

    /**
     * Navega directamente a la pregunta indicada por su índice.
     *
     * @param index Índice de la pregunta a mostrar
     */
    private void irAPregunta(int index) {
        preguntaActual = index;
        mostrarPregunta();
    }

    /**
     * Actualiza el color de las tarjetas de opciones según la selección actual,
     * el contador de preguntas respondidas, la barra de progreso y la visibilidad de botones.
     */
    private void actualizarEstadoVisual() {
        MaterialCardView[] cards = {cardA, cardB, cardC, cardD};
        for (int i = 0; i < cards.length; i++) {
            cards[i].setCardBackgroundColor(i == opcionSeleccionada ? COLOR_SELECCIONADO : COLOR_NORMAL);
            cards[i].setCardElevation(i == opcionSeleccionada ? 8f : 2f);
        }

        int respondidas = contarRespondidas();
        tvContador.setText("Pregunta " + (preguntaActual + 1) + " de " + preguntas.size()
                + " - " + respondidas + "/" + preguntas.size() + " respondidas");
        progressBar.setProgress((respondidas * 100) / preguntas.size());

        btnAnterior.setVisibility(preguntaActual > 0 ? View.VISIBLE : View.GONE);
        btnFinalizar.setVisibility(View.GONE);
        btnSiguiente.setText(preguntaActual == preguntas.size() - 1 ? "Terminar" : "Continuar");
    }

    /**
     * Carga los datos de la pregunta actual en los componentes de la vista:
     * enunciado, opciones de respuesta y estado de selección previo si existe.
     */
    private void mostrarPregunta() {
        Pregunta p = preguntas.get(preguntaActual);
        tvEnunciado.setText(p.getEnunciado());

        List<String> opciones = p.getOpciones() != null ? p.getOpciones() : Collections.emptyList();
        opcionA.setText(opciones.size() > 0 ? opciones.get(0) : "");
        opcionB.setText(opciones.size() > 1 ? opciones.get(1) : "");
        opcionC.setText(opciones.size() > 2 ? opciones.get(2) : "");
        opcionD.setText(opciones.size() > 3 ? opciones.get(3) : "");

        opcionSeleccionada = respuestas[preguntaActual];
        actualizarEstadoVisual();
    }

    /**
     * Recorre el array de respuestas y devuelve el índice de la primera pregunta
     * que aún no ha sido respondida. Devuelve -1 si todas están respondidas.
     *
     * @return Índice de la primera pregunta sin responder, o -1 si no hay ninguna
     */
    private int buscarPrimeroSinResponder() {
        for (int i = 0; i < respuestas.length; i++) {
            if (respuestas[i] == -1) return i;
        }
        return -1;
    }

    /**
     * Cuenta cuántas preguntas han sido respondidas (valor distinto de -1).
     *
     * @return Número de preguntas respondidas
     */
    private int contarRespondidas() {
        int count = 0;
        for (int r : respuestas) if (r != -1) count++;
        return count;
    }

    /**
     * Cuenta cuántas preguntas quedan sin responder (valor igual a -1).
     *
     * @return Número de preguntas sin responder
     */
    private int contarSinResponder() {
        int count = 0;
        for (int r : respuestas) if (r == -1) count++;
        return count;
    }

    /**
     * Comprueba si quedan preguntas sin responder antes de finalizar.
     * Si las hay, muestra un diálogo ofreciendo revisarlas o terminar igualmente.
     * Si todas están respondidas, pide confirmación para guardar la nota.
     */
    private void intentarFinalizar() {
        int sinResponder = contarSinResponder();
        if (sinResponder > 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Preguntas pendientes")
                    .setMessage("Te quedan " + sinResponder + " pregunta(s) sin responder. Puedes revisarlas o terminar ahora y contarlas como incorrectas.")
                    .setPositiveButton("Revisar", (dialog, which) -> {
                        int primeroSinResponder = buscarPrimeroSinResponder();
                        if (primeroSinResponder != -1) {
                            irAPregunta(primeroSinResponder);
                        }
                    })
                    .setNegativeButton("Terminar", (dialog, which) -> finalizarTest())
                    .setNeutralButton("Cancelar", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Finalizar test")
                .setMessage("Vas a guardar la nota de este intento.")
                .setPositiveButton("Finalizar", (dialog, which) -> finalizarTest())
                .setNegativeButton("Seguir revisando", null)
                .show();
    }

    /**
     * Muestra un diálogo de confirmación al pulsar Cancelar.
     * Si no se ha respondido ninguna pregunta, sale directamente sin preguntar.
     */
    private void confirmarSalida() {
        if (contarRespondidas() == 0) {
            getParentFragmentManager().popBackStack();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Salir del test")
                .setMessage("Si sales ahora no se guardara la nota de este intento.")
                .setPositiveButton("Salir", (dialog, which) -> getParentFragmentManager().popBackStack())
                .setNegativeButton("Continuar", null)
                .show();
    }

    /**
     * Calcula el resultado del test comparando las respuestas del usuario con las correctas,
     * y guarda la nota en Firebase. Al terminar, muestra el resultado y vuelve a la pantalla anterior.
     */
    private void finalizarTest() {
        int aciertos = 0;
        for (int i = 0; i < preguntas.size(); i++) {
            if (respuestas[i] == preguntas.get(i).getRespuestaCorrecta()) {
                aciertos++;
            }
        }
        final int aciertosFinal = aciertos;
        int nota = (aciertos * 100) / preguntas.size();
        double calificacion = Math.round(((double) aciertos / preguntas.size()) * 100.0) / 10.0;

        Map<String, Object> actualizacion = new HashMap<>();
        actualizacion.put("completado", true);
        actualizacion.put("ultimaNota", nota);
        actualizacion.put("calificacion", calificacion);

        mDatabase.child(test.getId()).updateChildren(actualizacion)
                .addOnSuccessListener(a -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(),
                                "Test completado: " + aciertosFinal + "/" + preguntas.size() + " (" + nota + "%)",
                                Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Error al guardar nota", Toast.LENGTH_SHORT).show();
                });
    }
}
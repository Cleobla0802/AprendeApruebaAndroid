package es.iescarrillo.aprendeaprueba.utils;

import java.util.List;

import es.iescarrillo.aprendeaprueba.models.Apuntes;
import es.iescarrillo.aprendeaprueba.models.Pregunta;
import es.iescarrillo.aprendeaprueba.models.Resumen;
import es.iescarrillo.aprendeaprueba.models.Test;

public final class GenerationStateUtils {

    public static final String APUNTE_GENERANDO = "Generando sus apuntes, espere...";

    private GenerationStateUtils() {}

    public static boolean isApunteGenerating(Apuntes apunte) {
        if (apunte == null) return true;
        String estado = apunte.getEstado();
        if ("generando".equalsIgnoreCase(estado)) return true;
        return isTextGenerating(apunte.getContenido());
    }

    public static boolean isResumenGenerating(Resumen resumen) {
        if (resumen == null) return true;
        String estado = resumen.getEstado();
        if ("generando".equalsIgnoreCase(estado)) return true;
        if ("error".equalsIgnoreCase(estado)) return false;
        return isTextGenerating(resumen.getResumenTexto());
    }

    public static boolean isTestGenerating(Test test) {
        if (test == null) return true;
        String estado = test.getEstado();
        if ("generando".equalsIgnoreCase(estado)) return true;
        if ("error".equalsIgnoreCase(estado)) return false;
        if (estado == null || estado.trim().isEmpty()) return false;
        List<Pregunta> preguntas = test != null ? test.getPreguntas() : null;
        return preguntas == null || preguntas.isEmpty();
    }

    public static boolean isTestError(Test test) {
        if (test == null) return false;
        String estado = test.getEstado();
        if ("error".equalsIgnoreCase(estado)) return true;
        List<Pregunta> preguntas = test.getPreguntas();
        return (estado == null || estado.trim().isEmpty()) && (preguntas == null || preguntas.isEmpty());
    }

    public static boolean isTextGenerating(String text) {
        return text == null || text.trim().isEmpty() || text.toLowerCase().contains("generando");
    }
}

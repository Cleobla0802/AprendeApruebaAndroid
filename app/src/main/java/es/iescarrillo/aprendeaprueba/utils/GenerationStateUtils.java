package es.iescarrillo.aprendeaprueba.utils;

import java.util.List;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
import es.iescarrillo.aprendeaprueba.models.Pregunta;
import es.iescarrillo.aprendeaprueba.models.Resumen;
import es.iescarrillo.aprendeaprueba.models.Test;

/**
 * Clase de utilidad para comprobar si un apunte, resumen o test
 * todavía está siendo generado por el backend.
 * Es final y tiene constructor privado para evitar instanciación,
 * ya que todos sus métodos son estáticos.
 */
public final class GenerationStateUtils {

    /** Mensaje de espera que se muestra mientras se generan los apuntes. */
    public static final String APUNTE_GENERANDO = "Generando sus apuntes, espere...";

    /** Constructor privado: esta clase no debe instanciarse. */
    private GenerationStateUtils() {}

    /**
     * Comprueba si un apunte está en proceso de generación.
     * Devuelve true si el apunte es null, si su estado es "generando",
     * o si su contenido aún no tiene texto válido.
     *
     * @param apunte El apunte a comprobar
     * @return true si el apunte está siendo generado
     */
    public static boolean isApunteGenerating(Apuntes apunte) {
        if (apunte == null) return true;
        String estado = apunte.getEstado();
        if ("generando".equalsIgnoreCase(estado)) return true;
        return isTextGenerating(apunte.getContenido());
    }

    /**
     * Comprueba si un resumen está en proceso de generación.
     * Devuelve false de forma inmediata si el estado es "error",
     * para distinguir entre "aún generando" y "falló la generación".
     *
     * @param resumen El resumen a comprobar
     * @return true si el resumen está siendo generado
     */
    public static boolean isResumenGenerating(Resumen resumen) {
        if (resumen == null) return true;
        String estado = resumen.getEstado();
        if ("generando".equalsIgnoreCase(estado)) return true;
        if ("error".equalsIgnoreCase(estado)) return false;
        return isTextGenerating(resumen.getResumenTexto());
    }

    /**
     * Comprueba si un test está en proceso de generación.
     * Considera que el test sigue generándose si no tiene preguntas cargadas,
     * salvo que el estado sea "error" o esté vacío (lo que indica un fallo).
     *
     * @param test El test a comprobar
     * @return true si el test está siendo generado
     */
    public static boolean isTestGenerating(Test test) {
        if (test == null) return true;
        String estado = test.getEstado();
        if ("generando".equalsIgnoreCase(estado)) return true;
        if ("error".equalsIgnoreCase(estado)) return false;
        if (estado == null || estado.trim().isEmpty()) return false;
        List<Pregunta> preguntas = test.getPreguntas();
        return preguntas == null || preguntas.isEmpty();
    }

    /**
     * Comprueba si un test ha fallado durante la generación.
     * Se considera error si el estado es "error" explícitamente, o si
     * el estado está vacío y además no hay preguntas (estado inconsistente).
     *
     * @param test El test a comprobar
     * @return true si el test tiene un error de generación
     */
    public static boolean isTestError(Test test) {
        if (test == null) return false;
        String estado = test.getEstado();
        if ("error".equalsIgnoreCase(estado)) return true;
        List<Pregunta> preguntas = test.getPreguntas();
        return (estado == null || estado.trim().isEmpty()) && (preguntas == null || preguntas.isEmpty());
    }

    /**
     * Comprueba si un texto indica que el contenido aún se está generando.
     * Un texto se considera "generando" si es null, está vacío,
     * o contiene literalmente la palabra "generando".
     *
     * @param text El texto a comprobar
     * @return true si el texto no tiene contenido válido o indica generación en curso
     */
    public static boolean isTextGenerating(String text) {
        return text == null || text.trim().isEmpty() || text.toLowerCase().contains("generando");
    }
}
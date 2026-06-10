package es.iescarrillo.aprendeaprueba.utils;

/**
 * Clase de utilidad con métodos para preparar y procesar texto
 * antes de enviarlo a la IA o para detectar cambios en el contenido.
 * Es final y tiene constructor privado para evitar instanciación,
 * ya que todos sus métodos son estáticos.
 */
public final class IaTextUtils {

    /** Constructor privado: esta clase no debe instanciarse. */
    private IaTextUtils() {}

    /**
     * Limpia y recorta el contenido de un apunte para enviarlo a la IA.
     * Elimina espacios y saltos de línea redundantes, y aplica un límite
     * de caracteres para no exceder el contexto máximo del modelo.
     *
     * @param contenido Texto original del apunte
     * @param limite    Número máximo de caracteres permitidos
     * @return Texto limpio y recortado, o "" si el contenido es null
     */
    public static String prepararContenidoParaIA(String contenido, int limite) {
        if (contenido == null) return "";

        String limpio = contenido
                .replaceAll("\\s+", " ")
                .trim();

        return limpio.substring(0, Math.min(limpio.length(), limite));
    }

    /**
     * Genera un hash FNV-1 de 32 bits a partir del contenido de un apunte.
     * Se usa para detectar si el contenido ha cambiado desde la última vez
     * que se generó un resumen o test, comparándolo con el hash almacenado.
     * No es criptográfico, pero es rápido y suficiente para detectar cambios.
     *
     * @param contenido Texto del apunte a hashear
     * @return Hash del contenido como String hexadecimal
     */
    public static String crearHashContenido(String contenido) {
        String texto = contenido != null ? contenido : "";
        int hash = 0x811c9dc5; // Valor inicial del algoritmo FNV-1 de 32 bits

        for (int i = 0; i < texto.length(); i++) {
            hash ^= texto.charAt(i);
            hash *= 0x01000193; // Primo FNV de 32 bits
        }

        return Integer.toHexString(hash);
    }
}
package es.iescarrillo.aprendeaprueba.utils;

public final class IaTextUtils {

    private IaTextUtils() {}

    public static String prepararContenidoParaIA(String contenido, int limite) {
        if (contenido == null) return "";

        String limpio = contenido
                .replaceAll("\\s+", " ")
                .trim();

        return limpio.substring(0, Math.min(limpio.length(), limite));
    }

    public static String crearHashContenido(String contenido) {
        String texto = contenido != null ? contenido : "";
        int hash = 0x811c9dc5;

        for (int i = 0; i < texto.length(); i++) {
            hash ^= texto.charAt(i);
            hash *= 0x01000193;
        }

        return Integer.toHexString(hash);
    }
}

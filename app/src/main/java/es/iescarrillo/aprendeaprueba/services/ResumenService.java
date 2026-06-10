package es.iescarrillo.aprendeaprueba.services;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interfaz Retrofit que define el endpoint del servicio de generación de resúmenes.
 * Se comunica con el backend para crear un resumen a partir del contenido de un apunte.
 */
public interface ResumenService {

    /**
     * Envía el contenido de un apunte al backend para generar un resumen automático.
     * El payload suele contener campos como "contenido" o "titulo" del apunte original.
     *
     * @param payload Mapa con los datos del apunte a resumir
     * @return Call con la respuesta del servidor (ej: clave "resumen" con el texto generado)
     */
    @POST("api/resumenes/generar")
    Call<Map<String, String>> generarResumen(@Body Map<String, String> payload);
}
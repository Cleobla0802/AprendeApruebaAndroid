package es.iescarrillo.aprendeaprueba.services;

import java.util.Map;
import es.iescarrillo.aprendeaprueba.models.Test;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interfaz Retrofit que define el endpoint del servicio de generación de tests.
 * Se comunica con el backend para crear un test de preguntas a partir de un material.
 */
public interface TestService {

    /**
     * Envía los datos de un material al backend para generar un test automático.
     * El body suele contener campos como "contenido", "cantidadPreguntas" o "materialTipo".
     * A diferencia de otros servicios, el backend devuelve directamente un objeto Test
     * con sus preguntas ya deserializadas.
     *
     * @param body Mapa con los parámetros necesarios para generar el test
     * @return Call con el objeto Test generado por el backend
     */
    @POST("api/tests/generar")
    Call<Test> generarTest(@Body Map<String, Object> body);
}
package es.iescarrillo.aprendeaprueba.services;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ResumenService {
    @POST("api/resumenes/generar") // Asegúrate de que este sea el path en tu Controller de Spring
    Call<Map<String, String>> generarResumen(@Body Map<String, String> payload);
}

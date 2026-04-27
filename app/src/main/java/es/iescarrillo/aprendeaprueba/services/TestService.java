package es.iescarrillo.aprendeaprueba.services;

import java.util.Map;
import es.iescarrillo.aprendeaprueba.models.Test;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TestService {
    @POST("api/tests/generar")
    Call<Test> generarTest(@Body Map<String, String> body);
}
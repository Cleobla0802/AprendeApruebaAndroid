package es.iescarrillo.aprendeaprueba.services;

import java.util.List;
import java.util.Map;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApunteService {


    @POST("api/apuntes/digitalizar")
    Call<Map<String, String>> digitalizarApunte(@Body Map<String, String> payload);

    @GET("api/apuntes/usuario/{uid}")
    Call<List<Apuntes>> getApuntesByUser(@Path("uid") String uid);

    @DELETE("api/apuntes/{id}")
    Call<Void> eliminarApunte(@Path("id") String id);
}
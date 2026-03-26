package es.iescarrillo.aprendeaprueba.services;

import java.util.List;
import es.iescarrillo.aprendeaprueba.models.Apuntes;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApunteService {

    // Obtener todos los apuntes
    @GET("apuntes")
    Call<List<Apuntes>> getApuntes();

    // Crear un nuevo apunte (Usamos Multipart para enviar texto + imagen)
    @Multipart
    @POST("apuntes")
    Call<Apuntes> crearApunte(
            @Part("titulo") RequestBody titulo,
            @Part("categoria") RequestBody categoria,
            @Part("dificultad") RequestBody dificultad,
            @Part MultipartBody.Part imagen
    );

    // Actualizar un apunte
    @Multipart
    @PUT("apuntes/{id}")
    Call<Apuntes> actualizarApunte(
            @Path("id") long id,
            @Part("titulo") RequestBody titulo,
            @Part("categoria") RequestBody categoria,
            @Part("dificultad") RequestBody dificultad,
            @Part MultipartBody.Part imagen
    );
}
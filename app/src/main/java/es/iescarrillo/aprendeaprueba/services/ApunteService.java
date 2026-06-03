package es.iescarrillo.aprendeaprueba.services;

import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApunteService {

    @POST("api/apuntes/digitalizar")
    Call<Map<String, String>> digitalizarApunte(@Body Map<String, String> payload);

    @Multipart
    @POST("api/apuntes/digitalizar-archivo")
    Call<ResponseBody> digitalizarArchivo(@Part MultipartBody.Part archivo);
}

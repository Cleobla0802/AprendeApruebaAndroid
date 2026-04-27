package es.iescarrillo.aprendeaprueba.api;

import java.util.concurrent.TimeUnit;

import es.iescarrillo.aprendeaprueba.services.ApunteService;
import es.iescarrillo.aprendeaprueba.services.ImgBBService;
import es.iescarrillo.aprendeaprueba.services.ResumenService; // Importa el nuevo servicio
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api-aprende-aprueba-1.onrender.com/";

    // 1. CAMBIO: Ahora es PUBLIC para que otros fragments puedan usarlo
    public static Retrofit getClient(String baseUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // 2. NUEVO MÉTODO: Para obtener el servicio de Resúmenes fácilmente
    public static ResumenService getResumenService() {
        return getClient(BASE_URL).create(ResumenService.class);
    }

    public static ApunteService getApunteService() {
        return getClient(BASE_URL).create(ApunteService.class);
    }

    public static ImgBBService getImgBBService() {
        return getClient("https://api.imgbb.com/").create(ImgBBService.class);
    }
}
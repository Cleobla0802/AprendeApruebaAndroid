package es.iescarrillo.aprendeaprueba.api;

import java.util.concurrent.TimeUnit;

import es.iescarrillo.aprendeaprueba.services.ApunteService;
import es.iescarrillo.aprendeaprueba.services.ImgBBService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api-aprende-aprueba-production.up.railway.app/";

    private static Retrofit getClient(String baseUrl) {
        // Configuramos OkHttpClient para evitar el SocketTimeoutException
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Tiempo máximo para conectar
                .readTimeout(30, TimeUnit.SECONDS)    // Tiempo máximo para recibir datos
                .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo máximo para enviar datos
                .retryOnConnectionFailure(true)       // Reintenta si hay un fallo puntual
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient) // Asignamos el cliente con más tiempo de espera
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApunteService getApunteService() {
        return getClient(BASE_URL).create(ApunteService.class);
    }

    public static ImgBBService getImgBBService() {
        return getClient("https://api.imgbb.com/").create(ImgBBService.class);
    }
}
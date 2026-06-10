package es.iescarrillo.aprendeaprueba.api;

import java.util.concurrent.TimeUnit;

import es.iescarrillo.aprendeaprueba.services.ApunteService;
import es.iescarrillo.aprendeaprueba.services.ResumenService;
import es.iescarrillo.aprendeaprueba.services.TestService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente centralizado de Retrofit para comunicarse con la API del backend.
 * Todos los servicios comparten la misma URL base y configuración de timeouts.
 */
public class RetrofitClient {

    private static final String BASE_URL = "https://api-aprende-aprueba-1.onrender.com/";

    /**
     * Construye y devuelve una instancia de Retrofit con timeouts extendidos.
     * Los tiempos son altos porque el servidor puede tardar en responder
     * al estar alojado en un servicio gratuito que entra en reposo.
     * @param baseUrl URL base del servidor al que apuntar.
     */
    public static Retrofit getClient(String baseUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Devuelve una instancia del servicio de resúmenes lista para hacer llamadas a la API.
     */
    public static ResumenService getResumenService() {
        return getClient(BASE_URL).create(ResumenService.class);
    }

    /**
     * Devuelve una instancia del servicio de apuntes lista para hacer llamadas a la API.
     */
    public static ApunteService getApunteService() {
        return getClient(BASE_URL).create(ApunteService.class);
    }

    /**
     * Devuelve una instancia del servicio de tests lista para hacer llamadas a la API.
     */
    public static TestService getTestService() {
        return getClient(BASE_URL).create(TestService.class);
    }
}
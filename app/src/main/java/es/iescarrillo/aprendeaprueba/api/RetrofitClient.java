package es.iescarrillo.aprendeaprueba.api;

import es.iescarrillo.aprendeaprueba.services.ApunteService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // CAMBIA ESTA URL por la de tu servidor (ej: http://10.0.2.2:8080/ si es local)
    private static final String BASE_URL = "http://TU_IP_O_DOMINIO/api/";
    private static Retrofit retrofit = null;

    public static ApunteService getApunteService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApunteService.class);
    }
}
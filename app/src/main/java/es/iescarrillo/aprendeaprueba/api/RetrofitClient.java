package es.iescarrillo.aprendeaprueba.api;

import es.iescarrillo.aprendeaprueba.services.ApunteService;
import es.iescarrillo.aprendeaprueba.services.ImgBBService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api-aprende-aprueba.onrender.com/";

    private static Retrofit getClient(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
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
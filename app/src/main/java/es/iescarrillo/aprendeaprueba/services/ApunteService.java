package es.iescarrillo.aprendeaprueba.services;

import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Interfaz Retrofit que define los endpoints del servicio de digitalización de apuntes.
 * Permite enviar texto plano o archivos al backend para su procesamiento.
 */
public interface ApunteService {

    /**
     * Envía un apunte en formato texto al backend para digitalizarlo.
     * El payload es un mapa clave-valor con los datos del apunte (ej: "contenido", "titulo").
     *
     * @param payload Mapa con los campos del apunte a digitalizar
     * @return Call con la respuesta del servidor como mapa clave-valor
     */
    @POST("api/apuntes/digitalizar")
    Call<Map<String, String>> digitalizarApunte(@Body Map<String, String> payload);

    /**
     * Envía un archivo (imagen) al backend para digitalizarlo mediante multipart.
     * Usado cuando el apunte proviene de un fichero en lugar de texto escrito.
     *
     * @param archivo Archivo a subir empaquetado como MultipartBody.Part
     * @return Call con la respuesta cruda del servidor (ResponseBody)
     */
    @Multipart
    @POST("api/apuntes/digitalizar-archivo")
    Call<ResponseBody> digitalizarArchivo(@Part MultipartBody.Part archivo);
}
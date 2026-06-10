package es.iescarrillo.aprendeaprueba.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.gson.annotations.SerializedName;

@IgnoreExtraProperties
public class Apuntes {

    private String id;
    private String titulo;
    private String descripcion;
    private String contenido;
    private String estado;

    @SerializedName("userId")
    private String userId;

    private String categoria;

    @SerializedName("url")
    private String imagenUrl;

    public Apuntes() {
    }

    public Apuntes(String id, String titulo, String descripcion, String contenido) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contenido = contenido;
    }

    public Apuntes(String id, String titulo, String descripcion, String contenido, String userId, String categoria, String imagenUrl) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contenido = contenido;
        this.userId = userId;
        this.categoria = categoria;
        this.imagenUrl = imagenUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @PropertyName("userId")
    public String getUserId() { return userId; }

    @PropertyName("userId")
    public void setUserId(String userId) { this.userId = userId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @PropertyName("url")
    public String getImagenUrl() { return imagenUrl; }

    @PropertyName("url")
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}

package es.iescarrillo.aprendeaprueba.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.gson.annotations.SerializedName;

/**
 * Modelo que representa un apunte creado por el usuario.
 * La anotación @IgnoreExtraProperties evita errores si Firebase
 * devuelve campos que no están definidos en esta clase.
 */
@IgnoreExtraProperties
public class Apuntes {

    private String id;
    private String titulo;
    private String descripcion;
    private String contenido;
    private String estado;

    /** Se serializa como "userId" tanto en Gson como en Firebase. */
    @SerializedName("userId")
    private String userId;

    private String categoria;

    /** Se serializa como "url" tanto en Gson como en Firebase. */
    @SerializedName("url")
    private String imagenUrl;

    /** Constructor vacío requerido por Firebase para deserializar objetos. */
    public Apuntes() {}

    /**
     * Constructor básico con los campos mínimos de un apunte.
     *
     * @param id         Identificador único del apunte
     * @param titulo     Título del apunte
     * @param descripcion Breve descripción del contenido
     * @param contenido  Texto completo del apunte
     */
    public Apuntes(String id, String titulo, String descripcion, String contenido) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contenido = contenido;
    }

    /**
     * Constructor completo con todos los campos del apunte.
     *
     * @param id         Identificador único del apunte
     * @param titulo     Título del apunte
     * @param descripcion Breve descripción del contenido
     * @param contenido  Texto completo del apunte
     * @param userId     UID del usuario propietario
     * @param categoria  Categoría a la que pertenece el apunte
     * @param imagenUrl  URL de la imagen asociada al apunte
     */
    public Apuntes(String id, String titulo, String descripcion, String contenido,
                   String userId, String categoria, String imagenUrl) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contenido = contenido;
        this.userId = userId;
        this.categoria = categoria;
        this.imagenUrl = imagenUrl;
    }

    // ---- Getters y Setters ----

    /** @return ID único del apunte en Firebase */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /** @return Título del apunte */
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /** @return Descripción breve del apunte */
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return Contenido completo del apunte */
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    /** @return Estado del apunte (ej: "borrador", "publicado") */
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * @return UID del usuario propietario del apunte.
     * @PropertyName indica a Firebase que use la clave "userId" en la base de datos.
     */
    @PropertyName("userId")
    public String getUserId() { return userId; }

    @PropertyName("userId")
    public void setUserId(String userId) { this.userId = userId; }

    /** @return Categoría del apunte */
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    /**
     * @return URL de la imagen asociada al apunte.
     * @PropertyName indica a Firebase que use la clave "url" en la base de datos.
     */
    @PropertyName("url")
    public String getImagenUrl() { return imagenUrl; }

    @PropertyName("url")
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
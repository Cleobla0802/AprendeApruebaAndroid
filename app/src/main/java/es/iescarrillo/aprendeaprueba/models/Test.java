package es.iescarrillo.aprendeaprueba.models;

import java.io.Serializable;
import java.util.List;

/**
 * Modelo que representa un test de preguntas generado a partir de un material
 * (apunte o resumen). Implementa Serializable para poder pasarlo entre fragmentos
 * mediante Bundle.
 */
public class Test implements Serializable {

    private String id;
    private String userId;
    private String titulo;
    private String descripcion;
    private String categoria;

    /** Fecha de creación almacenada como timestamp en milisegundos (epoch). */
    private long fecha;

    /** Lista de preguntas que componen el test. */
    private List<Pregunta> preguntas;

    /** Estado del test (ej: "generado", "pendiente"). */
    private String estado;

    /** Número de preguntas que tiene el test. Puede diferir de preguntas.size() si aún no se han cargado. */
    private int cantidadPreguntas;

    /** ID del apunte o resumen a partir del cual se generó el test. */
    private String materialId;

    /** Tipo del material origen del test (ej: "apunte", "resumen"). */
    private String materialTipo;

    /**
     * Hash del contenido del material original. Permite detectar si el material
     * ha cambiado desde que se generó el test.
     */
    private String contenidoHash;

    /** Indica si el usuario ya ha realizado el test al menos una vez. */
    private boolean completado;

    /** Última puntuación obtenida expresada en porcentaje (0-100). */
    private int ultimaNota;

    /** Última calificación obtenida sobre 10 (ej: 7.5). */
    private Double calificacion;

    /** Constructor vacío requerido por Firebase para deserializar objetos. */
    public Test() {}

    // ---- Getters y Setters ----

    /** @return ID único del test en Firebase */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /** @return UID del usuario propietario del test */
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    /** @return Título del test */
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /** @return Descripción breve del test */
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return Categoría a la que pertenece el test */
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    /** @return Timestamp en milisegundos de cuando se creó el test */
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    /** @return Lista de objetos Pregunta que forman el test */
    public List<Pregunta> getPreguntas() { return preguntas; }
    public void setPreguntas(List<Pregunta> preguntas) { this.preguntas = preguntas; }

    /** @return Estado actual del test */
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    /** @return Número total de preguntas del test */
    public int getCantidadPreguntas() { return cantidadPreguntas; }
    public void setCantidadPreguntas(int cantidadPreguntas) { this.cantidadPreguntas = cantidadPreguntas; }

    /** @return ID del material (apunte o resumen) usado para generar el test */
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }

    /** @return Tipo del material origen ("apunte" o "resumen") */
    public String getMaterialTipo() { return materialTipo; }
    public void setMaterialTipo(String materialTipo) { this.materialTipo = materialTipo; }

    /** @return Hash del contenido del material para detectar cambios posteriores */
    public String getContenidoHash() { return contenidoHash; }
    public void setContenidoHash(String contenidoHash) { this.contenidoHash = contenidoHash; }

    /** @return true si el usuario ya ha completado el test al menos una vez */
    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }

    /** @return Última nota obtenida en porcentaje (0-100) */
    public int getUltimaNota() { return ultimaNota; }
    public void setUltimaNota(int ultimaNota) { this.ultimaNota = ultimaNota; }

    /** @return Última calificación obtenida sobre 10 */
    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }
}
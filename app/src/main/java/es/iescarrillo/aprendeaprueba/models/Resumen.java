package es.iescarrillo.aprendeaprueba.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modelo que representa un resumen generado a partir de un apunte.
 * Implementa Serializable para poder pasarlo entre fragmentos mediante Bundle.
 */
public class Resumen implements java.io.Serializable {

    private String id;
    private String userId;
    private String titulo;
    private String descripcion;
    private String resumenTexto;
    private String estado;

    /** Fecha de creación almacenada como timestamp en milisegundos (epoch). */
    private long fecha;

    private String categoria;

    /** ID del apunte original a partir del cual se generó este resumen. */
    private String idApunteOriginal;

    /**
     * Hash del contenido del apunte original. Permite detectar si el apunte
     * ha cambiado desde que se generó el resumen.
     */
    private String contenidoHash;

    /** Constructor vacío requerido por Firebase para deserializar objetos. */
    public Resumen() {}

    // ---- Getters y Setters ----

    /** @return ID único del resumen en Firebase */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /** @return UID del usuario propietario del resumen */
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    /** @return Título del resumen */
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /** @return Descripción breve del resumen */
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return Timestamp en milisegundos de cuando se creó el resumen */
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    /** @return Categoría a la que pertenece el resumen */
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    /** @return Texto completo del resumen generado */
    public String getResumenTexto() { return resumenTexto; }
    public void setResumenTexto(String resumenTexto) { this.resumenTexto = resumenTexto; }

    /** @return Estado del resumen (ej: "generado", "pendiente") */
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    /** @return ID del apunte original vinculado a este resumen */
    public String getIdApunteOriginal() { return idApunteOriginal; }
    public void setIdApunteOriginal(String idApunteOriginal) { this.idApunteOriginal = idApunteOriginal; }

    /** @return Hash del contenido del apunte para detectar cambios posteriores */
    public String getContenidoHash() { return contenidoHash; }
    public void setContenidoHash(String contenidoHash) { this.contenidoHash = contenidoHash; }

    /**
     * Convierte el timestamp de la fecha a un formato legible "dd/MM/yyyy".
     * Si la fecha no está definida (valor 0), devuelve una cadena vacía.
     *
     * @return Fecha formateada como String, o "" si no hay fecha
     */
    public String getFechaFormateada() {
        if (fecha == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(fecha));
    }
}
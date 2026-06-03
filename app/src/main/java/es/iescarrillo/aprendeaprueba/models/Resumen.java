package es.iescarrillo.aprendeaprueba.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Resumen implements java.io.Serializable {
    private String id;
    private String userId;
    private String titulo;
    private String descripcion;
    private String resumenTexto;
    private String estado;
    private long fecha;
    private String categoria;
    private String idApunteOriginal;
    private String contenidoHash;

    public Resumen() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getResumenTexto() { return resumenTexto; }
    public void setResumenTexto(String resumenTexto) { this.resumenTexto = resumenTexto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getIdApunteOriginal() { return idApunteOriginal; }
    public void setIdApunteOriginal(String idApunteOriginal) { this.idApunteOriginal = idApunteOriginal; }
    public String getContenidoHash() { return contenidoHash; }
    public void setContenidoHash(String contenidoHash) { this.contenidoHash = contenidoHash; }

    public String getFechaFormateada() {
        if (fecha == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(fecha));
    }
}

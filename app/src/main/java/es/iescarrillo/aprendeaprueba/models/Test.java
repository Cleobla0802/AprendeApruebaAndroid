package es.iescarrillo.aprendeaprueba.models;

import java.io.Serializable;
import java.util.List;

public class Test implements Serializable {
    private String id;
    private String userId;
    private String titulo;
    private String descripcion;
    private String categoria;
    private long fecha;
    private List<Pregunta> preguntas;
    private String estado;
    private int cantidadPreguntas;
    private String materialId;
    private String materialTipo;
    private String contenidoHash;
    private boolean completado;
    private int ultimaNota;
    private Double calificacion;

    public Test() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
    public List<Pregunta> getPreguntas() { return preguntas; }
    public void setPreguntas(List<Pregunta> preguntas) { this.preguntas = preguntas; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getCantidadPreguntas() { return cantidadPreguntas; }
    public void setCantidadPreguntas(int cantidadPreguntas) { this.cantidadPreguntas = cantidadPreguntas; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public String getMaterialTipo() { return materialTipo; }
    public void setMaterialTipo(String materialTipo) { this.materialTipo = materialTipo; }
    public String getContenidoHash() { return contenidoHash; }
    public void setContenidoHash(String contenidoHash) { this.contenidoHash = contenidoHash; }
    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }
    public int getUltimaNota() { return ultimaNota; }
    public void setUltimaNota(int ultimaNota) { this.ultimaNota = ultimaNota; }
    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }
}

package es.iescarrillo.aprendeaprueba.models;

import java.util.List;

/**
 * Modelo que representa una pregunta dentro de un test.
 * Contiene el enunciado, las opciones de respuesta y el índice de la opción correcta.
 */
public class Pregunta {

    private int id;
    private String enunciado;
    private List<String> opciones;

    /** Índice (0-3) de la opción correcta dentro de la lista de opciones. */
    private int respuestaCorrecta;

    /** Constructor vacío requerido por Firebase para deserializar objetos. */
    public Pregunta() {}

    // ---- Getters y Setters ----

    /** @return ID único de la pregunta */
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /** @return Texto de la pregunta que se muestra al usuario */
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    /** @return Lista de opciones de respuesta (A, B, C, D) */
    public List<String> getOpciones() { return opciones; }
    public void setOpciones(List<String> opciones) { this.opciones = opciones; }

    /** @return Índice de la opción correcta (0=A, 1=B, 2=C, 3=D) */
    public int getRespuestaCorrecta() { return respuestaCorrecta; }
    public void setRespuestaCorrecta(int respuestaCorrecta) { this.respuestaCorrecta = respuestaCorrecta; }
}
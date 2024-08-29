package com.example.testpython.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PytubeDTO {
    private String titulo;
    private Integer duracion;
    private Integer vistas;
    private String descripcion;
    private String autor;

    private String error;
}

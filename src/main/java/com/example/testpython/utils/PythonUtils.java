package com.example.testpython.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;

@Service
@Log
public class PythonUtils {
    @Value("${APP_PRODUCTION}")
    private String appProduction;

    private String pythonPath;
    private String pythonScriptsPath;

    @PostConstruct
    public void init() {
        this.pythonScriptsPath = "src/main/java/com/example/testpython/python/";

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            this.pythonPath = "venv/Scripts/python.exe";
        } else {
            this.pythonPath = "venv/bin/python";
        }
    }

    public String executeFunction(String scriptName, String functionName, Object variable) throws Exception {
        String result = "";
        if (!scriptName.endsWith(".py")) {
            scriptName += ".py";
        }

        // Crear un comando para ejecutar Python
        List<String> command = new ArrayList<>();
        command.add(this.pythonPath);
        command.add(this.pythonScriptsPath + scriptName);
        command.add(functionName); // Nombre de la función
        command.add(variable.toString()); // Argumento para la función

        // Configurar el proceso
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        // Iniciar el proceso
        Process process = processBuilder.start();

        // Leer la salida del proceso
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }
        }

        // Esperar a que el proceso termine y verificar el código de salida
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Error al ejecutar el script de Python");
        }

        return result.trim();
    }

    public String convertParamsToListOnPython(List<Object> params) {
        StringBuilder result = new StringBuilder("[");
        for (Object param : params) {
            // Convertir todos los elementos a cadenas de texto y usar comillas simples
            result.append("'").append(param.toString()).append("'").append(", ");
        }
        // Eliminar la última coma y espacio adicional
        if (result.length() > 1) {
            result.setLength(result.length() - 2);
        }
        result.append("]");
        return result.toString();
    }

    public <T> T convertToDTO(String jsonString, Class<T> dtoClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        T dto = null;

        try {
            // Reemplaza valores de Python para que sean válidos en Java
            jsonString = jsonString.replaceAll("None", "null");
            jsonString = jsonString.replaceAll("True", "true");
            jsonString = jsonString.replaceAll("False", "false");
            jsonString = jsonString.replaceAll("'", "\"");

            // Verificar si la respuesta contiene un campo "error"
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            // Crear un DTO con el mensaje de error
            if (jsonNode.has("error")) {
                dto = dtoClass.getDeclaredConstructor().newInstance();
                dto.getClass().getMethod("setError", String.class).invoke(dto, jsonNode.get("error").asText());
            } else {
                // Intenta convertir el JSON a la clase DTO normalmente
                dto = objectMapper.readValue(jsonString, dtoClass);
            }

        } catch (IOException | InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            log.info("Error al convertir el JSON a DTO: " + e.getMessage());
            try {
                // Si hay un error en la deserialización, crear un DTO vacío
                dto = dtoClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException ex) {
                log.info("Error al crear una instancia del DTO: " + ex.getMessage());
            }
        }

        return dto;
    }

    public <T> List<T> convertToDTOList(String jsonString, Class<T> dtoClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<T> dtoList = null;
    
        try {
            // Reemplaza valores de Python para que sean válidos en Java
            jsonString = jsonString.replaceAll("None", "null");
            jsonString = jsonString.replaceAll("True", "true");
            jsonString = jsonString.replaceAll("False", "false");
            jsonString = jsonString.replaceAll("'", "\"");
    
            // Intenta convertir el JSON a una lista de objetos DTO
            dtoList = objectMapper.readValue(jsonString, objectMapper.getTypeFactory().constructCollectionType(List.class, dtoClass));
            
        } catch (IOException e) {
            log.info("Error al convertir el JSON a la lista de DTOs: " + e.getMessage());
            try {
                // Si hay un error en la deserialización, crear una lista vacía
                dtoList = List.of();
            } catch (Exception ex) {
                log.info("Error al crear una lista vacía: " + ex.getMessage());
            }
        }
    
        return dtoList;
    }
}

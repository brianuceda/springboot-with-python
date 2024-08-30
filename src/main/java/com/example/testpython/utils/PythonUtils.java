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
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;

@Service
@Log
public class PythonUtils {
    @Value("${APP_PRODUCTION}")
    private Boolean appProduction;

    private String pythonPath;
    private String pythonScriptsPath;

    // Se ejecuta de forma automática al crear una instancia de la clase PythonUtils
    @PostConstruct
    public void init() {
        // Establecer la ruta de los scripts de Python
        this.pythonScriptsPath = "src/main/java/com/example/testpython/python/";

        // Establecer la ruta del ejecutable de Python en Windows o Linux
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            this.pythonPath = "venv/Scripts/python.exe";
        } else {
            this.pythonPath = "venv/bin/python";
        }
    }
    
    public String executeFunction(String scriptName, String functionName) throws Exception {
        return this.executeFunction(scriptName, functionName, null);
    }


    // Ejecuta una función de un script de Python
    public String executeFunction(String scriptName, String functionName, Object params) throws Exception {
        String result = "";

        if (!scriptName.endsWith(".py")) {
            scriptName += ".py";
        }

        // Crea el comando a ejecutar en la terminal para llamar al script de Python
        List<String> command = new ArrayList<>();
        command.add(this.pythonPath); // Ruta del ejecutable de Python
        command.add(this.pythonScriptsPath + scriptName); // Ruta del script de Python
        command.add(functionName); // Nombre de la función a ejecutar

        // Si hay parámetros, los agrega al comando
        if (params != null) {
            // Argumento(s) de la función convertidos a cadena de texto: "[param1, param2, ...]"
            command.add(params.toString());
        }

        // Crea un proceso para ejecutar el comando creado
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        // Inicia el proceso de ejecución
        Process process = processBuilder.start();

        // Lee la salida del proceso línea por línea (resultado de la función de Python ejecutada)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }
        }

        // Espera a que el proceso termine y obtiene el código de salida        
        int exitCode = process.waitFor();

        // Lanza una excepción si el código de salida es diferente de 0 (ocurrió un error)
        if (exitCode != 0) {
            throw new Exception("Error al ejecutar el script de Python");
        }

        // Devuelve el resultado de la función de Python
        return result.trim();
    }

    // Convierte una lista de parámetros de Java a una lista de parámetros soportada por Python
    public String convertParamsToListOnPython(List<Object> params) {
        StringBuilder result = new StringBuilder("[");

        // Convierte cada parámetro a una cadena de texto y los agrega a la lista
        for (Object param : params) {
            result.append("'").append(param.toString()).append("'").append(", ");
        }

        // Elimina la última coma y espacio
        if (result.length() > 1) {
            result.setLength(result.length() - 2);
        }

        // Cierra la lista
        result.append("]");

        // Devuelve la lista de parámetros en formato de cadena de texto
        return result.toString();
    }

    // Convierte un JSON (de Python) a un DTO (de Java)
    // # Leer las instrucciones sobre cómo mapear los campos de Python a Java indicados en el archivo README.md
    public <T> T convertToDTO(String jsonString, Class<T> dtoClass) {
        T dto = null;
        ObjectMapper objectMapper = new ObjectMapper();
        // Configura el ObjectMapper para usar la estrategia de nombres de snake_case
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        try {
            jsonString = this.replacePythonValues(jsonString);

            JsonNode jsonNode = objectMapper.readTree(jsonString);

            // Verifica si la respuesta contiene un campo "error"
            if (jsonNode.has("error")) {
                // Crear un DTO con el mensaje de error
                dto = dtoClass.getDeclaredConstructor().newInstance();
                dto.getClass().getMethod("setError", String.class).invoke(dto, jsonNode.get("error").asText());
            } else {
                // Intenta convertir el JSON a la clase DTO normalmente
                dto = objectMapper.readValue(jsonString, dtoClass);
            }

        } catch (IOException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // Si hay un error en la deserialización, crear un DTO vacío
            try {
                dto = dtoClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                // El log solo está disponible en el entorno de desarrollo (no en producción)
                if (this.appProduction == false) {
                    log.info("Error al crear una instancia del DTO: " + ex.getMessage());
                } else {
                    System.out.println("Error al crear una instancia del DTO: " + ex.getMessage());
                }
            }
        }

        return dto;
    }

    public <T> List<T> convertToDTOList(String jsonString, Class<T> dtoClass) {
        List<T> dtoList = null;
        ObjectMapper objectMapper = new ObjectMapper();
        // Configura el ObjectMapper para usar la estrategia de nombres de snake_case
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    
        try {
            jsonString = this.replacePythonValues(jsonString);
    
            // Intenta convertir el JSON a una lista de objetos DTO
            dtoList = objectMapper.readValue(jsonString, objectMapper.getTypeFactory().constructCollectionType(List.class, dtoClass));
            
        } catch (IOException e) {
            // Si hay un error en la deserialización, crear una lista vacía
            try {
                dtoList = List.of();
            } catch (Exception ex) {
                // El log solo está disponible en el entorno de desarrollo (no en producción)
                if (this.appProduction == false) {
                    log.info("Error al crear una lista de DTOs: " + ex.getMessage());
                } else {
                    System.out.println("Error al crear una lista de DTOs: " + ex.getMessage());
                }
            }
        }
    
        return dtoList;
    }

    // Reemplaza valores de Python para que sean válidos en Java
    private String replacePythonValues(String jsonString) {
        jsonString = jsonString.replaceAll("None", "null");
        jsonString = jsonString.replaceAll("True", "true");
        jsonString = jsonString.replaceAll("False", "false");

        return jsonString;
    }
}

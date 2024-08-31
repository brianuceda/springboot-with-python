package com.example.testpython.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;

@Component
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
    
    public String execute(String scriptName, String functionName) throws Exception {
        return this.execute(scriptName, functionName, null, null);
    }
    
    public String execute(String scriptName, String functionName, Object params) throws Exception {
        return this.execute(scriptName, functionName, params, null);
    }

    // Verifica el tipo de dato de Object
    @SuppressWarnings("unchecked")
    public String execute(String scriptName, String functionName, Object params, MultipartFile file) throws Exception {

        // Agrega la extensión ".py" al nombre del script si no la tiene
        if (!scriptName.endsWith(".py")) {
            scriptName += ".py";
        }
        
        // Lista
        if (params instanceof List) {
            params = this.convertParamsToListOnPython((List<Object>) params);
        }
        
        // Imagen
        if (file != null) {
            return this.executeWithImage(scriptName, functionName, params, file.getBytes());
        }
        if (params instanceof MultipartFile) {
            return this.executeWithImage(scriptName, functionName, null, ((MultipartFile) params).getBytes());
        }
        
        // Cualquier otro tipo de dato
        return this.executeWithPrimitive(scriptName, functionName, params);
    }

    // Ejecuta una función de un script de Python
    public String executeWithPrimitive(String scriptName, String functionName, Object params) throws Exception {
        String result = "";

        // Crea el comando a ejecutar en la terminal para llamar al script de Python
        List<String> command = new ArrayList<>();
        command.add(this.pythonPath); // Ejecutable de Python
        command.add(this.pythonScriptsPath + "__main__.py"); // Archivo principal
        command.add(scriptName); // Nombre del script de Python a importar
        command.add(functionName); // Nombre de la función a ejecutar

        // Si hay parámetros, los agrega al comando
        if (params != null) {
            command.add(params.toString());
        } else {
            command.add("None");
        }

        // Crea e inicia un proceso para ejecutar el comando creado
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // Lee la salida del proceso línea por línea (resultado de la función de Python ejecutada)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }
        }

        // Lanza una excepción si el proceso no termina correctamente
        if (process.waitFor() != 0) {
            throw new Exception("Error al ejecutar el script de Python");
        }

        return result.trim();
    }
    
    // Ejecuta una función de un script de Python que recibe una imagen como parámetro
    public String executeWithImage(String scriptName, String functionName, Object params, byte[] imageBytes) throws Exception {
        String result = "";
        
        List<String> command = new ArrayList<>();
        command.add(this.pythonPath); // Ejecutable de Python
        command.add(this.pythonScriptsPath + "__main__.py"); // Archivo principal
        command.add(scriptName); // Nombre del script de Python a importar
        command.add(functionName); // Nombre de la función a ejecutar
        
        // Si hay parámetros, los agrega al comando
        if (params != null) {
            command.add(params.toString());
        } else {
            command.add("None");
        }

        log.info("Ejecutando script de Python: " + command.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Enviar datos de la imagen al proceso Python a través de stdin
        try (OutputStream os = process.getOutputStream()) {
            os.write(imageBytes);
            os.flush();
        }

        // Leer la salida del proceso Python
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Error al ejecutar el script de Python");
        }

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

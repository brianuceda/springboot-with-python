package com.example.testpython.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.testpython.dtos.ImageDTO;
import com.example.testpython.dtos.YoutubeSearchDTO;
import com.example.testpython.utils.PythonUtils;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class MainController {
    @Autowired
    private PythonUtils pythonUtils;

    @GetMapping("/function_a")
    public ResponseEntity<?> executeFunctionA(@RequestParam String variable) {
        return new ResponseEntity<>("Ejecutando funcion_a con el argumento: " + variable, HttpStatus.OK);
    }

    @GetMapping("/function_b")
    public ResponseEntity<?> executeFunctionB(@RequestParam String variable) {
        try {
            String response = pythonUtils.execute("test", "function_b", variable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search_youtube")
    public ResponseEntity<?> searchYoutube(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "1") Integer maxResults) {

        try {
            List<Object> javaParams = List.of(query, maxResults);
            String response = pythonUtils.execute("youtube", "search_youtube", javaParams);

            List<YoutubeSearchDTO> dto = pythonUtils.convertToDTOList(response, YoutubeSearchDTO.class);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("null")
    @PostMapping("/process_image")
    public ResponseEntity<?> processImage(@RequestParam(required = true) MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>(Map.of("error", "No se ha enviado ninguna imagen"), HttpStatus.BAD_REQUEST);
            }
            if (file.getContentType() != null && !file.getContentType().startsWith("image")) {
                return new ResponseEntity<>(Map.of("error", "El archivo no es una imagen"), HttpStatus.BAD_REQUEST);
            }

            String response = pythonUtils.execute("image.py", "process_image", file);
            ImageDTO dto = pythonUtils.convertToDTO(response, ImageDTO.class);

            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
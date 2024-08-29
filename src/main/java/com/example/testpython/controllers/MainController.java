package com.example.testpython.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.testpython.dtos.PytubeDTO;
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
            String response = pythonUtils.executeFunction("python", "funcion_b", variable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/watch")
    public ResponseEntity<?> watchYoutube(@RequestParam String v) {
        v = v.replace("https://www.youtube.com/watch?v=", "");

        try {
            String response = pythonUtils.executeFunction("python", "watch_youtube", v);
            PytubeDTO dto = pythonUtils.convertToDTO(response, PytubeDTO.class);

            return new ResponseEntity<>(dto, HttpStatus.OK);
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
            String pythonParams = pythonUtils.convertParamsToListOnPython(javaParams);

            String response = pythonUtils.executeFunction("python", "search_youtube", pythonParams);
            List<YoutubeSearchDTO> dto = pythonUtils.convertToDTOList(response, YoutubeSearchDTO.class);

            // Solo para este caso de videos de youtube
            for (YoutubeSearchDTO video : dto) {
                video.setUrlSuffix("https://www.youtube.com/" + video.getUrlSuffix());
            }

            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
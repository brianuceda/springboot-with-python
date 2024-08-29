package com.example.testpython.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YoutubeSearchDTO {
    private String id;
    private List<String> thumbnails;
    private String title;
    private String longDesc;
    private String channel;
    private String duration;
    private String views;
    private String publishTime;
    private String urlSuffix;
}

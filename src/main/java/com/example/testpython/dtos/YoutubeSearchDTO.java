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
    private String long_desc;
    private String channel;
    private String duration;
    private String views;
    private String publish_time;
    private String url_suffix;
}

package com.example.easynotes.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ImageDTO
{
    @JsonProperty("src")
    String src;

    @JsonProperty("alt")
    String alt;

    @JsonProperty("postLink")
    String postLink;

    public ImageDTO(String src, String alt, String postLink)
    {
        this.src = src;
        this.alt = alt;
        this.postLink = postLink;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageDTO imageDTO = (ImageDTO) o;
        return Objects.equals(src, imageDTO.src);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(src);
    }
}

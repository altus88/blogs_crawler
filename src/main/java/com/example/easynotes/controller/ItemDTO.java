package com.example.easynotes.controller;

import com.example.easynotes.model.Item;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ItemDTO
{
    @JsonProperty("id")
    private Long id;

    @JsonProperty("header")
    private String header;

    @JsonProperty("link")
    private String link;

    @JsonProperty("content")
    private String content;

    public ItemDTO(Item item)
    {
        this.header = item.getText();
        this.content = item.getContent().substring(0, 200) + " ... ";
        this.link = item.getUrl();
        this.id = item.getId();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDTO itemDTO = (ItemDTO) o;
        return Objects.equals(id, itemDTO.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}

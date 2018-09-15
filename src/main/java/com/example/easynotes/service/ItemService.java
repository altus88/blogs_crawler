package com.example.easynotes.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.example.easynotes.controller.ImageDTO;
import com.example.easynotes.controller.ItemDTO;
import com.example.easynotes.model.Item;
import com.example.easynotes.repository.ItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItemService
{
    @Autowired
    ItemRepository itemRepository;

    public List<ItemDTO> getItemsByText(String search)
    {
        search = " " + search.trim() + " "; // we want only words
        List<ItemDTO> foundByTextItemDTOS = itemRepository.getByTextContaining(search).stream().map(item -> new ItemDTO(item)).collect(Collectors.toList());
        Set<ItemDTO> foundByTextItemDTOSSet = new HashSet<>(foundByTextItemDTOS); // to find easy duplicates


        List<ItemDTO> foundByContentItemDTOs = itemRepository.getByContentContaining(search).stream().map(item -> new ItemDTO(item)).collect(Collectors.toList())
                .stream().filter(itemDTO -> !foundByTextItemDTOSSet.contains(itemDTO)).collect(Collectors.toList());
        foundByTextItemDTOS.addAll(foundByContentItemDTOs);
        return foundByTextItemDTOS;
    }

    public List<ImageDTO> getImagesByText(String search)
    {
        search = " " + search.trim() + " "; // we want only words

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        List<Item> itemsByText = itemRepository.getByTextContaining(search);
        List<ImageDTO> foundByTextImageDTOs = new ArrayList<>();
        for (Item item : itemsByText)
        {
            Images siteMap = gson.fromJson(item.getImages(), Images.class);
            for (ImageSrcAndAlt imageSrcAndAlt : siteMap.imageSrcAndAlt)
            {
                if (!StringUtils.isEmpty(imageSrcAndAlt.src))
                {
                    foundByTextImageDTOs.add(new ImageDTO(imageSrcAndAlt.src, imageSrcAndAlt.alt, item.getUrl()));
                }
            }
        }
        Set<ImageDTO> foundByTextImageDTOSSet = new HashSet<>(foundByTextImageDTOs); // to find easy duplicates

        List<Item> itemsByContent = itemRepository.getByContentContaining(search);

        for (Item item : itemsByContent)
        {
            Images siteMap = gson.fromJson(item.getImages(), Images.class);
            for (ImageSrcAndAlt imageSrcAndAlt : siteMap.imageSrcAndAlt)
            {
                if (!StringUtils.isEmpty(imageSrcAndAlt.src))
                {
                    ImageDTO imageDTO = new ImageDTO(imageSrcAndAlt.src, imageSrcAndAlt.alt, item.getUrl());
                    if (!foundByTextImageDTOSSet.contains(imageDTO)) // do not put repeated images
                    {
                        foundByTextImageDTOs.add(imageDTO);
                    }
                }
            }
        }
        return foundByTextImageDTOs;
    }
}

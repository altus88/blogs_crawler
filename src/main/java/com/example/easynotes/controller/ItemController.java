package com.example.easynotes.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.Item;
import com.example.easynotes.repository.ItemRepository;

/**
 * Created by anush on 23.08.18.
 */
@RestController
@RequestMapping("/api")
public class ItemController
{
	private final static Log LOG = LogFactory.getLog(ItemController.class);

	@Autowired
	ItemRepository itemRepository;


	@GetMapping("/items")
	public List<Item> getAllItems() {
		return itemRepository.findAll();
	}


	@GetMapping("/items/{id}")
	public Item getItemById(@PathVariable(value = "id") Long itemId) {

		return itemRepository.findById(itemId)
			.orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

	}

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/items/text/{search}")
	public List<Item> getItemByText(@PathVariable(value = "search") String search) {

		return itemRepository.getByTextContaining(search);

	}

}

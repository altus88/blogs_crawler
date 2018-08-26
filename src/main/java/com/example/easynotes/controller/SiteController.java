package com.example.easynotes.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import com.example.easynotes.model.Site;
import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.repository.SiteRepository;
import com.example.easynotes.service.ScraperService;

@RestController
@RequestMapping("/api")
public class SiteController
{

	private final static Log LOG = LogFactory.getLog(SiteController.class);

	@Autowired
	SiteRepository siteRepository;

	@Autowired
	ScraperService scraperService;

	// Get All Sites
	@GetMapping("/sites")
	public List<Site> getAllNotes() {
		return siteRepository.findAll();
	}
	// Create a new Site Schema
	@PostMapping("/sites")
	public Site createNote(@RequestBody Site site) {
		return siteRepository.save(site);
	}
	// Get a Single Site
	@GetMapping("/sites/{id}")
	public Site getSiteById(@PathVariable(value = "id") Long siteId) {

		scraperService.parseWebScraper(siteId);
		return siteRepository.findById(siteId)
			.orElseThrow(() -> new ResourceNotFoundException("Site", "id", siteId));
	}
	// Update a Site
	@PutMapping("/sites/{id}")
	public Site updateNote(@PathVariable(value = "id") Long siteId,
		@RequestBody Site siteDetails) {

		Site site = siteRepository.findById(siteId)
			.orElseThrow(() -> new ResourceNotFoundException("Site", "id", siteId));

		site.setUrl(siteDetails.getUrl());
		site.setWebScraperSchema(siteDetails.getWebScraperSchema());

		Site updatedSite = siteRepository.save(site);
		return updatedSite;
	}
	// Delete a Site
	@DeleteMapping("/sites/{id}")
	public ResponseEntity<?> deleteSite(@PathVariable(value = "id") Long siteId) {
		Site note = siteRepository.findById(siteId)
			.orElseThrow(() -> new ResourceNotFoundException("Site", "id", siteId));

		siteRepository.delete(note);

		return ResponseEntity.ok().build();
	}}
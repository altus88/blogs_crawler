package com.example.easynotes.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.Item;
import com.example.easynotes.model.Site;
import com.example.easynotes.repository.ItemRepository;
import com.example.easynotes.repository.SiteRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by anush on 18.07.18.
 */
@Service
public class ScraperService
{

	private final static Log LOG = LogFactory.getLog(ScraperService.class);

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private ItemRepository itemRepository;

	private static final String separator = "KeySeparator";
	private static final int timeout = 5000;


	public void parseWebScraper(Long siteId) throws JSONException
	{
		Site site = siteRepository.findById(siteId).orElseThrow(() -> new ResourceNotFoundException("Site", "id", siteId));

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		SiteMap siteMap = gson.fromJson(site.getWebScraperSchema(), SiteMap.class);
		Selector rootSelector = siteMap.buildTree();

		LOG.info("siteMap = \n" + rootSelector.toString(""));
		Map<String, Item> extractedItems = new HashMap<>();

		for (String url : siteMap.startUrl)
		{
			try
			{
				Document document = Jsoup.connect(url).timeout(timeout).get();
				for (Selector selector : rootSelector.children)
				{
					browseSite(document, selector, extractedItems, "root");
				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		LOG.info("extractedTexts = " + extractedItems.values());
		for (Item item : extractedItems.values())
		{
			itemRepository.save(item);
		}
	}

	
	private void browseSite(Element element, Selector selector, Map<String, Item> extractedTexts, String key) throws IOException
	{
		if (selector.type.equals(SelectorType.SelectorLink.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines)
			{
				String absUrl = headline.absUrl("href");
				Document childDoc = Jsoup.connect(absUrl).timeout(timeout).get();
				int i = 1;
				for (Selector childSelector : selector.children)
				{
					browseSite(childDoc, childSelector, extractedTexts, absUrl);

				}
			}
		}
		else if (selector.type.equals(SelectorType.SelectorElement.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines)
			{
				for (Selector childSelector : selector.children)
				{
					browseSite(headline, childSelector, extractedTexts, key + separator + headline);

				}
			}
		}
		else if (selector.type.equals(SelectorType.SelectorText.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines)
			{
				Item item = extractedTexts.get(key);
				if (item == null)
				{
					item = new Item();
					int separatorIndex = key.indexOf(separator);
					if (separatorIndex != -1)
					{
						item.setUrl(key.substring(0, separatorIndex));
					}
					else
					{
						item.setUrl(key);
					}
					extractedTexts.put(key, item);
				}
				item.setText(headline.text());
			}
		}
		else if (selector.type.equals(SelectorType.SelectorImage.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines)
			{
				Item item = extractedTexts.get(key);
				if (item == null)
				{
					item = new Item();
					item.setUrl(key.substring(0, key.indexOf(separator)));
					extractedTexts.put(key, item);
				}
				item.setImageUrl(headline.absUrl("src"));
			}
		}
	}

}

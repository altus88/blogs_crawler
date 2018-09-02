package com.example.easynotes.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.Item;
import com.example.easynotes.model.Site;
import com.example.easynotes.repository.ItemRepository;
import com.example.easynotes.repository.SiteRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.bonigarcia.wdm.ChromeDriverManager;

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
	private static final int requestTimeout = 60000;
	private static final int requestInterval = 2000;

	static WebDriver driver;
	static
	{
		ChromeDriverManager.getInstance().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		driver = new ChromeDriver(options);
	}

	public void parseWebScraper(Long siteId)
	{


		Site site = siteRepository.findById(siteId).orElseThrow(() -> new ResourceNotFoundException("Site", "id", siteId));

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		SiteMap siteMap = gson.fromJson(site.getWebScraperSchema(), SiteMap.class);
		Map<String, List<Selector>> selectorIdToChildren = siteMap.buildTree();

		LOG.info("siteMap = \n" + selectorIdToChildren.toString());
		Map<String, Item> extractedItems = new HashMap<>();

		for (String url : siteMap.startUrl)
		{
			try
			{
				Document document = requestDocument(url);
				for (Selector selector : selectorIdToChildren.get("_root"))
				{
					browseSite(document, selector, extractedItems, url, false, selectorIdToChildren);
				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		LOG.info("extractedTexts = " + extractedItems.values().size());
		for (Item item : extractedItems.values())
		{
			item.setSiteId(siteId);
			itemRepository.save(item);
		}
	}

	private Document requestDocument(String url) throws IOException, InterruptedException
	{
		driver.get(url);
		String pageSource = driver.getPageSource();
		Document document = Jsoup.parse(pageSource);
		Thread.sleep(requestInterval); // between subsequent requests, there should be a delay to avoid blocking
		return document;
	}

	private void browseSite(Element element, Selector selector, Map<String, Item> extractedTexts, String key, boolean insideElement, Map<String, List<Selector>> selectorIdToChildren) throws IOException, InterruptedException
	{
		if (selector.type.equals(SelectorType.SelectorLink.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines)
			{
				String absUrl = headline.absUrl("href");
				if (insideElement && selector.id.equals("Link"))
				{
					getItem(key, extractedTexts).setUrl(absUrl);
				}
				Document childDoc = requestDocument(absUrl);
				for (Selector childSelector : selectorIdToChildren.get(selector.id))
				{
					browseSite(childDoc, childSelector, extractedTexts, insideElement ? key: absUrl, insideElement, selectorIdToChildren);
				}
			}
		}
		else if (selector.type.equals(SelectorType.SelectorElement.name()))
		{
			Elements selectedElements = element.select(selector.selector);
			int size = selectedElements.size();
			LOG.info("Found elements: " + size);
			int i = 0;
			for (Element selectedElement : selectedElements)
			{
				LOG.info("Parse: " + ++i + "/" + size);
				for (Selector selectedElementDataSelector : selectorIdToChildren.get(selector.id))
				{
					browseSite(selectedElement, selectedElementDataSelector, extractedTexts, key + separator + selectedElement, true, selectorIdToChildren);
				}
			}
		}
		else if (selector.type.equals(SelectorType.SelectorText.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines)
			{
				Item item = getItem(key, extractedTexts);
				if (selector.id.equals("Intro"))
				{
					item.setIntro(headline.text());
				} else
				{
					item.setText(headline.text());
				}
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

	private Item getItem(String key,  Map<String, Item> keyToItem)
	{
		Item item = keyToItem.get(key);
		if (item == null)
		{
			item = new Item();
			keyToItem.put(key, item);
		}
		return item;
	}

}

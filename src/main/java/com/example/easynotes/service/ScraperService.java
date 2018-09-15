package com.example.easynotes.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.PageLoadStrategy;
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

	private WebDriver driver;


	public void extractAndSaveBlogText(Long siteId)
	{
		Site site = siteRepository.findById(siteId).get();
		String selectorString = site.getTextTag();
		List<Item> items = itemRepository.getBySiteId(siteId);

		int i = 0;
		int n = items.size();
		for (Item item : items)
		{
			StringBuilder text = new StringBuilder();
			Document content = Jsoup.parse(item.getIntro());
			Elements elements = content.select(selectorString);
			for (Element element : elements)
			{
				extractTest(element, text);
			}
			item.setContent(text.toString());
			itemRepository.save(item);
			if (++i % 20 == 0)
			{
				LOG.info("Processed items: " + i + "/" + n);
			}
		}
	}

	private void extractTest(Element element, StringBuilder text)
	{
		text.append(element.text());
		for (Element childElement : element.children())
		{
			extractTest(childElement, text);
		}
	}

	public void extractAndSaveImages(Long siteId)
	{
		Site site = siteRepository.findById(siteId).get();
		String imageSelector = site.getImageSelector();
		List<Item> items = itemRepository.getBySiteId(siteId);

		int i = 0;
		int n = items.size();
		for (Item item : items)
		{
			StringBuilder images = new StringBuilder();
			Document content = Jsoup.parse(item.getIntro());
			Elements imageElements = content.select(imageSelector);
			// keep in json format
			images.append("{\"imageSrcAndAlt\":").append("[");
			int elements = 0;
			for (Element imageElement : imageElements)
			{
				if (elements > 0)
				{
					images.append(",");
				}
				images.append("{").append("\"").append("src").append("\"").append(":");
				images.append("\"").append(imageElement.attr("src")).append("\"");
				images.append(",").append("\"").append("alt").append("\"").append(":");
				images.append("\"").append(imageElement.attr("alt")).append("\"").append("}");
				elements++;
			}
			images.append("]").append("}");
			item.setImages(images.toString());
			itemRepository.save(item);
			if (++i % 20 == 0)
			{
				LOG.info("Processed items: " + i + "/" + n);
			}
		}
	}

	public void parseWebScraper(Long siteId)
	{
		initializeDriver();
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
			try
			{
				itemRepository.save(item);
			} catch (Exception e)
			{
				System.out.println(e);
			}
		}
	}

	private Document requestDocument(String url) throws IOException, InterruptedException
	{
		try
		{
			driver.get(url);

			if (driver.getPageSource().contains("This site can’t be reached"))
			{
				System.out.println("Page not found. Wait and try again");
				Thread.sleep(5000);
				driver.get(url);
			}
		} catch (Exception ex)
		{
			System.out.println("Timeout happened. Try again ...");
			driver.get(url);
		}
		Thread.sleep(requestInterval);
		String pageSource = driver.getPageSource();
		Document document = Jsoup.parse(pageSource);
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
				Document childDoc = requestDocument(absUrl);

				if (insideElement && selector.id.equals("Link"))
				{
					getItem(key, extractedTexts).setUrl(absUrl);
					getItem(key, extractedTexts).setIntro(childDoc.toString());
				} else
				{
					for (Selector childSelector : selectorIdToChildren.get(selector.id))
					{
						browseSite(childDoc, childSelector, extractedTexts, insideElement ? key: absUrl, insideElement, selectorIdToChildren);
					}
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
				LOG.info("Elements: " + extractedTexts.keySet().size());
			}
		}
		else if (selector.type.equals(SelectorType.SelectorText.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			Item item = getItem(key, extractedTexts);
			for (Element headline : newsHeadlines)
			{
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

	private void initializeDriver()
	{
		ChromeDriverManager.getInstance().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		driver = new ChromeDriver(options);
		driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
	}

}

package com.example.easynotes.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.example.easynotes.model.Site;
import com.example.easynotes.repository.SiteRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

/**
 * Created by anush on 18.07.18.
 */
@Service
public class ScraperService
{

	private final static Log LOG = LogFactory.getLog(ScraperService.class);

	@Autowired
	private SiteRepository siteRepository;

	public void parseWebScraper(Long siteId) throws JSONException
	{
		Site site = siteRepository.findById(siteId)
		.orElseThrow(() -> new ResourceNotFoundException("Site", "id", siteId));


		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		SiteMap siteMap = gson.fromJson(site.getWebScraperSchema(), SiteMap.class);
		Selector rootSelector = siteMap.buildTree();

//		LOG.info("siteMap = \n"+rootSelector.toString(""));

		extractedTexts.clear();
//		for(Selector selector : siteMap.selectors)
//		{
			for (String url : siteMap.startUrl)
			{
				try
				{
					Document document = Jsoup.connect(url).get();
					for(Selector selector : rootSelector.children)
					{
						browseSite(document, selector);
					}

				}catch (Exception e){
					e.printStackTrace();;
				}
			}
//		}



		LOG.info("extractedTexts = "+extractedTexts);

/*
		JSONObject obj = new JSONObject(site.getWebScraperSchema());
		String pageName = obj.getString("startUrl");
		LOG.info("pageName = "+pageName);
		try
		{
			Document document = Jsoup.connect("http://www.thefashionspot.com/").get();
			JSONArray arr = obj.getJSONArray("selectors");
			for (int i = 0; i < arr.length(); i++)
			{
				JSONObject selector = arr.getJSONObject(i);
				String sel_id = selector.getString("id");
				String tagValue = selector.getString("selector");
				LOG.info("sel id = " + sel_id);
				LOG.info("tagValue = " + tagValue);
				Elements linksOnPage = document.select(tagValue);
				for (Element page : linksOnPage) {
					LOG.info("page = " + page);
				}

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
*/
	}


	List<String> extractedTexts = new ArrayList<String>();



	private void browseSite(Element element, Selector selector) throws IOException
	{
		if(selector.type.equals(SelectorType.SelectorLink.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines) {
//				LOG.info("headline headline.absUrl()= "+headline.absUrl("href"));
				String absUrl = headline.absUrl("href");
				Document childDoc = Jsoup.connect(absUrl).get();
				for(Selector childSelector : selector.children)
				{
					browseSite(childDoc, childSelector);
				}
			}
		}
		else if(selector.type.equals(SelectorType.SelectorElement.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines) {
				//				LOG.info("headline headline.absUrl()= "+headline.absUrl("href"));
				for(Selector childSelector : selector.children)
				{
					browseSite(headline, childSelector);
				}
			}
		}
		else if(selector.type.equals(SelectorType.SelectorText.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines) {
				//				LOG.info("headline="+headline);
				extractedTexts.add(headline.text());
				LOG.info("headline="+headline);
				LOG.info("headline.text="+headline.text());
				LOG.info("headline.html="+headline.html());
				LOG.info("headline.ownText="+headline.ownText());
			}
		}
		else if(selector.type.equals(SelectorType.SelectorImage.name()))
		{
			Elements newsHeadlines = element.select(selector.selector);
			for (Element headline : newsHeadlines) {
				//				LOG.info("headline="+headline);
				extractedTexts.add(headline.text());
				LOG.info("img="+headline);
				LOG.info("img.html="+headline.html());
				LOG.info("img.ownText="+headline.ownText());
			}
		}
	}

}

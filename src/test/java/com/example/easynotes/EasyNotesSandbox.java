package com.example.easynotes;

import com.example.easynotes.controller.ImageDTO;
import com.example.easynotes.model.Item;
import com.example.easynotes.model.Site;
import com.example.easynotes.repository.ItemRepository;
import com.example.easynotes.repository.SiteRepository;
import com.example.easynotes.service.ItemService;
import com.example.easynotes.service.ScraperService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EasyNotesApplication.class)
public class EasyNotesSandbox
{
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    ScraperService parseWebScraper;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemService itemService;

    @Test
    public void test()
    {
        List<ImageDTO> imageDTOS = itemService.getImagesByText("Paris");
        System.out.println(imageDTOS);
    }

    @Test
    public void saveSite()
    {
        Site site = new Site();
        site.setUrl("https://www.danflyingsolo.com/travel-blog/");
        site.setWebScraperSchema("{\"_id\":\"danflyingsolo\",\"startUrl\":[\"https://www.danflyingsolo.com/travel-blog/\"],\"selectors\":[{\"id\":\"Element\",\"type\":\"SelectorElement\",\"parentSelectors\":[\"_root\",\"Pagination\"],\"selector\":\"article.slide-entry\",\"multiple\":true,\"delay\":0},{\"id\":\"Image\",\"type\":\"SelectorImage\",\"parentSelectors\":[\"Element\"],\"selector\":\"img\",\"multiple\":false,\"delay\":0},{\"id\":\"Header\",\"type\":\"SelectorText\",\"parentSelectors\":[\"Element\"],\"selector\":\"h3.slide-entry-title a\",\"multiple\":false,\"regex\":\"\",\"delay\":0},{\"id\":\"Link\",\"type\":\"SelectorLink\",\"parentSelectors\":[\"Element\"],\"selector\":\"h3.slide-entry-title a\",\"multiple\":false,\"delay\":0},{\"id\":\"Pagination\",\"type\":\"SelectorLink\",\"parentSelectors\":[\"_root\",\"Pagination\"],\"selector\":\"nav.pagination a:contains('›')\",\"multiple\":false,\"delay\":0},{\"id\":\"Intro\",\"type\":\"SelectorText\",\"parentSelectors\":[\"Link\"],\"selector\":\"p.intro\",\"multiple\":false,\"regex\":\"\",\"delay\":0}]}");
        siteRepository.save(site);
    }

    @Test
    public void parseSchema()
    {
        parseWebScraper.parseWebScraper(1l);
    }

    @Test
    public void extractAndSaveBlogText()
    {
        parseWebScraper.extractAndSaveBlogText(1l);
    }

    @Test
    public void extractAndSaveImages()
    {
        parseWebScraper.extractAndSaveImages(1l);
    }
}

package com.example.easynotes;

import com.example.easynotes.model.Site;
import com.example.easynotes.repository.SiteRepository;
import com.example.easynotes.service.ScraperService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EasyNotesApplication.class)
public class EasyNotesSandbox
{
    @Autowired
    SiteRepository siteRepository;

    @Autowired
    ScraperService parseWebScraper;

    @Test
    public void saveSite()
    {
        Site site = new Site();
        site.setUrl("https://www.danflyingsolo.com/travel-blog/");
        site.setWebScraperSchema("[{\"id\":\"Element\",\"type\":\"SelectorElement\",\"parentSelectors\":[\"_root\",\"Pagination\"],\"selector\":\"article.slide-entry\",\"multiple\":true,\"delay\":0},{\"id\":\"Image\",\"type\":\"SelectorImage\",\"parentSelectors\":[\"Element\"],\"selector\":\"img\",\"multiple\":false,\"delay\":0},{\"id\":\"Header\",\"type\":\"SelectorText\",\"parentSelectors\":[\"Element\"],\"selector\":\"h3.slide-entry-title a\",\"multiple\":false,\"regex\":\"\",\"delay\":0},{\"id\":\"Link\",\"type\":\"SelectorLink\",\"parentSelectors\":[\"Element\"],\"selector\":\"h3.slide-entry-title a\",\"multiple\":false,\"delay\":0},{\"id\":\"Pagination\",\"type\":\"SelectorLink\",\"parentSelectors\":[\"_root\",\"Pagination\"],\"selector\":\"nav.pagination a:contains('›')\",\"multiple\":false,\"delay\":0}]");
        siteRepository.save(site);
    }

    @Test
    public void parseSchema()
    {
        parseWebScraper.parseWebScraper(1l);
    }
}

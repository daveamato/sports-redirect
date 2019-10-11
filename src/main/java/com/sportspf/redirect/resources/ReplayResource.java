package com.sportspf.redirect.resources;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.sportspf.redirect.cache.ReplayCache.NBA_CACHE;

//@CrossOrigin(origins = {
//        "http://www.cominstream.com",
//        "http://cominstream.com",
//        "http://www.dofustream.com",
//        "http://dofustream.com"
//})
@RestController
@RequestMapping(value = "/api/replay")
public class ReplayResource {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36";

    @GetMapping(value = "/watchraw/get-link")
    public Map<String, Map<String, String>> getLink(@RequestParam(value = "url") String url) throws IOException {
        Map<String, Map<String, String>> result = (Map<String, Map<String, String>>) NBA_CACHE.get(url);
        if (MapUtils.isNotEmpty(result)) {
            return result;
        } else {
            result = new LinkedHashMap<>();
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select("div.container div.card ul li div.les-content");
            String server;
            Map<String, String> linkMap;
            Elements aEls;
            int index = 1;
            for (Element element : elements) {
                try {
                    server = element.selectFirst("i").text().trim();
                    linkMap = new LinkedHashMap<>();
                    aEls = element.select("a");
                    if (aEls == null || aEls.size() == 0) {
                        continue;
                    }
                    for (Element aEl : aEls) {
                        linkMap.put(aEl.text().trim(), aEl.attr("link"));
                    }
                    if (MapUtils.isNotEmpty(linkMap)) {
                        result.put(server + " " + index, linkMap);
                        index++;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            NBA_CACHE.put(url, result);
            return result;
        }
    }

    @GetMapping(value = "/watchraw")
    public List<String> getUrl(@RequestParam(value = "data") String data) throws IOException {
        if (NBA_CACHE.get(data) != null) {
            return (List<String>) NBA_CACHE.get(data);
        } else {
            HttpGet httpGet = new HttpGet("http://watchraw.com/ajax?data=" + data);
            httpGet.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
            httpGet.setHeader("Referer", "http://watchraw.com/");
            String html = IOUtils.toString(HttpClientBuilder.create().build().execute(httpGet).getEntity().getContent(), "UTF-8");
            List<String> result = new ArrayList<>();
            String source = "";
            int from = html.indexOf("\"file\": \"") + 9, to;
            if (from > 9) {
                to = html.indexOf("\"", from);
                source = html.substring(from, to);
                if (source.startsWith("http")) {
                    result.add(source);
                }
            }
            from = html.indexOf("<iframe src=\"") + 13;
            if (from > 13) {
                to = html.indexOf("\"", from);
                source = html.substring(from, to);
                if (source.startsWith("http")) {
                    result.add(source);
                }
            }
            NBA_CACHE.put(data, result);
            return result;
        }
    }
}

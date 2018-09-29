package com.sportspf.redirect.resources;

import com.sportspf.redirect.cache.M3U8Cache;
import com.sportspf.redirect.dtos.ResponseDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@CrossOrigin(origins = {
        "http://www.cominstream.com",
        "http://cominstream.com",
        "http://www.dofustream.com",
        "http://dofustream.com"
})
@RestController
@RequestMapping(value = "/api/nfl")
public class NFLKeyResource {
    private final RestTemplate restTemplate;

    public NFLKeyResource(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping(value = "/m3u8/**")
    public void getM3u8(HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(14);
        url = url.substring(url.indexOf("http"));
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        String baseUrl = url.substring(0, url.lastIndexOf("/") + 1);
        String queryString = request.getQueryString();
        if (queryString != null) {
            url += "?" + queryString;
        }
        ResponseDTO responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
        if (!(responseDto != null
                && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(10)) > 0)) {
            Boolean check = M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.get(url);
            while (check != null) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    // ignore
                }
                check = M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.get(url);
                if (check == null) {
                    responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
                    if (responseDto != null) {
                        response.getWriter().write(responseDto.getResponse());
                    }
                    response.flushBuffer();
                    return;
                }
            }
            M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.put(url, true);
            responseDto = new ResponseDTO();
            String m3u8 = restTemplate.getForObject(url, String.class);
            if (!fileName.contains("key")) {
                int from = m3u8.indexOf("AES-128,URI=\"") + 13;
                m3u8 = m3u8.substring(0, from) + baseUrl + m3u8.substring(from);
                StringBuilder res = new StringBuilder();
                String m3u8Lines[] = m3u8.split("\n");
                for (String line : m3u8Lines) {
                    if (line.contains(".ts")) {
                        res.append(baseUrl).append(line).append("\n");
                    } else {
                        res.append(line).append("\n");
                    }
                }
                responseDto.setResponse(res.toString());
            } else {
                responseDto.setResponse(m3u8);
            }
            responseDto.setDownloadedAt(LocalDateTime.now());
            M3U8Cache.M3U8_RESPONSE_CACHE.put(url, responseDto);
            M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.remove(url);
        }
        response.getWriter().write(responseDto.getResponse());
        response.flushBuffer();
    }
}

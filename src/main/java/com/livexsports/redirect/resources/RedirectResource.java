package com.livexsports.redirect.resources;

import com.livexsports.redirect.cache.KeyFileCache;
import com.livexsports.redirect.cache.M3U8Cache;
import com.livexsports.redirect.cache.RedirectFileCache;
import com.livexsports.redirect.dtos.RedirectFileDTO;
import com.livexsports.redirect.dtos.ResponseDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {
        "http://www.xfasports.com",
        "http://xfasports.com",
        "http://www.sportspf.com",
        "http://sportspf.com"
})
@RestController
@RequestMapping(value = "/api/redirect")
public class RedirectResource {
    private final RestTemplate restTemplate;
    private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";

    @Autowired
    public RedirectResource(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping(value = "/withBaseUrl/**")
    public void m3u8RedirectWithBaseUrl(HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(26);
        String queryString = request.getQueryString();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        String baseUrl = url.substring(0, url.lastIndexOf("/") + 1);
        if (queryString != null) {
            url += "?" + queryString;
        }
        if (fileName.contains(".m3u8")) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
            String m3u8 = IOUtils.toString(HTTP_CLIENT.execute(httpGet).getEntity().getContent(), "UTF-8");
            StringBuilder res = new StringBuilder();
            String m3u8Lines[] = m3u8.split("\n");
            for (String line : m3u8Lines) {
                if (line.contains(".ts")) {
                    res.append(baseUrl).append(line).append("\n");
                } else {
                    res.append(line).append("\n");
                }
            }
            ResponseDTO m3U8ResponseDTO = M3U8Cache.getM3u8ResponseCache().get(url);
            if (!(m3U8ResponseDTO != null && m3U8ResponseDTO.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(9)) > 0)) {
                m3U8ResponseDTO = new ResponseDTO();
                m3U8ResponseDTO.setResponse(res.toString());
                m3U8ResponseDTO.setDownloadedAt(LocalDateTime.now());
                M3U8Cache.getM3u8ResponseCache().put(url, m3U8ResponseDTO);
            }
            response.getWriter().write(m3U8ResponseDTO.getResponse());
        }
        response.flushBuffer();
    }

    @GetMapping(value = "/withTimer/{timer}/**")
    public void redirectWithTimer(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable(value = "timer") Long timer) throws IOException {
        String url = request.getRequestURI().substring(27);
        String queryString = request.getQueryString();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (queryString != null) {
            url += "?" + queryString;
        }
        if (fileName.contains(".m3u8")) {
            ResponseDTO responseDto = M3U8Cache.getM3u8ResponseCache().get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(timer)) > 0)) {
                String m3u8 = restTemplate.getForObject(url, String.class);
                responseDto = new ResponseDTO();
                responseDto.setResponse(m3u8);
                responseDto.setDownloadedAt(LocalDateTime.now());
                M3U8Cache.getM3u8ResponseCache().put(url, responseDto);
            }
            response.getWriter().write(responseDto.getResponse());
        } else if (fileName.contains(".file")) {
            ResponseDTO responseDto = KeyFileCache.getKeyFileResponseCache().get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusMinutes(3)) > 0)) {
                String key = restTemplate.getForObject(url, String.class);
                System.out.println(key);
                responseDto = new ResponseDTO();
                responseDto.setResponse(key);
                responseDto.setDownloadedAt(LocalDateTime.now());
                KeyFileCache.getKeyFileResponseCache().put(url, responseDto);
            }
            response.getWriter().write(responseDto.getResponse());
        }
        response.flushBuffer();
    }

    @GetMapping(value = "/**")
    public void redirect(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(14);
        String queryString = request.getQueryString();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (queryString != null) {
            url += "?" + queryString;
        }
        if (fileName.contains(".m3u8")) {
            ResponseDTO responseDto = M3U8Cache.getM3u8ResponseCache().get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(9)) > 0)) {
                String m3u8 = restTemplate.getForObject(url, String.class);
                responseDto = new ResponseDTO();
                responseDto.setResponse(m3u8);
                responseDto.setDownloadedAt(LocalDateTime.now());
                M3U8Cache.getM3u8ResponseCache().put(url, responseDto);
            }
            response.getWriter().write(responseDto.getResponse());
        } else if (fileName.contains(".file") || url.contains("keys")) {
            ResponseDTO responseDto = KeyFileCache.getKeyFileResponseCache().get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusMinutes(3)) > 0)) {
                String key = restTemplate.getForObject(url, String.class);
                System.out.println(key);
                responseDto = new ResponseDTO();
                responseDto.setResponse(key);
                responseDto.setDownloadedAt(LocalDateTime.now());
                KeyFileCache.getKeyFileResponseCache().put(url, responseDto);
            }
            response.getWriter().write(responseDto.getResponse());
        }
        response.flushBuffer();
    }
}

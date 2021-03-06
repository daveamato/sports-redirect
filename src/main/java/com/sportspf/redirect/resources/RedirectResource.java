package com.sportspf.redirect.resources;

import com.mchange.util.Base64Encoder;
import com.sportspf.redirect.cache.KeyFileCache;
import com.sportspf.redirect.cache.M3U8Cache;
import com.sportspf.redirect.dtos.ResponseDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping(value = "/api/redirect")
public class RedirectResource {
    private final RestTemplate restTemplate;
    private static final String BASE_URL = System.getenv("BASE_URL") + "api/redirect/ncaaf/";
    //    private static final String BASE_URL = "http://localhost:8080/api/redirect/ncaaf/";
    private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";

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
        if (StringUtils.isEmpty(request.getHeader("origin")) && !url.contains("my_app")
                && fileName.contains(".m3u8")) {
            return;
        }
        if (fileName.contains(".m3u8")) {
            response.setContentType("audio/x-mpegurl");
            ResponseDTO responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(5)) > 0)) {
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
                try {
                    HttpGet httpGet = new HttpGet(url);
                    httpGet.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
                    if (url.contains("femmebiotch")) {
                        httpGet.setHeader("Referer", "http://femmebiotch.com");
                    }
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
                    responseDto = new ResponseDTO();
                    responseDto.setResponse(res.toString());
                    responseDto.setDownloadedAt(LocalDateTime.now());
                    M3U8Cache.M3U8_RESPONSE_CACHE.put(url, responseDto);
                } catch (Exception e) {
                    response.getWriter().write(e.getMessage());
                    M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.remove(url);
                    response.flushBuffer();
                    return;
                }
                M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.remove(url);
            }
            response.getWriter().write(responseDto.getResponse());
        } else {
            ResponseDTO responseDto = KeyFileCache.KEY_FILE_RESPONSE_CACHE.get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusMinutes(5)) > 0)) {
                Boolean check = KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.get(url);
                while (check != null) {
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    check = KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.get(url);
                    if (check != null) {
                        responseDto = KeyFileCache.KEY_FILE_RESPONSE_CACHE.get(url);
                        if (responseDto != null) {
                            response.getWriter().write(responseDto.getResponse());
                        }
                        response.flushBuffer();
                        return;
                    }
                }
                KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.put(url, true);
                try {
                    String key = restTemplate.getForObject(url, String.class);
                    responseDto = new ResponseDTO();
                    responseDto.setResponse(key);
                    responseDto.setDownloadedAt(LocalDateTime.now());
                    KeyFileCache.KEY_FILE_RESPONSE_CACHE.put(url, responseDto);
                } catch (Exception e) {
                    response.getWriter().write(e.getMessage());
                    KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.remove(url);
                    response.flushBuffer();
                    return;
                }
                KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.remove(url);
            }
            response.getWriter().write(responseDto.getResponse());
        }
        response.flushBuffer();
    }

    @GetMapping(value = "/nhl/**")
    public void redirectNHL(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
    }

    @GetMapping(value = "/ncaaf/**")
    public void ncaaf(HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(20);
        String queryString = request.getQueryString();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (queryString != null) {
            url += "?" + queryString;
            fileName += Base64.getEncoder().encodeToString(queryString.getBytes());
        }
        if (StringUtils.isEmpty(request.getHeader("origin")) && !url.contains("my_app")
                && fileName.contains(".m3u8")) {
            return;
        }
        if (fileName.contains(".m3u8")) {
            response.setContentType("audio/x-mpegurl");
        }
        if (fileName.contains(".m3u8") || fileName.contains("check")) {
            ResponseDTO responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(5)) > 0)) {
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
                try {
                    String m3u8 = restTemplate.getForObject(url, String.class);
                    if (fileName.contains("m3u8")) {
                        int from = m3u8.indexOf("#UPLYNK-KEY:") + 12;
                        if (from > 12) {
                            m3u8 = m3u8.substring(0, from) + BASE_URL + m3u8.substring(from);
                            m3u8 = m3u8.replaceAll("AES-128,URI=\"", "AES-128,URI=\"" + BASE_URL);
                        }
                        m3u8 = m3u8.replaceAll("\\.ts\\?.*", ".ts");
                    }
                    responseDto = new ResponseDTO();
                    responseDto.setResponse(m3u8);
                    responseDto.setDownloadedAt(LocalDateTime.now());
                    M3U8Cache.M3U8_RESPONSE_CACHE.put(url, responseDto);
                } catch (Exception e) {
                    response.getWriter().write(e.getMessage());
                    M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.remove(url);
                    response.flushBuffer();
                    return;
                }
                M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.remove(url);
            }
            response.getWriter().write(responseDto.getResponse());
        }
        response.flushBuffer();
    }

    @GetMapping("/302/**")
    public void method(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String url = request.getRequestURI().substring(18);
        httpServletResponse.setHeader("Location", url);
        httpServletResponse.setStatus(302);
    }
}

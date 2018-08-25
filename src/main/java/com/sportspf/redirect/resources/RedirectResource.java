package com.sportspf.redirect.resources;

import com.sportspf.redirect.cache.KeyFileCache;
import com.sportspf.redirect.cache.M3U8Cache;
import com.sportspf.redirect.dtos.ResponseDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {
        "http://www.cominstream.com",
        "http://cominstream.com"
})
@RestController
@RequestMapping(value = "/api/redirect")
public class RedirectResource {
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://ncaaf-redirect.cfapps.io/api/redirect/ncaaf/";
    //    private static final String BASE_URL = "http://localhost:8080/api/redirect/ncaaf/";
    private static final Map<Integer, String> MLB_KEY_URL_MAP = new HashMap<>();
    private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";

    static {
        MLB_KEY_URL_MAP.put(2, "http://52.56.118.143/keys/");
        MLB_KEY_URL_MAP.put(3, "http://bilasport.net/keys/");
        MLB_KEY_URL_MAP.put(4, "http://sports24.fun/mlb/test/");
        MLB_KEY_URL_MAP.put(5, "http://sports24.fun/mlb/keys/");
    }

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
            ResponseDTO responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(5)) > 0)) {
                Boolean check = M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.get(url);
                while (check != null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    check = M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.get(url);
                    if (check != null) {
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
                responseDto.setResponse(res.toString());
                responseDto.setDownloadedAt(LocalDateTime.now());
                M3U8Cache.M3U8_RESPONSE_CACHE.put(url, responseDto);
                M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.remove(url);
            }
            response.getWriter().write(responseDto.getResponse());
        }
        response.flushBuffer();
    }

    @GetMapping(value = "/mlb/{id}/**")
    public void redirectMLB(HttpServletRequest request,
                            HttpServletResponse response,
                            @PathVariable(value = "id") Integer id) throws IOException {
        response.setContentType("application/octet-stream");
        String url = MLB_KEY_URL_MAP.get(id);
        if (url == null) {
            response.flushBuffer();
            return;
        }
        url += request.getRequestURI().substring(20);
        String queryString = request.getQueryString();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (queryString != null) {
            url += "?" + queryString;
        }
        if (fileName.contains(".file") || url.contains("keys")) {
            ResponseDTO responseDto = KeyFileCache.KEY_FILE_RESPONSE_CACHE.get(url);
            File file = new File(fileName);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusMinutes(5)) > 0)) {
                Boolean check = KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.get(url);
                while (check != null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    check = KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.get(url);
                    if (check != null) {
                        responseDto = KeyFileCache.KEY_FILE_RESPONSE_CACHE.get(url);
                        if (responseDto != null) {
                            InputStream inputStream = new FileInputStream(file);
                            IOUtils.copy(inputStream, response.getOutputStream());
                            inputStream.close();
                        }
                        response.flushBuffer();
                        return;
                    }
                }
                KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.put(url, true);
                ReadableByteChannel rbc = Channels.newChannel(new URL(url).openStream());
                FileOutputStream fos = new FileOutputStream(file);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                responseDto = new ResponseDTO();
                responseDto.setDownloadedAt(LocalDateTime.now());
                KeyFileCache.KEY_FILE_RESPONSE_CACHE.put(url, responseDto);
                KeyFileCache.CHECK_DOWNLOAD_KEY_FILE_CACHE.remove(url);
            }
            InputStream inputStream = new FileInputStream(file);
            IOUtils.copy(inputStream, response.getOutputStream());
            inputStream.close();
        }
        response.flushBuffer();
    }

    @GetMapping(value = "/withUrl/**")
    public void redirect(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(22);
        String queryString = request.getQueryString();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (queryString != null) {
            url += "?" + queryString;
        }
        if (fileName.contains(".m3u8")) {
            ResponseDTO responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(5)) > 0)) {
                Boolean check = M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.get(url);
                while (check != null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    check = M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.get(url);
                    if (check != null) {
                        responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
                        if (responseDto != null) {
                            response.getWriter().write(responseDto.getResponse());
                        }
                        response.flushBuffer();
                        return;
                    }
                }
                M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.put(url, true);
                String m3u8 = restTemplate.getForObject(url, String.class);
                responseDto = new ResponseDTO();
                responseDto.setResponse(m3u8);
                responseDto.setDownloadedAt(LocalDateTime.now());
                M3U8Cache.M3U8_RESPONSE_CACHE.put(url, responseDto);
                M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.remove(url);
            }
            response.getWriter().write(responseDto.getResponse());
        }
        response.flushBuffer();
    }

    @GetMapping(value = "/ncaaf/**")
    public void ncaaf(HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(20);
        String queryString = request.getQueryString();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (queryString != null) {
            url += "?" + queryString;
        }
        if (fileName.contains(".m3u8") || fileName.contains("check")) {
            ResponseDTO responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
            if (!(responseDto != null && responseDto.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(5)) > 0)) {
                Boolean check = M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.get(url);
                while (check != null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    check = M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.get(url);
                    if (check != null) {
                        responseDto = M3U8Cache.M3U8_RESPONSE_CACHE.get(url);
                        if (responseDto != null) {
                            response.getWriter().write(responseDto.getResponse());
                        }
                        response.flushBuffer();
                        return;
                    }
                }
                M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.put(url, true);
                String m3u8 = restTemplate.getForObject(url, String.class);
                if (fileName.contains("m3u8")) {
                    if (m3u8.contains("EXT-UPLYNK-LIVE")) {
                        m3u8 = m3u8.replaceAll("https", BASE_URL + "https");
                    } else {
                        int from = m3u8.indexOf("#UPLYNK-KEY:") + 12;
                        if (from > 12) {
                            m3u8 = m3u8.substring(0, from) + BASE_URL + m3u8.substring(from);
                            m3u8 = m3u8.replaceAll("AES-128,URI=\"", "AES-128,URI=\"" + BASE_URL);
                        }
                    }
                }
                responseDto = new ResponseDTO();
                responseDto.setResponse(m3u8);
                responseDto.setDownloadedAt(LocalDateTime.now());
                M3U8Cache.M3U8_RESPONSE_CACHE.put(url, responseDto);
                M3U8Cache.CHECK_DOWNLOAD_M3U8_FILE_CACHE.remove(url);
            }
            response.getWriter().write(responseDto.getResponse());
        }
        response.flushBuffer();
    }
}

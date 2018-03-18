package com.livexsports.redirect.resources;

import com.livexsports.redirect.cache.M3U8Cache;
import com.livexsports.redirect.cache.RedirectFileCache;
import com.livexsports.redirect.dtos.M3U8ResponseDTO;
import com.livexsports.redirect.dtos.RedirectFileDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;

@CrossOrigin(origins = {
    "http://www.xfasports.com",
    "http://xfasports.com"
})
@RestController
@RequestMapping(value = "/api/redirect")
public class RedirectResource {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();

    @GetMapping(value = "/**")
    public void m3u8Redirect(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(14);
        String queryString = request.getQueryString();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (queryString != null) {
            url += "?" + queryString;
        }
        if (fileName.contains(".m3u8")) {
            M3U8ResponseDTO m3U8ResponseDTO = M3U8Cache.getM3u8ResponseCache().get(url);
            if (!(m3U8ResponseDTO != null && m3U8ResponseDTO.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(10)) > 0)) {
                String m3u8 = REST_TEMPLATE.getForObject(url, String.class);
                m3U8ResponseDTO = new M3U8ResponseDTO();
                m3U8ResponseDTO.setResponse(m3u8);
                m3U8ResponseDTO.setDownloadedAt(LocalDateTime.now());
                M3U8Cache.getM3u8ResponseCache().put(url, m3U8ResponseDTO);
            }
            IOUtils.write(m3U8ResponseDTO.getResponse(), response.getOutputStream(), "UTF-8");
        } else {
            RedirectFileDTO redirectFileDTO = RedirectFileCache.REDIRECT_FILE_CACHE.get(fileName);
            if (redirectFileDTO == null) {
                redirectFileDTO = new RedirectFileDTO();
                redirectFileDTO.setFile(new File(fileName));
                redirectFileDTO.setDownloadedAt(LocalDateTime.now());
                FileUtils.copyURLToFile(new URL(url), redirectFileDTO.getFile());
                RedirectFileCache.REDIRECT_FILE_CACHE.put(fileName, redirectFileDTO);
            }
            FileInputStream fileInputStream = new FileInputStream(redirectFileDTO.getFile());
            IOUtils.copy(fileInputStream, response.getOutputStream());
            fileInputStream.close();
        }
        response.flushBuffer();
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
            httpGet.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36");
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
            M3U8ResponseDTO m3U8ResponseDTO = M3U8Cache.getM3u8ResponseCache().get(url);
            if (!(m3U8ResponseDTO != null && m3U8ResponseDTO.getDownloadedAt().compareTo(LocalDateTime.now().minusSeconds(10)) > 0)) {
                m3U8ResponseDTO = new M3U8ResponseDTO();
                m3U8ResponseDTO.setResponse(res.toString());
                m3U8ResponseDTO.setDownloadedAt(LocalDateTime.now());
                M3U8Cache.getM3u8ResponseCache().put(url, m3U8ResponseDTO);
            }
            IOUtils.write(m3U8ResponseDTO.getResponse(), response.getOutputStream(), "UTF-8");
        }
        response.flushBuffer();
    }
}

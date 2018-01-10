package com.livexsports.redirect.resources;

import com.livexsports.redirect.cache.RedirectFileCache;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/api/redirect")
public class RedirectResource {
    //    @CrossOrigin(origins = {
//        "http://livexsports.com/", "http://m3u8.livexsports.com",
//        "http://navixstream.com/", "http://m3u8.navixstream.com",
//        "http://watchusasports.com/", "http://m3u8.watchusasports.com"
//    })
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/**")
    public void m3u8Redirect(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(14);
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        InputStream inputStream;
        if (!fileName.endsWith(".m3u8")) {
            File file = new File(fileName);
            if (RedirectFileCache.getRedirectFileCache().get(fileName) == null) {
                FileUtils.copyURLToFile(new URL(url), file);
                RedirectFileCache.getRedirectFileCache().put(fileName, LocalDateTime.now());
            }
            inputStream = new FileInputStream(file);
        } else {
            inputStream = new URL(url).openStream();
        }
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
        inputStream.close();
    }
}

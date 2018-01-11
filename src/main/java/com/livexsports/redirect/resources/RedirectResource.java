package com.livexsports.redirect.resources;

import com.livexsports.redirect.cache.RedirectFileCache;
import com.livexsports.redirect.dtos.RedirectFileDTO;
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

@RestController
@RequestMapping(value = "/api/redirect")
public class RedirectResource {
    @CrossOrigin(origins = {
        "http://livexsports.com/", "http://m3u8.livexsports.com",
        "http://navixstream.com/", "http://m3u8.navixstream.com",
        "http://watchusasports.com/", "http://m3u8.watchusasports.com"
    })
    @GetMapping(value = "/**")
    public void m3u8Redirect(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        String url = request.getRequestURI().substring(14);
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        InputStream inputStream;
        if (!fileName.endsWith(".m3u8")) {
            RedirectFileDTO redirectFileDTO = RedirectFileCache.getRedirectFileCache().get(fileName);
            if (redirectFileDTO == null) {
                redirectFileDTO = new RedirectFileDTO();
                redirectFileDTO.setFile(new File(fileName));
                FileUtils.copyURLToFile(new URL(url), redirectFileDTO.getFile());
                RedirectFileCache.getRedirectFileCache().put(fileName, redirectFileDTO);
            }
            inputStream = new FileInputStream(redirectFileDTO.getFile());
        } else {
            inputStream = new URL(url).openStream();
        }
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
        inputStream.close();
    }
}

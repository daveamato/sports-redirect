package com.livexsports.redirect.resources;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        InputStream inputStream = new URL(url).openStream();
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
        inputStream.close();
    }
}

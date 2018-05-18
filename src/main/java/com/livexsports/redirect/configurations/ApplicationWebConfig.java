package com.livexsports.redirect.configurations;

import net.sf.ehcache.Ehcache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.ehcache.EhcacheHttpCacheStorage;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.cache.Cache;

@EnableWebMvc
@Configuration
@ComponentScan({"com.livexsports.redirect"})
@EnableCaching
public class ApplicationWebConfig extends WebMvcConfigurerAdapter {
    public static final int DEFAULT_MAX_CACHE_ENTRIES = 50;

    @Value("#{cacheManager.getCache('httpClient')}")
    private Cache httpClientCache;

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager result = new PoolingHttpClientConnectionManager();
        result.setMaxTotal(20);
        return result;
    }

    @Bean
    public CacheConfig cacheConfig() {
        return CacheConfig
                .custom()
                .setMaxCacheEntries(DEFAULT_MAX_CACHE_ENTRIES)
                .build();
    }

    @Bean
    public HttpCacheStorage httpCacheStorage() {
        Ehcache ehcache = (Ehcache) this.httpClientCache.getNativeCache();
        return new EhcacheHttpCacheStorage(ehcache);
    }

    @Bean
    public HttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager,
                                 CacheConfig cacheConfig, HttpCacheStorage httpCacheStorage) {

        return CachingHttpClientBuilder
                .create()
                .setCacheConfig(cacheConfig)
                .setHttpCacheStorage(httpCacheStorage)
                .disableRedirectHandling()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .build();
    }

    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }
}

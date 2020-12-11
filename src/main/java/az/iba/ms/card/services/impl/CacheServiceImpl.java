package az.iba.ms.card.services.impl;

import az.iba.ms.card.services.CacheService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    @Value("${caching.maxItem}")
    private int maxItem;

    @Value("${caching.expireTime}")
    private int expireTime;

    private LoadingCache<String, List<String>> cifCardNumberCache;

    @PostConstruct
    public void initCache() {
        cifCardNumberCache =
                CacheBuilder.newBuilder()
                        .maximumSize(maxItem)
                        .expireAfterWrite(expireTime, TimeUnit.HOURS)
                        .build(
                                new CacheLoader<String, List<String>>() {
                                    @Override
                                    public List<String> load(String key) {
                                        return new ArrayList<>();
                                    }
                                });
    }

    @Override
    public List<String> getCachedCardNumbersOfCif(String cif) {
        try {
            return cifCardNumberCache.get(cif);
        } catch (NullPointerException | ExecutionException ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getCachedCardNumbersOfCifList(List<String> cifs) {
        List<String> result = new ArrayList<>();
        cifs.forEach(cif -> result.addAll(getCachedCardNumbersOfCif(cif)));

        return result;
    }

    @Override
    public void cacheCardNumbers(String cif, List<String> cardNumbers) {
        cifCardNumberCache.put(cif, cardNumbers);
    }
}

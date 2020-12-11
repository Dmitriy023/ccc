package az.iba.ms.card.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    @Test
    public void shouldHandleException_WhenNullRequested() {

        List<String> result = cacheService.getCachedCardNumbersOfCif(null);

        assertThat(result).isEmpty();
    }
}

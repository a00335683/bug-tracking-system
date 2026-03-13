package karate;

import com.intuit.karate.junit5.Karate;
import com.tus.BugTrackerApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        classes = BugTrackerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class KarateTest {

    @LocalServerPort
    int port;

    @Karate.Test
    Karate testAll() {
        System.setProperty("karate.baseUrl", "http://localhost:" + port);
        return Karate.run("classpath:karate");
    }
}
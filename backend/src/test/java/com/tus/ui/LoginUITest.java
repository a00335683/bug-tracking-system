package com.tus.ui;

import com.tus.db.models.User;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoginUITest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new User(
                "tester1",
                passwordEncoder.encode("pass"),
                "TESTER",
                true
        ));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("http://localhost:" + port + "/index.html");

        ((JavascriptExecutor) driver).executeScript(
                "localStorage.removeItem('token'); localStorage.removeItem('role');"
        );
        driver.navigate().refresh();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }

        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void loginSuccess_loadsDashboardInSpa() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));

        driver.findElement(By.id("username")).sendKeys("tester1");
        driver.findElement(By.id("password")).sendKeys("pass");
        driver.findElement(By.id("loginForm")).submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("projectCount")));

        assertTrue(driver.getCurrentUrl().contains("index.html"));
        assertTrue(driver.findElement(By.id("projectCount")).isDisplayed());
    }

    @Test
    void loginFailure_showsErrorMessage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));

        driver.findElement(By.id("username")).sendKeys("tester1");
        driver.findElement(By.id("password")).sendKeys("wrongpass");
        driver.findElement(By.id("loginForm")).submit();

        WebElement errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("errorMessage"))
        );

        assertTrue(errorMessage.isDisplayed());
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }
}

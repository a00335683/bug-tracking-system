package com.tus.ui;

import com.tus.db.models.Project;
import com.tus.db.models.User;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
class ArchiveProjectUITest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;
    private WebDriverWait wait;

    private Project project;

    @BeforeEach
    void setUp() {
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new User(
                "admin",
                passwordEncoder.encode("password"),
                "ADMIN",
                true
        ));

        project = projectRepository.save(new Project(
                "Archive UI Project",
                "Project to test archive flow"
        ));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
    void archiveProject_success() {
        loginAsAdmin();

        driver.findElement(By.id("navProjects")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("projectsTable")));

        WebElement archiveButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".archive-btn[data-id='" + project.getId() + "']")
                )
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", archiveButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", archiveButton);

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("projectMessage"),
                "Project archived successfully."
        ));

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("projectsTable"),
                "ARCHIVED"
        ));

        WebElement projectsTable = driver.findElement(By.id("projectsTable"));
        assertTrue(projectsTable.getText().contains("Archive UI Project"));
        assertTrue(projectsTable.getText().contains("ARCHIVED"));
    }

    private void loginAsAdmin() {
        driver.get("http://localhost:" + port + "/index.html");

        ((JavascriptExecutor) driver).executeScript(
                "localStorage.removeItem('token'); localStorage.removeItem('role');"
        );
        driver.navigate().refresh();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));

        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("loginForm")).submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("navProjects")));
    }

}
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
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
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
class CreateIssueUITest {

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

    private User tester;
    private Project project;

    @BeforeEach
    void setUp() {
        issueRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        tester = userRepository.save(new User(
                "tester1",
                passwordEncoder.encode("pass"),
                "TESTER",
                true
        ));

        project = projectRepository.save(new Project(
                "Test Project",
                "Project for UI test"
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
    void createIssue_success() {
        loginAsTester();

        driver.findElement(By.id("navIssues")).click();

        WebElement createIssueBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("createIssueBtn"))
        );

        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", createIssueBtn);

        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", createIssueBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("createIssueForm")));

        driver.findElement(By.id("projectId")).sendKeys(String.valueOf(project.getId()));
        driver.findElement(By.id("reporterId")).sendKeys(String.valueOf(tester.getId()));
        driver.findElement(By.id("issueTitle")).sendKeys("UI Test Bug");
        driver.findElement(By.id("issueDescription")).sendKeys("Issue created by Selenium test");
        driver.findElement(By.id("issuePriority")).sendKeys("HIGH");

        driver.findElement(By.id("createIssueForm")).submit();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(alert.getText().contains("Issue created successfully"));
        alert.accept();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("issuesTable"),
                "UI Test Bug"
        ));

        WebElement issuesTable = driver.findElement(By.id("issuesTable"));
        assertTrue(issuesTable.getText().contains("UI Test Bug"));
        assertTrue(issuesTable.getText().contains("HIGH"));
    }

    private void loginAsTester() {
        driver.get("http://localhost:" + port + "/index.html");

        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "localStorage.removeItem('token'); localStorage.removeItem('role');"
        );
        driver.navigate().refresh();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));

        driver.findElement(By.id("username")).sendKeys("tester1");
        driver.findElement(By.id("password")).sendKeys("pass");
        driver.findElement(By.id("loginForm")).submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("navIssues")));
    }

}
package com.tus.ui;

import com.tus.db.models.Issue;
import com.tus.db.models.Project;
import com.tus.db.models.User;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import com.tus.enums.IssuePriority;
import com.tus.enums.IssueStatus;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
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
class UpdateIssueStatusUITest {

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
    private User developer;
    private Project project;
    private Issue issue;

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

        developer = userRepository.save(new User(
                "dev1",
                passwordEncoder.encode("pass"),
                "DEVELOPER",
                true
        ));

        project = projectRepository.save(new Project(
                "Status UI Project",
                "Project for update status UI test"
        ));

        issue = new Issue(
                "Status Test Bug",
                "Bug for developer workflow test",
                IssuePriority.HIGH,
                project,
                tester
        );
        issue.setAssignedTo(developer);
        issue.setStatus(IssueStatus.OPEN);
        issue = issueRepository.save(issue);

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
    void developerCanMoveIssueToInProgress() {
        loginAsDeveloper();

        driver.findElement(By.id("navIssues")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("issuesTable")));

        WebElement statusButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".status-btn[data-id='" + issue.getId() + "']")
                )
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", statusButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", statusButton);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateStatusForm")));

        Select statusSelect = new Select(driver.findElement(By.id("newStatus")));
        statusSelect.selectByVisibleText("IN_PROGRESS");

        driver.findElement(By.id("updateStatusForm")).submit();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(alert.getText().contains("Issue status updated successfully"));
        alert.accept();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("issuesTable"),
                "IN_PROGRESS"
        ));

        WebElement issuesTable = driver.findElement(By.id("issuesTable"));
        assertTrue(issuesTable.getText().contains("Status Test Bug"));
        assertTrue(issuesTable.getText().contains("IN_PROGRESS"));
    }

    private void loginAsDeveloper() {
        driver.get("http://localhost:" + port + "/login.html");

        driver.findElement(By.id("username")).sendKeys("dev1");
        driver.findElement(By.id("password")).sendKeys("pass");
        driver.findElement(By.id("loginForm")).submit();

        wait.until(ExpectedConditions.urlContains("index.html"));
    }
}
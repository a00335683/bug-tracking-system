package com.tus.config;

import com.tus.db.models.Issue;
import com.tus.db.models.Project;
import com.tus.db.models.User;
import com.tus.db.repos.IssueRepository;
import com.tus.db.repos.ProjectRepository;
import com.tus.db.repos.UserRepository;
import com.tus.enums.IssuePriority;
import com.tus.enums.IssueStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile({"dev", "test"})
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            IssueRepository issueRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            issueRepository.deleteAll();
            projectRepository.deleteAll();
            userRepository.deleteAll();

            User admin = userRepository.save(new User(
                    "admin",
                    passwordEncoder.encode("password"),
                    "ADMIN",
                    true
            ));

            User tester = userRepository.save(new User(
                    "tester1",
                    passwordEncoder.encode("tester123"),
                    "TESTER",
                    true
            ));

            User developer = userRepository.save(new User(
                    "dev1",
                    passwordEncoder.encode("pass"),
                    "DEVELOPER",
                    true
            ));

            Project project1 = projectRepository.save(new Project(
                    "Bug Tracker System",
                    "Main project for tracking and resolving software bugs"
            ));

            Project project2 = projectRepository.save(new Project(
                    "Mobile Banking App",
                    "Tracks defects and fixes for the banking mobile application"
            ));

            Project project3 = projectRepository.save(new Project(
                    "E-Commerce Platform",
                    "Tracks issues in the online shopping platform"
            ));

            Issue issue1 = new Issue(
                    "Login fails with valid credentials",
                    "Users report that valid usernames and passwords are rejected on login.",
                    IssuePriority.HIGH,
                    project1,
                    tester
            );
            issue1.setAssignedTo(developer);
            issueRepository.save(issue1);

            Issue issue2 = new Issue(
                    "Dashboard graph not loading",
                    "The issue statistics graph does not appear after page refresh.",
                    IssuePriority.MEDIUM,
                    project1,
                    tester
            );
            issue2.setStatus(IssueStatus.IN_PROGRESS);
            issue2.setAssignedTo(developer);
            issueRepository.save(issue2);

            Issue issue3 = new Issue(
                    "Issue filter returns delayed results",
                    "Filtering by priority and status takes too long on larger issue lists.",
                    IssuePriority.LOW,
                    project1,
                    tester
            );
            issue3.setStatus(IssueStatus.RESOLVED);
            issue3.setAssignedTo(developer);
            issue3.setResolutionNote("Optimized filtering logic in service layer.");
            issueRepository.save(issue3);

            Issue issue4 = new Issue(
                    "Payment confirmation screen freezes",
                    "Users get stuck on the payment confirmation screen after submitting payment.",
                    IssuePriority.HIGH,
                    project2,
                    tester
            );
            issue4.setAssignedTo(developer);
            issueRepository.save(issue4);

            Issue issue5 = new Issue(
                    "Transaction history not refreshing",
                    "Recent transactions are not shown until the user logs out and back in.",
                    IssuePriority.MEDIUM,
                    project2,
                    tester
            );
            issue5.setStatus(IssueStatus.VERIFIED);
            issue5.setAssignedTo(developer);
            issue5.setResolutionNote("Fixed stale API response caching.");
            issueRepository.save(issue5);

            Issue issue6 = new Issue(
                    "Notification badge count incorrect",
                    "Unread notification count does not match the actual number of messages.",
                    IssuePriority.LOW,
                    project2,
                    tester
            );
            issue6.setStatus(IssueStatus.CLOSED);
            issue6.setAssignedTo(developer);
            issue6.setResolutionNote("Adjusted badge count update after read action.");
            issueRepository.save(issue6);

            Issue issue7 = new Issue(
                    "Checkout button disabled after coupon applied",
                    "Users cannot proceed to checkout after entering a valid coupon code.",
                    IssuePriority.HIGH,
                    project3,
                    tester
            );
            issue7.setAssignedTo(developer);
            issueRepository.save(issue7);

            Issue issue8 = new Issue(
                    "Product search is slow",
                    "Search results take several seconds to load for common keywords.",
                    IssuePriority.MEDIUM,
                    project3,
                    tester
            );
            issue8.setStatus(IssueStatus.IN_PROGRESS);
            issue8.setAssignedTo(developer);
            issueRepository.save(issue8);

            Issue issue9 = new Issue(
                    "Product image alignment broken on mobile",
                    "Product cards appear uneven on smaller screens.",
                    IssuePriority.LOW,
                    project3,
                    tester
            );
            issue9.setStatus(IssueStatus.CLOSED);
            issue9.setAssignedTo(developer);
            issue9.setResolutionNote("Updated responsive CSS grid rules.");
            issueRepository.save(issue9);
        };
    }
}
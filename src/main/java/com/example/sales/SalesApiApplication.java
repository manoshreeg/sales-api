package com.example.sales;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SalesApiApplication {
    public static void main(String[] args) { SpringApplication.run(SalesApiApplication.class, args); }

    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.findByUsername("admin").isEmpty()) {
                AppUser u = new AppUser();
                u.username = "admin";
                u.password = encoder.encode("admin123");
                u.role = "ADMIN";
                users.save(u);
            }
        };
    }

    @Bean
    CommandLineRunner seedRules(CategoryRuleRepository rules) {
        return args -> {
            if (rules.count() > 0) return;
            String[][] seed = {
                {"shaving cream", "Shaving Care", "5"}, {"wax", "Hair Removal", "10"},
                {"epilator", "Hair Removal", "10"}, {"razor", "Razors", "10"},
                {"blade", "Razors", "20"}, {"shaving", "Shaving Care", "30"},
                {"shampoo", "Hair Care", "10"}, {"conditioner", "Hair Care", "10"},
                {"lotion", "Skincare", "10"}, {"moisturizer", "Skincare", "10"}};
            for (String[] r : seed) rules.save(new CategoryRule(r[0], r[1], Integer.parseInt(r[2])));
        };
    }
}

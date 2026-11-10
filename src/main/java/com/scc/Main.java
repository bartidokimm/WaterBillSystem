package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            DatabaseInitializer.initialize();
        } catch (SQLException e) {
            System.out.println(
                    "Database initialization failed: " + e.getMessage()
            );
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            Authentication authentication =
                    new Authentication(scanner, new UserDao());
            authentication.start();
        }
    }
}
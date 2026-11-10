package com.scc.ui;

import java.util.Scanner;

// Shared console helpers so every screen header looks the same.
public final class ConsoleUi {

    private static final int WIDTH = 34;

    private ConsoleUi() {
    }

    public static void printHeader(String title) {
        String line = "=".repeat(WIDTH);
        int padding = Math.max(0, (WIDTH - title.length()) / 2);

        System.out.println();
        System.out.println(line);
        System.out.println(" ".repeat(padding) + title);
        System.out.println(line);
    }

    // Shows a numbered list and accepts a number or the option's name.
    // Returns null when the user enters 0 to cancel.
    public static String chooseOption(Scanner scanner, String title,
                                      String[] options, String prompt,
                                      String invalidMessage) {
        while (true) {
            System.out.println();
            System.out.println(title);
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ". " + options[i]);
            }
            System.out.print(prompt);

            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                return null;
            }

            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < options.length) {
                    return options[index];
                }
            } catch (NumberFormatException e) {
                // not a number, so try matching the name below
            }

            for (String option : options) {
                if (option.equals(input)) {
                    return option;
                }
            }

            System.out.println(invalidMessage);
        }
    }
}
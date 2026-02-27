package com.example.yoyo_data.user_generate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class FixIdCardFormat {
    private static final Random random = new Random();

    public static void main(String[] args) {
        String inputFile = "d:\\JavaPro\\yoyo_data\\src\\main\\resources\\jmeter-test\\test_requests.txt";
        String outputFile = "d:\\JavaPro\\yoyo_data\\src\\main\\resources\\jmeter-test\\test_requests_fixed.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String fixedLine = fixIdCardInLine(line);
                writer.write(fixedLine);
                writer.newLine();
                count++;
            }
            System.out.println("Fixed " + count + " lines.");
            System.out.println("Output saved to: " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String fixIdCardInLine(String line) {
        String result = line;
        int startIndex = result.indexOf("\"contactIdCard\":");
        while (startIndex != -1) {
            int quoteIndex = result.indexOf('"', startIndex + 15);
            if (quoteIndex == -1) break;
            int endQuoteIndex = result.indexOf('"', quoteIndex + 1);
            if (endQuoteIndex == -1) break;

            String oldIdCard = result.substring(quoteIndex + 1, endQuoteIndex);
            String newIdCard = generateValidIdCard();
            result = result.substring(0, quoteIndex + 1) + newIdCard + result.substring(endQuoteIndex);

            startIndex = result.indexOf("\"contactIdCard\":", endQuoteIndex);
        }
        return result;
    }

    private static String generateValidIdCard() {
        String areaCode = "110101";
        int year = 1990 + random.nextInt(11);
        String yearStr = String.format("%04d", year);
        int month = 1 + random.nextInt(12);
        String monthStr = String.format("%02d", month);
        int day;
        if (month == 2) {
            boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            day = 1 + random.nextInt(isLeapYear ? 29 : 28);
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            day = 1 + random.nextInt(30);
        } else {
            day = 1 + random.nextInt(31);
        }
        String dayStr = String.format("%02d", day);
        int sequence = random.nextInt(1000);
        String sequenceStr = String.format("%03d", sequence);
        String[] checkCodes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "X"};
        String checkCode = checkCodes[random.nextInt(checkCodes.length)];
        return areaCode + yearStr + monthStr + dayStr + sequenceStr + checkCode;
    }
}
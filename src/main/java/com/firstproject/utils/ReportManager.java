package com.firstproject.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();
    private static String reportDir;

    public static synchronized ExtentReports get() {
        if (extent == null) {
            String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            reportDir = "reports/run_" + stamp;
            new File(reportDir).mkdirs();
            new File(reportDir + "/screenshots").mkdirs();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportDir + "/index.html");
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("TVA Calculator – Test Report");
            spark.config().setReportName("Suite TestNG (Selenium + DataProvider Excel)");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Site", "ma-calculatrice.fr");
            extent.setSystemInfo("Framework", "Java + Selenium + TestNG + ExtentReports");
        }
        return extent;
    }

    public static void setCurrent(ExtentTest test) {
        CURRENT.set(test);
    }

    public static ExtentTest current() {
        return CURRENT.get();
    }

    public static void clearCurrent() {
        CURRENT.remove();
    }

    public static String captureScreenshot(WebDriver driver, String testName) {
        if (driver == null) return null;
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String safe = testName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path dest = Paths.get(reportDir, "screenshots", safe + "_" + System.currentTimeMillis() + ".png");
            Files.copy(src.toPath(), dest);
            return Paths.get("screenshots").resolve(dest.getFileName()).toString().replace("\\", "/");
        } catch (IOException e) {
            System.err.println("Erreur screenshot : " + e.getMessage());
            return null;
        }
    }

    public static void flush() {
        if (extent != null) extent.flush();
    }
}

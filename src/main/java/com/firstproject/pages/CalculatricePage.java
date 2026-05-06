package com.firstproject.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class CalculatricePage {

    public static final String URL = "https://www.ma-calculatrice.fr/calculer-tva-ttc-ht-convertir";

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By HT_INPUT  = By.id("inputHT");
    private static final By TVA_INPUT = By.id("inputTVA");
    private static final By TTC_INPUT = By.id("inputTTC");
    private static final By TAUX_SELECT = By.id("selectTaux");
    private static final By TAUX_CUSTOM_INPUT = By.id("autreTx");
    private static final By RESET_BUTTON = By.id("reset");
    private static final By SAVE_BUTTON  = By.id("sauvegarde");

    private static final Map<Double, String> FRANCE_TAUX_VALUES = Map.of(
            20.0,  "0.2",
            10.0,  "0.1",
            5.5,   "0.055",
            2.1,   "0.021"
    );

    public CalculatricePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public CalculatricePage open() {
        driver.get(URL);
        acceptCookiesIfPresent();
        wait.until(ExpectedConditions.presenceOfElementLocated(HT_INPUT));
        return this;
    }

    private void acceptCookiesIfPresent() {
        List<WebElement> btns = driver.findElements(
                By.xpath("//button[contains(translate(., 'ACEPT', 'acept'), 'accepter') or contains(., 'Accept')]"));
        if (!btns.isEmpty()) {
            try { btns.get(0).click(); } catch (Exception ignored) {}
        }
    }

    public CalculatricePage reset() {
        List<WebElement> btn = driver.findElements(RESET_BUTTON);
        if (!btn.isEmpty()) {
            btn.get(0).click();
        } else {
            clearAll();
        }
        return this;
    }

    private void clearAll() {
        for (By loc : List.of(HT_INPUT, TVA_INPUT, TTC_INPUT)) {
            List<WebElement> els = driver.findElements(loc);
            if (!els.isEmpty()) {
                els.get(0).clear();
            }
        }
    }

    public CalculatricePage selectTaux(String typeTaux, double taux) {
        org.openqa.selenium.support.ui.Select select =
                new org.openqa.selenium.support.ui.Select(driver.findElement(TAUX_SELECT));

        if ("autre".equalsIgnoreCase(typeTaux)) {
            select.selectByValue("autre");
            WebElement custom = wait.until(
                    ExpectedConditions.elementToBeClickable(TAUX_CUSTOM_INPUT));
            custom.clear();
            custom.sendKeys(formatNumber(taux));
            custom.sendKeys(Keys.TAB);
        } else {
            String value = FRANCE_TAUX_VALUES.get(taux);
            if (value == null) {
                throw new IllegalArgumentException(
                        "Taux France non standard : " + taux + " (attendu 2.1, 5.5, 10 ou 20)");
            }
            select.selectByValue(value);
        }
        return this;
    }

    public CalculatricePage enterHT(double value) {
        return type(HT_INPUT, value);
    }

    public CalculatricePage enterTVA(double value) {
        return type(TVA_INPUT, value);
    }

    public CalculatricePage enterTTC(double value) {
        return type(TTC_INPUT, value);
    }

    private CalculatricePage type(By locator, double value) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.clear();
        el.sendKeys(formatNumber(value));
        el.sendKeys(Keys.TAB);
        return this;
    }

    public Double readHT()  { return readNumeric(HT_INPUT); }
    public Double readTVA() { return readNumeric(TVA_INPUT); }
    public Double readTTC() { return readNumeric(TTC_INPUT); }

    private Double readNumeric(By locator) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) return null;
        String raw = els.get(0).getAttribute("value");
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.replace(",", ".").replace(" ", "").replaceAll("[^0-9.\\-]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String formatNumber(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }
}

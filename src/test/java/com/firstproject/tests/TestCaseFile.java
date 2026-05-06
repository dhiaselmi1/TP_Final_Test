package com.firstproject.tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.firstproject.dataproviders.TVADataProvider;
import com.firstproject.models.TestRow;
import com.firstproject.pages.CalculatricePage;
import com.firstproject.utils.DriverManager;
import com.firstproject.utils.ReportManager;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestCaseFile {

    private static final double TOLERANCE = 0.02;

    @BeforeMethod
    public void setUp() {
        ReportManager.get();
    }

    private CalculatricePage openCalculator() {
        return new CalculatricePage(DriverManager.get()).open();
    }

    @Test(dataProvider = "htData", dataProviderClass = TVADataProvider.class)
    public void HT_test_validator(TestRow row) {
        ExtentTest tc = ReportManager.get().createTest("HT_test_validator – " + row.testName());
        ReportManager.setCurrent(tc);
        tc.info("Données : " + row);

        if (!row.isValide()) {
            Assert.assertFalse(TVADataProvider.isRowDataValid(row),
                    "Validation incohérente : la ligne marquée invalide passe la validation : " + row);
            tc.info("Cas invalide correctement rejeté par la validation (" + row.getRemarque() + ") — UI non sollicitée");
            return;
        }

        CalculatricePage page = openCalculator()
                .reset()
                .selectTaux(row.getTypeTaux(), row.getTaux())
                .enterHT(row.getInputValue());

        Double actualTVA = page.readTVA();
        Double actualTTC = page.readTTC();

        tc.info(String.format("Réel : TVA=%s, TTC=%s | Attendu : TVA=%s, TTC=%s",
                actualTVA, actualTTC, row.getExpectedFirst(), row.getExpectedSecond()));

        Assert.assertNotNull(actualTVA, "TVA absente");
        Assert.assertNotNull(actualTTC, "TTC absente");
        Assert.assertEquals(actualTVA, row.getExpectedFirst(), TOLERANCE, "TVA incorrecte");
        Assert.assertEquals(actualTTC, row.getExpectedSecond(), TOLERANCE, "TTC incorrecte");
    }

    @Test(dataProvider = "tvaData", dataProviderClass = TVADataProvider.class)
    public void TVA_test_validator(TestRow row) {
        ExtentTest tc = ReportManager.get().createTest("TVA_test_validator – " + row.testName());
        ReportManager.setCurrent(tc);
        tc.info("Données : " + row);

        if (!row.isValide()) {
            Assert.assertFalse(TVADataProvider.isRowDataValid(row),
                    "Validation incohérente : la ligne marquée invalide passe la validation : " + row);
            tc.info("Cas invalide correctement rejeté par la validation (" + row.getRemarque() + ") — UI non sollicitée");
            return;
        }

        CalculatricePage page = openCalculator()
                .reset()
                .selectTaux(row.getTypeTaux(), row.getTaux())
                .enterTVA(row.getInputValue());

        Double actualHT  = page.readHT();
        Double actualTTC = page.readTTC();

        tc.info(String.format("Réel : HT=%s, TTC=%s | Attendu : HT=%s, TTC=%s",
                actualHT, actualTTC, row.getExpectedFirst(), row.getExpectedSecond()));

        Assert.assertNotNull(actualHT, "HT absente");
        Assert.assertNotNull(actualTTC, "TTC absente");
        Assert.assertEquals(actualHT,  row.getExpectedFirst(),  TOLERANCE, "HT incorrecte");
        Assert.assertEquals(actualTTC, row.getExpectedSecond(), TOLERANCE, "TTC incorrecte");
    }

    @Test(dataProvider = "ttcData", dataProviderClass = TVADataProvider.class)
    public void TTC_test_validator(TestRow row) {
        ExtentTest tc = ReportManager.get().createTest("TTC_test_validator – " + row.testName());
        ReportManager.setCurrent(tc);
        tc.info("Données : " + row);

        if (!row.isValide()) {
            Assert.assertFalse(TVADataProvider.isRowDataValid(row),
                    "Validation incohérente : la ligne marquée invalide passe la validation : " + row);
            tc.info("Cas invalide correctement rejeté par la validation (" + row.getRemarque() + ") — UI non sollicitée");
            return;
        }

        CalculatricePage page = openCalculator()
                .reset()
                .selectTaux(row.getTypeTaux(), row.getTaux())
                .enterTTC(row.getInputValue());

        Double actualHT  = page.readHT();
        Double actualTVA = page.readTVA();

        tc.info(String.format("Réel : HT=%s, TVA=%s | Attendu : HT=%s, TVA=%s",
                actualHT, actualTVA, row.getExpectedFirst(), row.getExpectedSecond()));

        Assert.assertNotNull(actualHT,  "HT absente");
        Assert.assertNotNull(actualTVA, "TVA absente");
        Assert.assertEquals(actualHT,  row.getExpectedFirst(),  TOLERANCE, "HT incorrecte");
        Assert.assertEquals(actualTVA, row.getExpectedSecond(), TOLERANCE, "TVA incorrecte");
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        ExtentTest tc = ReportManager.current();
        try {
            if (tc != null) {
                if (result.getStatus() == ITestResult.SUCCESS) {
                    tc.pass("Test réussi");
                } else if (result.getStatus() == ITestResult.FAILURE) {
                    String shotPath = ReportManager.captureScreenshot(
                            DriverManager.get(), result.getName());
                    if (shotPath != null) {
                        tc.fail("Échec : " + result.getThrowable(),
                                MediaEntityBuilder.createScreenCaptureFromPath(shotPath).build());
                    } else {
                        tc.fail("Échec : " + result.getThrowable());
                    }
                } else if (result.getStatus() == ITestResult.SKIP) {
                    tc.skip("Skippé : " + result.getThrowable());
                }
            }
        } finally {
            ReportManager.clearCurrent();
            DriverManager.quit();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        ReportManager.flush();
    }
}

package com.firstproject.dataproviders;

import com.firstproject.models.TestRow;
import com.firstproject.utils.ExcelDataGenerator;
import com.firstproject.utils.ExcelReader;
import org.testng.annotations.DataProvider;

import java.util.List;
import java.util.Set;

public class TVADataProvider {

    private static final Set<Double> TAUX_FRANCE_VALIDES = Set.of(2.1, 5.5, 10.0, 20.0);

    @DataProvider(name = "htData", parallel = false)
    public static Object[][] htData() {
        return loadAndPack(ExcelDataGenerator.SHEET_HT, TestRow.Mode.HT);
    }

    @DataProvider(name = "tvaData", parallel = false)
    public static Object[][] tvaData() {
        return loadAndPack(ExcelDataGenerator.SHEET_TVA, TestRow.Mode.TVA);
    }

    @DataProvider(name = "ttcData", parallel = false)
    public static Object[][] ttcData() {
        return loadAndPack(ExcelDataGenerator.SHEET_TTC, TestRow.Mode.TTC);
    }

    private static Object[][] loadAndPack(String sheet, TestRow.Mode mode) {
        ExcelDataGenerator.ensureExists();
        List<TestRow> rows = ExcelReader.readSheet(ExcelDataGenerator.DEFAULT_PATH, sheet, mode);
        if (!isExcelValid(rows)) {
            throw new RuntimeException("Le fichier Excel est incohérent pour la sheet " + sheet);
        }
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    public static boolean isExcelValid(List<TestRow> rows) {
        if (rows == null || rows.isEmpty()) return false;
        for (TestRow row : rows) {
            boolean computedValid = isRowDataValid(row);
            if (computedValid != row.isValide()) {
                System.err.printf("Incohérence ligne %d : Excel.valide=%s, calculé=%s — %s%n",
                        row.getRowIndex(), row.isValide(), computedValid, row);
                return false;
            }
        }
        return true;
    }

    public static boolean isRowDataValid(TestRow row) {
        if (row.getInputValue() <= 0) return false;
        if (!isTauxValid(row.getTypeTaux(), row.getTaux())) return false;
        if (row.getExpectedFirst() == null || row.getExpectedSecond() == null) return false;
        if (row.getExpectedFirst() <= 0 || row.getExpectedSecond() <= 0) return false;
        return true;
    }

    public static boolean isTauxValid(String typeTaux, double taux) {
        if (typeTaux == null) return false;
        if ("France".equalsIgnoreCase(typeTaux)) {
            return TAUX_FRANCE_VALIDES.contains(taux);
        }
        if ("autre".equalsIgnoreCase(typeTaux)) {
            return taux > 0 && taux < 100;
        }
        return false;
    }
}

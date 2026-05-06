package com.firstproject.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelDataGenerator {

    public static final String DEFAULT_PATH = "test-data/tva_test_data.xlsx";

    public static final String SHEET_HT = "HT";
    public static final String SHEET_TVA = "TVA";
    public static final String SHEET_TTC = "TTC";

    private static final Object[][] HT_ROWS = {
            { 100.0, "France", 20.0,  20.00, 120.00, true,  "Taux France valide" },
            { 200.0, "France", 10.0,  20.00, 220.00, true,  "" },
            {  50.0, "France",  5.5,   2.75,  52.75, true,  "" },
            { 150.0, "France", 105.0, null,  null,   false, "Taux > 100%, invalide" },
            { -80.0, "France", 20.0,  null,  null,   false, "HT négatif" },
            { 120.0, "autre",   7.0,   8.40, 128.40, true,  "Taux personnalisé" },
            {  90.0, "France",  2.1,   1.89,  91.89, true,  "" }
    };

    private static final Object[][] TVA_ROWS = {
            {  20.0, "France", 20.0, 100.00, 120.00, true,  "" },
            {   5.5, "France",  5.5, 100.00, 105.50, true,  "" },
            {   2.1, "France",  2.1, 100.00, 102.10, true,  "" },
            {  10.0, "France",  0.0, null,   null,   false, "Taux zéro, invalide" },
            { -15.0, "France", 10.0, null,   null,   false, "TVA négative" },
            {   9.0, "autre",   6.0, 150.00, 159.00, true,  "Taux personnalisé" },
            {  18.0, "France", 105.0, null,  null,   false, "Taux > 100%" }
    };

    private static final Object[][] TTC_ROWS = {
            { 120.0,   "France", 20.0,  100.00,  20.00, true,  "" },
            { 105.5,   "France",  5.5, 100.00,   5.50, true,  "" },
            {  91.89,  "France",  2.1,  90.00,   1.89, true,  "" },
            { 220.0,   "France", 110.0, null,   null,  false, "Taux invalide (>100%)" },
            { -200.0,  "France", 20.0,  null,   null,  false, "TTC négatif" },
            { 150.0,   "autre",  25.0, 120.00,  30.00, true,  "Taux personnalisé" },
            { 100.0,   "France",  0.0, null,   null,   false, "Taux invalide (0%)" }
    };

    private static final String[] HT_HEADERS  = { "HT",  "Type de taux", "Taux", "Attendu TVA", "Attendu TTC", "Valide", "Remarque" };
    private static final String[] TVA_HEADERS = { "TVA", "Type de taux", "Taux", "Attendu HT",  "Attendu TTC", "Valide", "Remarque" };
    private static final String[] TTC_HEADERS = { "TTC", "Type de taux", "Taux", "Attendu HT",  "Attendu TVA", "Valide", "Remarque" };

    public static void ensureExists() {
        File f = new File(DEFAULT_PATH);
        if (f.exists()) return;
        f.getParentFile().mkdirs();
        generate(DEFAULT_PATH);
    }

    public static void generate(String path) {
        File target = new File(path);
        if (target.getParentFile() != null) target.getParentFile().mkdirs();
        try (Workbook wb = new XSSFWorkbook()) {
            writeSheet(wb, SHEET_HT,  HT_HEADERS,  HT_ROWS);
            writeSheet(wb, SHEET_TVA, TVA_HEADERS, TVA_ROWS);
            writeSheet(wb, SHEET_TTC, TTC_HEADERS, TTC_ROWS);
            try (FileOutputStream fos = new FileOutputStream(target)) {
                wb.write(fos);
            }
            System.out.println("Excel généré : " + new File(path).getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Échec génération Excel", e);
        }
    }

    private static void writeSheet(Workbook wb, String name, String[] headers, Object[][] rows) {
        Sheet sheet = wb.createSheet(name);

        CellStyle headerStyle = wb.createCellStyle();
        Font bold = wb.createFont();
        bold.setBold(true);
        headerStyle.setFont(bold);

        Row header = sheet.createRow(0);
        for (int c = 0; c < headers.length; c++) {
            Cell cell = header.createCell(c);
            cell.setCellValue(headers[c]);
            cell.setCellStyle(headerStyle);
        }

        for (int r = 0; r < rows.length; r++) {
            Row row = sheet.createRow(r + 1);
            Object[] data = rows[r];
            for (int c = 0; c < data.length; c++) {
                Cell cell = row.createCell(c);
                Object v = data[c];
                if (v == null) {
                    cell.setCellValue("-");
                } else if (v instanceof Number) {
                    cell.setCellValue(((Number) v).doubleValue());
                } else if (v instanceof Boolean) {
                    cell.setCellValue((Boolean) v);
                } else {
                    cell.setCellValue(v.toString());
                }
            }
        }

        for (int c = 0; c < headers.length; c++) {
            sheet.autoSizeColumn(c);
        }
    }

    public static void main(String[] args) {
        generate(DEFAULT_PATH);
    }
}

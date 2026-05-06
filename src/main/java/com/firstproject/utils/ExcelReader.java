package com.firstproject.utils;

import com.firstproject.models.TestRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    public static List<TestRow> readSheet(String path, String sheetName, TestRow.Mode mode) {
        File f = new File(path);
        if (!f.exists()) {
            throw new RuntimeException("Fichier Excel introuvable : " + f.getAbsolutePath());
        }
        List<TestRow> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(f);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet manquante : " + sheetName);
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                double input        = readDouble(row.getCell(0));
                String typeTaux     = readString(row.getCell(1));
                double taux         = readDouble(row.getCell(2));
                Double expectedA    = readNullableDouble(row.getCell(3));
                Double expectedB    = readNullableDouble(row.getCell(4));
                boolean valide      = readBoolean(row.getCell(5));
                String remarque     = readString(row.getCell(6));

                rows.add(new TestRow(mode, input, typeTaux, taux,
                        expectedA, expectedB, valide, remarque, r));
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture Excel : " + path, e);
        }
        return rows;
    }

    private static boolean isRowEmpty(Row row) {
        for (int c = 0; c < 7; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private static double readDouble(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC: return cell.getNumericCellValue();
            case STRING:
                String s = cell.getStringCellValue().trim();
                if (s.isEmpty() || "-".equals(s)) return 0;
                return Double.parseDouble(s.replace(",", "."));
            default: return 0;
        }
    }

    private static Double readNullableDouble(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC: return cell.getNumericCellValue();
            case STRING:
                String s = cell.getStringCellValue().trim();
                if (s.isEmpty() || "-".equals(s)) return null;
                try { return Double.parseDouble(s.replace(",", ".")); }
                catch (NumberFormatException e) { return null; }
            default: return null;
        }
    }

    private static String readString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private static boolean readBoolean(Cell cell) {
        if (cell == null) return false;
        switch (cell.getCellType()) {
            case BOOLEAN: return cell.getBooleanCellValue();
            case STRING:  return Boolean.parseBoolean(cell.getStringCellValue().trim());
            default: return false;
        }
    }
}

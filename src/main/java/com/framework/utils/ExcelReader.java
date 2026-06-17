package com.framework.utils;

import com.framework.exception.TestDataException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Reads Excel (.xlsx) test data files.
 *
 * Convention: first row = headers, subsequent rows = data.
 *
 * Usage:
 *   ExcelReader xls = new ExcelReader("src/test/resources/testdata/payments.xlsx");
 *   List<Map<String,String>> rows = xls.getSheetData("CreditCard");
 */
public class ExcelReader {

    private static final Logger log = LogManager.getLogger(ExcelReader.class);
    private final String filePath;

    public ExcelReader(String filePath) {
        this.filePath = filePath;
    }

    /** Returns all data rows as List<Map<header, cellValue>>. */
    public List<Map<String, String>> getSheetData(String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) headers.add(cell.getStringCellValue().trim());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowMap.put(headers.get(j), getCellValue(cell));
                }
                data.add(rowMap);
            }
            log.info("Read {} data rows from {}[{}]", data.size(), filePath, sheetName);

        } catch (IOException e) {
            throw new TestDataException(filePath, e);
        }
        return data;
    }

    /** Returns data as Object[][] for TestNG @DataProvider. */
    public Object[][] asDataProvider(String sheetName) {
        List<Map<String, String>> rows = getSheetData(sheetName);
        Object[][] result = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) result[i][0] = rows.get(i);
        return result;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default      -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !getCellValue(cell).isEmpty()) return false;
        }
        return true;
    }
}

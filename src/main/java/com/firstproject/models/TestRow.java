package com.firstproject.models;

public class TestRow {

    public enum Mode { HT, TVA, TTC }

    private final Mode mode;
    private final double inputValue;
    private final String typeTaux;
    private final double taux;
    private final Double expectedFirst;
    private final Double expectedSecond;
    private final boolean valide;
    private final String remarque;
    private final int rowIndex;

    public TestRow(Mode mode, double inputValue, String typeTaux, double taux,
                   Double expectedFirst, Double expectedSecond,
                   boolean valide, String remarque, int rowIndex) {
        this.mode = mode;
        this.inputValue = inputValue;
        this.typeTaux = typeTaux;
        this.taux = taux;
        this.expectedFirst = expectedFirst;
        this.expectedSecond = expectedSecond;
        this.valide = valide;
        this.remarque = remarque;
        this.rowIndex = rowIndex;
    }

    public Mode getMode() { return mode; }
    public double getInputValue() { return inputValue; }
    public String getTypeTaux() { return typeTaux; }
    public double getTaux() { return taux; }
    public Double getExpectedFirst() { return expectedFirst; }
    public Double getExpectedSecond() { return expectedSecond; }
    public boolean isValide() { return valide; }
    public String getRemarque() { return remarque; }
    public int getRowIndex() { return rowIndex; }

    public String testName() {
        return mode + "_row" + rowIndex + "_" + inputValue + "@" + taux + "%";
    }

    @Override
    public String toString() {
        return String.format("[%s] input=%.2f type=%s taux=%.2f attendu=(%s, %s) valide=%s remarque=%s",
                mode, inputValue, typeTaux, taux,
                expectedFirst, expectedSecond, valide, remarque);
    }
}

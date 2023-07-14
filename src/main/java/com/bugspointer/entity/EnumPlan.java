package com.bugspointer.entity;

public enum EnumPlan {

    FREE("0"),
    TARGET("15.00"); //,
    // ULTIMATE("30.00");

    private final String valeur;

    private EnumPlan(String valeur) {
        this.valeur = valeur;
    }

    public String getValeur() {
        return this.valeur;
    }
}

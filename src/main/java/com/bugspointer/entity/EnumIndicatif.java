package com.bugspointer.entity;

public enum EnumIndicatif {

    FRANCE("+33"),
    BELGIQUE("+32");

    private final String valeur;

    private EnumIndicatif(String valeur) {
        this.valeur = valeur;
    }

    public String getValeur() {
        return this.valeur;
    }

    public static EnumIndicatif getByValeur(String valeur) {
        for (EnumIndicatif indicatif : values()) {
            if (indicatif.getValeur().equals(valeur)) {
                return indicatif;
            }
        }
        return null;
    }
}

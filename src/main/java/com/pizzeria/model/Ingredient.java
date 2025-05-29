package com.pizzeria.model;

public class Ingredient {
    private int id;
    private String nom;
    private int stock;
    private int nbUtilisations;

    public Ingredient() {
    }

    public Ingredient(int id, String nom, int stock, int nbUtilisations) {
        this.id = id;
        this.nom = nom;
        this.stock = stock;
        this.nbUtilisations = nbUtilisations;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getNbUtilisations() {
        return nbUtilisations;
    }

    public void setNbUtilisations(int nbUtilisations) {
        this.nbUtilisations = nbUtilisations;
    }

    @Override
    public String toString() {
        return nom + " (Stock: " + stock + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 
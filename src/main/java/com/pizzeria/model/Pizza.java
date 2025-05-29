package com.pizzeria.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Pizza {
    private int id;
    private String nom;
    private BigDecimal prixBase;
    private List<Ingredient> ingredients;

    public Pizza() {
        this.ingredients = new ArrayList<>();
    }

    public Pizza(int id, String nom, BigDecimal prixBase) {
        this.id = id;
        this.nom = nom;
        this.prixBase = prixBase;
        this.ingredients = new ArrayList<>();
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

    public BigDecimal getPrixBase() {
        return prixBase;
    }

    public void setPrixBase(BigDecimal prixBase) {
        this.prixBase = prixBase;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public void removeIngredient(Ingredient ingredient) {
        this.ingredients.remove(ingredient);
    }

    @Override
    public String toString() {
        return nom + " - " + prixBase + "€";
    }

    public String getIngredientsAsString() {
        List<String> noms = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            noms.add(ingredient.getNom());
        }
        return String.join(", ", noms);
    }
} 
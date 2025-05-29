package com.pizzeria.model;

public class Livreur {
    private int id;
    private String nom;
    private String prenom;
    private String telephone;
    private int nombreRetards;
    private boolean disponible;

    // Constructeur par défaut
    public Livreur() {
        this.nombreRetards = 0;
        this.disponible = true;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public int getNombreRetards() { return nombreRetards; }
    public void setNombreRetards(int nombreRetards) { this.nombreRetards = nombreRetards; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }
} 
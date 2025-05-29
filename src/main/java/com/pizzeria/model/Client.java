package com.pizzeria.model;

import java.math.BigDecimal;

public class Client {
    private int id;
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;
    private BigDecimal soldeCompte;
    private int nbPizzasAchetees;

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public BigDecimal getSoldeCompte() { return soldeCompte; }
    public void setSoldeCompte(BigDecimal soldeCompte) { this.soldeCompte = soldeCompte; }

    public int getNbPizzasAchetees() { return nbPizzasAchetees; }
    public void setNbPizzasAchetees(int nbPizzasAchetees) { this.nbPizzasAchetees = nbPizzasAchetees; }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }
} 
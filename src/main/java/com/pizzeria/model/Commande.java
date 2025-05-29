package com.pizzeria.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Commande {
    private int id;
    private int idClient;
    private int idPizza;
    private int idTaille;
    private Integer idLivreur;
    private Integer idVehicule;
    private Timestamp dateCommande;
    private Timestamp dateLivraison;
    private BigDecimal montantInitial;
    private BigDecimal montantFinal;
    private String statut;
    
    // Champs pour l'affichage
    private String clientNom;
    private String clientAdresse;
    private String pizzaNom;
    private String tailleNom;
    private String livreurNom;
    private int tempsEcoule;  // en minutes

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }
    
    public int getIdPizza() { return idPizza; }
    public void setIdPizza(int idPizza) { this.idPizza = idPizza; }
    
    public int getIdTaille() { return idTaille; }
    public void setIdTaille(int idTaille) { this.idTaille = idTaille; }
    
    public Integer getIdLivreur() { return idLivreur; }
    public void setIdLivreur(Integer idLivreur) { this.idLivreur = idLivreur; }
    
    public Integer getIdVehicule() { return idVehicule; }
    public void setIdVehicule(Integer idVehicule) { this.idVehicule = idVehicule; }
    
    public Timestamp getDateCommande() { return dateCommande; }
    public void setDateCommande(Timestamp dateCommande) { this.dateCommande = dateCommande; }
    
    public Timestamp getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(Timestamp dateLivraison) { this.dateLivraison = dateLivraison; }
    
    public BigDecimal getMontantInitial() { return montantInitial; }
    public void setMontantInitial(BigDecimal montantInitial) { this.montantInitial = montantInitial; }
    
    public BigDecimal getMontantFinal() { return montantFinal; }
    public void setMontantFinal(BigDecimal montantFinal) { this.montantFinal = montantFinal; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }
    
    public String getClientAdresse() { return clientAdresse; }
    public void setClientAdresse(String clientAdresse) { this.clientAdresse = clientAdresse; }
    
    public String getPizzaNom() { return pizzaNom; }
    public void setPizzaNom(String pizzaNom) { this.pizzaNom = pizzaNom; }
    
    public String getTailleNom() { return tailleNom; }
    public void setTailleNom(String tailleNom) { this.tailleNom = tailleNom; }
    
    public String getLivreurNom() { return livreurNom; }
    public void setLivreurNom(String livreurNom) { this.livreurNom = livreurNom; }

    public int getTempsEcoule() {
        return tempsEcoule;
    }

    public void setTempsEcoule(int tempsEcoule) {
        this.tempsEcoule = tempsEcoule;
    }
} 
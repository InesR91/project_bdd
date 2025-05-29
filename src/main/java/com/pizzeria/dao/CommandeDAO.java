package com.pizzeria.dao;

import com.pizzeria.model.Commande;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeDAO {
    private static final String INSERT_COMMANDE = """
        INSERT INTO Vente (
            id_client, id_pizza, id_taille, 
            montant_initial, montant_final, statut,
            date_commande
        ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
    """;
    private static final String UPDATE_COMMANDE = "UPDATE Vente SET id_livreur = ?, id_vehicule = ?, statut = ?, date_livraison = ? WHERE id_vente = ?";
    private static final String DELETE_COMMANDE = "DELETE FROM Vente WHERE id_vente = ?";
    private static final String SELECT_ALL_COMMANDES = """
        SELECT v.*, c.nom as client_nom, c.prenom as client_prenom,
               p.nom as pizza_nom, t.nom as taille_nom,
               l.nom as livreur_nom, l.prenom as livreur_prenom
        FROM Vente v
        JOIN Client c ON v.id_client = c.id_client
        JOIN Pizza p ON v.id_pizza = p.id_pizza
        JOIN Taille t ON v.id_taille = t.id_taille
        LEFT JOIN Livreur l ON v.id_livreur = l.id_livreur
        ORDER BY v.date_commande DESC
    """;
    private static final String SELECT_COMMANDE_BY_ID = """
        SELECT v.*, c.nom as client_nom, c.prenom as client_prenom,
               p.nom as pizza_nom, t.nom as taille_nom,
               l.nom as livreur_nom, l.prenom as livreur_prenom
        FROM Vente v
        JOIN Client c ON v.id_client = c.id_client
        JOIN Pizza p ON v.id_pizza = p.id_pizza
        JOIN Taille t ON v.id_taille = t.id_taille
        LEFT JOIN Livreur l ON v.id_livreur = l.id_livreur
        WHERE v.id_vente = ?
    """;
    private static final String GET_VEHICULE_DISPONIBLE = """
        SELECT id_vehicule FROM Vehicule 
        WHERE disponible = true 
        AND NOT EXISTS (
            SELECT 1 FROM Vente 
            WHERE Vente.id_vehicule = Vehicule.id_vehicule 
            AND statut = 'en_cours'
        )
        LIMIT 1
    """;
    private static final String SELECT_LIVRAISONS_EN_COURS = """
        SELECT v.*, c.nom as client_nom, c.prenom as client_prenom,
               c.adresse as client_adresse,
               l.nom as livreur_nom, l.prenom as livreur_prenom,
               EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - v.date_commande))/60 as temps_ecoule
        FROM Vente v
        JOIN Client c ON v.id_client = c.id_client
        JOIN Livreur l ON v.id_livreur = l.id_livreur
        WHERE v.statut = 'en_cours'
        ORDER BY v.date_commande ASC
    """;
    private static final String GET_PIZZA_INGREDIENTS = """
        SELECT pi.id_ingredient, 
               CASE 
                   WHEN t.nom = 'Grande' THEN pi.quantite * 1.5
                   WHEN t.nom = 'Moyenne' THEN pi.quantite * 1.0
                   WHEN t.nom = 'Petite' THEN pi.quantite * 0.75
                   ELSE pi.quantite
               END as quantite_totale
        FROM Pizza_Ingredient pi
        JOIN Vente v ON v.id_pizza = pi.id_pizza
        JOIN Taille t ON t.id_taille = v.id_taille
        WHERE v.id_vente = ?
    """;
    
    private static final String UPDATE_INGREDIENT_STOCK = """
        UPDATE Ingredient 
        SET stock = stock - ? 
        WHERE id_ingredient = ? AND stock >= ?
    """;

    private static final String CHECK_CLIENT_EXISTS = "SELECT 1 FROM Client WHERE id_client = ?";

    private final Connection connection;

    public CommandeDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public void create(Commande commande) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Vérifier que le client existe
            try (PreparedStatement checkStmt = conn.prepareStatement(CHECK_CLIENT_EXISTS)) {
                checkStmt.setInt(1, commande.getIdClient());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Le client avec l'ID " + commande.getIdClient() + " n'existe pas.");
                    }
                }
            }

            // Insérer la commande
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_COMMANDE, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, commande.getIdClient());
                pstmt.setInt(2, commande.getIdPizza());
                pstmt.setInt(3, commande.getIdTaille());
                pstmt.setBigDecimal(4, commande.getMontantInitial());
                pstmt.setBigDecimal(5, commande.getMontantFinal());
                pstmt.setString(6, "en_attente");
                
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        commande.setId(rs.getInt(1));
                    }
                }
            }

            // Mettre à jour le stock des ingrédients
            try (PreparedStatement pstmt = conn.prepareStatement(GET_PIZZA_INGREDIENTS)) {
                pstmt.setInt(1, commande.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int idIngredient = rs.getInt("id_ingredient");
                        double quantiteNecessaire = rs.getDouble("quantite_totale");
                        
                        // Mettre à jour le stock
                        try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_INGREDIENT_STOCK)) {
                            updateStmt.setDouble(1, quantiteNecessaire);
                            updateStmt.setInt(2, idIngredient);
                            updateStmt.setDouble(3, quantiteNecessaire);
                            
                            int rowsAffected = updateStmt.executeUpdate();
                            if (rowsAffected == 0) {
                                throw new SQLException("Stock insuffisant pour un ou plusieurs ingrédients");
                            }
                        }
                    }
                }
            }

            // Mettre à jour le nombre de pizzas achetées par le client
            try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE Client SET nb_pizzas_achetees = nb_pizzas_achetees + 1 WHERE id_client = ?")) {
                updateStmt.setInt(1, commande.getIdClient());
                updateStmt.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Erreur lors du rollback: " + ex.getMessage(), ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DatabaseConnection.closeConnection(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Integer getVehiculeDisponible() throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(GET_VEHICULE_DISPONIBLE)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_vehicule");
                }
            }
        }
        return null;
    }

    public void update(Commande commande) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Récupérer l'ancien statut
            String ancienStatut = null;
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT statut FROM Vente WHERE id_vente = ?")) {
                pstmt.setInt(1, commande.getId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    ancienStatut = rs.getString("statut");
                }
            }

            // Si on assigne un livreur pour la première fois (passage de en_attente à en_cours)
            if (commande.getIdLivreur() != null && commande.getIdLivreur() > 0 
                && "en_attente".equals(ancienStatut) && "en_cours".equals(commande.getStatut())) {
                // Vérifier si le livreur est disponible
                try (PreparedStatement checkLivreur = conn.prepareStatement(
                        "SELECT disponible FROM Livreur WHERE id_livreur = ?")) {
                    checkLivreur.setInt(1, commande.getIdLivreur());
                    ResultSet rs = checkLivreur.executeQuery();
                    if (!rs.next() || !rs.getBoolean("disponible")) {
                        throw new SQLException("Le livreur n'est pas disponible");
                    }
                }

                // Trouver un véhicule disponible
                Integer vehiculeId = getVehiculeDisponible();
                if (vehiculeId == null) {
                    throw new SQLException("Aucun véhicule disponible pour le moment");
                }
                commande.setIdVehicule(vehiculeId);

                // Mettre à jour le statut du véhicule
                try (PreparedStatement updateVehicule = conn.prepareStatement(
                        "UPDATE Vehicule SET disponible = false WHERE id_vehicule = ?")) {
                    updateVehicule.setInt(1, vehiculeId);
                    updateVehicule.executeUpdate();
                }
            }

            // Mettre à jour la commande
            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_COMMANDE)) {
                pstmt.setObject(1, commande.getIdLivreur());
                pstmt.setObject(2, commande.getIdVehicule());
                pstmt.setString(3, commande.getStatut());
                pstmt.setTimestamp(4, commande.getDateLivraison());
                pstmt.setInt(5, commande.getId());
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Erreur lors du rollback: " + ex.getMessage(), ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DatabaseConnection.closeConnection(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_COMMANDE)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public Commande getById(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_COMMANDE_BY_ID)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Commande commande = new Commande();
                    commande.setId(rs.getInt("id_vente"));
                    commande.setIdClient(rs.getInt("id_client"));
                    commande.setIdPizza(rs.getInt("id_pizza"));
                    commande.setIdTaille(rs.getInt("id_taille"));
                    commande.setIdLivreur(rs.getInt("id_livreur"));
                    commande.setIdVehicule(rs.getInt("id_vehicule"));
                    commande.setDateCommande(rs.getTimestamp("date_commande"));
                    commande.setDateLivraison(rs.getTimestamp("date_livraison"));
                    commande.setMontantInitial(rs.getBigDecimal("montant_initial"));
                    commande.setMontantFinal(rs.getBigDecimal("montant_final"));
                    commande.setStatut(rs.getString("statut"));
                    
                    // Informations supplémentaires pour l'affichage
                    commande.setClientNom(rs.getString("client_nom") + " " + rs.getString("client_prenom"));
                    commande.setPizzaNom(rs.getString("pizza_nom"));
                    commande.setTailleNom(rs.getString("taille_nom"));
                    if (rs.getString("livreur_nom") != null) {
                        commande.setLivreurNom(rs.getString("livreur_nom") + " " + rs.getString("livreur_prenom"));
                    }
                    
                    return commande;
                }
            }
        }
        return null;
    }

    public List<Commande> getAllCommandes() throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_ALL_COMMANDES);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id_vente"));
                commande.setIdClient(rs.getInt("id_client"));
                commande.setIdPizza(rs.getInt("id_pizza"));
                commande.setIdTaille(rs.getInt("id_taille"));
                commande.setIdLivreur(rs.getInt("id_livreur"));
                commande.setIdVehicule(rs.getInt("id_vehicule"));
                commande.setDateCommande(rs.getTimestamp("date_commande"));
                commande.setDateLivraison(rs.getTimestamp("date_livraison"));
                commande.setMontantInitial(rs.getBigDecimal("montant_initial"));
                commande.setMontantFinal(rs.getBigDecimal("montant_final"));
                commande.setStatut(rs.getString("statut"));
                
                // Informations supplémentaires pour l'affichage
                commande.setClientNom(rs.getString("client_nom") + " " + rs.getString("client_prenom"));
                commande.setPizzaNom(rs.getString("pizza_nom"));
                commande.setTailleNom(rs.getString("taille_nom"));
                if (rs.getString("livreur_nom") != null) {
                    commande.setLivreurNom(rs.getString("livreur_nom") + " " + rs.getString("livreur_prenom"));
                }
                
                commandes.add(commande);
            }
        }
        
        return commandes;
    }

    public List<Commande> getLivraisonsEnCours() throws SQLException {
        List<Commande> livraisons = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_LIVRAISONS_EN_COURS);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id_vente"));
                commande.setClientNom(rs.getString("client_nom") + " " + rs.getString("client_prenom"));
                commande.setClientAdresse(rs.getString("client_adresse"));
                commande.setLivreurNom(rs.getString("livreur_nom") + " " + rs.getString("livreur_prenom"));
                commande.setTempsEcoule((int) rs.getDouble("temps_ecoule"));
                livraisons.add(commande);
            }
        }
        return livraisons;
    }
} 
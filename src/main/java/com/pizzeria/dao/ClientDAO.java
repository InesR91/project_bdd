package com.pizzeria.dao;

import com.pizzeria.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ClientDAO {
    private static final String INSERT_CLIENT = "INSERT INTO Client (nom, prenom, telephone, adresse, solde_compte, nb_pizzas_achetees) VALUES (?, ?, ?, ?, ?, 0)";
    private static final String UPDATE_CLIENT = "UPDATE Client SET nom = ?, prenom = ?, telephone = ?, adresse = ? WHERE id_client = ?";
    private static final String DELETE_CLIENT = "DELETE FROM Client WHERE id_client = ?";
    private static final String SELECT_ALL_CLIENTS = "SELECT * FROM Client ORDER BY nom, prenom";
    private static final String SELECT_CLIENT_BY_ID = "SELECT * FROM Client WHERE id_client = ?";
    private static final String UPDATE_SOLDE = "UPDATE Client SET solde_compte = solde_compte + ? WHERE id_client = ?";
    private static final String SEARCH_CLIENTS = "SELECT * FROM Client WHERE LOWER(nom) LIKE LOWER(?) OR LOWER(prenom) LIKE LOWER(?) OR telephone LIKE ?";

    public void create(Client client) throws SQLException {
        Connection conn = null;
        try {
            System.out.println("Tentative de création d'un client : " + client.getNom() + " " + client.getPrenom());
            conn = DatabaseConnection.getConnection();
            
            if (conn == null) {
                throw new SQLException("Impossible d'obtenir une connexion à la base de données");
            }
            
            conn.setAutoCommit(false);
            System.out.println("Connexion obtenue, autocommit désactivé");

            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_CLIENT, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, client.getNom());
                pstmt.setString(2, client.getPrenom());
                pstmt.setString(3, client.getTelephone());
                pstmt.setString(4, client.getAdresse());
                pstmt.setBigDecimal(5, client.getSoldeCompte() != null ? client.getSoldeCompte() : BigDecimal.ZERO);
                
                System.out.println("Exécution de la requête d'insertion");
                int affectedRows = pstmt.executeUpdate();
                System.out.println("Nombre de lignes affectées : " + affectedRows);
                
                if (affectedRows == 0) {
                    throw new SQLException("La création du client a échoué, aucune ligne insérée.");
                }
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        client.setId(rs.getInt(1));
                        System.out.println("ID généré : " + client.getId());
                    } else {
                        throw new SQLException("La création du client a échoué, aucun ID généré.");
                    }
                }
            }
            
            System.out.println("Commit de la transaction");
            conn.commit();
            System.out.println("Client créé avec succès");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du client : " + e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Tentative de rollback");
                    conn.rollback();
                    System.err.println("Rollback effectué");
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback : " + ex.getMessage());
                    throw new SQLException("Erreur lors du rollback: " + ex.getMessage(), ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    System.out.println("Autocommit réactivé");
                    DatabaseConnection.closeConnection(conn);
                    System.out.println("Connexion fermée");
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void update(Client client) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_CLIENT)) {
                pstmt.setString(1, client.getNom());
                pstmt.setString(2, client.getPrenom());
                pstmt.setString(3, client.getTelephone());
                pstmt.setString(4, client.getAdresse());
                pstmt.setInt(5, client.getId());
                
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_CLIENT)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public Client getById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_CLIENT_BY_ID)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClient(rs);
                }
            }
        }
        return null;
    }

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_CLIENTS);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        }
        return clients;
    }

    public void updateSolde(int clientId, java.math.BigDecimal montant) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_SOLDE)) {
                pstmt.setBigDecimal(1, montant);
                pstmt.setInt(2, clientId);
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

    public List<Client> searchClients(String searchTerm) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_CLIENTS)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapResultSetToClient(rs));
                }
            }
        }
        return clients;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getInt("id_client"));
        client.setNom(rs.getString("nom"));
        client.setPrenom(rs.getString("prenom"));
        client.setTelephone(rs.getString("telephone"));
        client.setAdresse(rs.getString("adresse"));
        client.setSoldeCompte(rs.getBigDecimal("solde_compte"));
        client.setNbPizzasAchetees(rs.getInt("nb_pizzas_achetees"));
        return client;
    }
} 
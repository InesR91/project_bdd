package com.pizzeria.dao;

import com.pizzeria.model.Livreur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LivreurDAO {
    private static final String INSERT_LIVREUR = "INSERT INTO Livreur (nom, prenom, telephone) VALUES (?, ?, ?)";
    private static final String UPDATE_LIVREUR = "UPDATE Livreur SET nom = ?, prenom = ?, telephone = ? WHERE id_livreur = ?";
    private static final String DELETE_LIVREUR = "DELETE FROM Livreur WHERE id_livreur = ?";
    private static final String SELECT_ALL_LIVREURS = "SELECT * FROM Livreur ORDER BY nom, prenom";
    private static final String SELECT_LIVREUR_BY_ID = "SELECT * FROM Livreur WHERE id_livreur = ?";
    private static final String UPDATE_RETARDS = "UPDATE Livreur SET nombre_retards = nombre_retards + 1 WHERE id_livreur = ?";
    private static final String SELECT_DISPONIBLES = """
        SELECT l.* FROM Livreur l
        WHERE NOT EXISTS (
            SELECT 1 FROM Vente v
            WHERE v.id_livreur = l.id_livreur
            AND v.statut = 'en_cours'
        )
    """;

    private final Connection connection;

    public LivreurDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public void create(Livreur livreur) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_LIVREUR, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, livreur.getNom());
            pstmt.setString(2, livreur.getPrenom());
            pstmt.setString(3, livreur.getTelephone());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    livreur.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Livreur livreur) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_LIVREUR)) {
            pstmt.setString(1, livreur.getNom());
            pstmt.setString(2, livreur.getPrenom());
            pstmt.setString(3, livreur.getTelephone());
            pstmt.setInt(4, livreur.getId());
            
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_LIVREUR)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Livreur> getAllLivreurs() throws SQLException {
        List<Livreur> livreurs = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_ALL_LIVREURS);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                livreurs.add(mapResultSetToLivreur(rs));
            }
        }
        
        return livreurs;
    }

    public Livreur getById(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_LIVREUR_BY_ID)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLivreur(rs);
                }
            }
        }
        return null;
    }

    public List<Livreur> getLivreursDisponibles() throws SQLException {
        List<Livreur> livreurs = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_DISPONIBLES);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Livreur livreur = mapResultSetToLivreur(rs);
                livreur.setDisponible(true);
                livreurs.add(livreur);
            }
        }
        
        return livreurs;
    }

    public void incrementRetards(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_RETARDS)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Livreur mapResultSetToLivreur(ResultSet rs) throws SQLException {
        Livreur livreur = new Livreur();
        livreur.setId(rs.getInt("id_livreur"));
        livreur.setNom(rs.getString("nom"));
        livreur.setPrenom(rs.getString("prenom"));
        livreur.setTelephone(rs.getString("telephone"));
        livreur.setNombreRetards(rs.getInt("nombre_retards"));
        return livreur;
    }
} 
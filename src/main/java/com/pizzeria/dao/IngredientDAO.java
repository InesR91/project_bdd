package com.pizzeria.dao;

import com.pizzeria.model.Ingredient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {
    private static final String SELECT_ALL = "SELECT * FROM Ingredient ORDER BY nom";
    private static final String SELECT_BY_ID = "SELECT * FROM Ingredient WHERE id_ingredient = ?";
    private static final String INSERT = "INSERT INTO Ingredient (nom, stock, nb_utilisations) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE Ingredient SET nom = ?, stock = ?, nb_utilisations = ? WHERE id_ingredient = ?";
    private static final String DELETE = "DELETE FROM Ingredient WHERE id_ingredient = ?";
    private static final String SELECT_BY_PIZZA = """
        SELECT i.* FROM Ingredient i
        JOIN Pizza_Ingredient pi ON i.id_ingredient = pi.id_ingredient
        WHERE pi.id_pizza = ?
    """;
    private static final String INSERT_PIZZA_INGREDIENT = "INSERT INTO Pizza_Ingredient (id_pizza, id_ingredient, quantite) VALUES (?, ?, 1)";
    private static final String DELETE_PIZZA_INGREDIENTS = "DELETE FROM Pizza_Ingredient WHERE id_pizza = ?";

    private final Connection connection;

    public IngredientDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ingredients.add(mapResultSetToIngredient(rs));
            }
        }
        return ingredients;
    }

    public Ingredient getById(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BY_ID)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIngredient(rs);
                }
            }
        }
        return null;
    }

    public List<Ingredient> getIngredientsByPizza(int pizzaId) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BY_PIZZA)) {
            pstmt.setInt(1, pizzaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(mapResultSetToIngredient(rs));
                }
            }
        }
        return ingredients;
    }

    public void updatePizzaIngredients(int pizzaId, List<Ingredient> ingredients) throws SQLException {
        // Supprimer les anciennes associations
        try (PreparedStatement deleteStmt = connection.prepareStatement(DELETE_PIZZA_INGREDIENTS)) {
            deleteStmt.setInt(1, pizzaId);
            deleteStmt.executeUpdate();
        }

        // Ajouter les nouvelles associations
        try (PreparedStatement insertStmt = connection.prepareStatement(INSERT_PIZZA_INGREDIENT)) {
            for (Ingredient ingredient : ingredients) {
                insertStmt.setInt(1, pizzaId);
                insertStmt.setInt(2, ingredient.getId());
                insertStmt.executeUpdate();
            }
        }
    }

    private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("id_ingredient"));
        ingredient.setNom(rs.getString("nom"));
        ingredient.setStock(rs.getInt("stock"));
        ingredient.setNbUtilisations(rs.getInt("nb_utilisations"));
        return ingredient;
    }
} 
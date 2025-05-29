package com.pizzeria.dao;

import com.pizzeria.model.Pizza;
import com.pizzeria.model.Ingredient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PizzaDAO {
    private static final String INSERT_PIZZA = "INSERT INTO Pizza (nom, prix_base) VALUES (?, ?)";
    private static final String UPDATE_PIZZA = "UPDATE Pizza SET nom = ?, prix_base = ? WHERE id_pizza = ?";
    private static final String DELETE_PIZZA = "DELETE FROM Pizza WHERE id_pizza = ?";
    private static final String DELETE_PIZZA_INGREDIENTS = "DELETE FROM Pizza_Ingredient WHERE id_pizza = ?";
    private static final String SELECT_ALL_PIZZAS = "SELECT * FROM Pizza ORDER BY nom";
    private static final String SELECT_PIZZA_BY_ID = "SELECT * FROM Pizza WHERE id_pizza = ?";
    private static final String CHECK_PIZZA_IN_USE = """
        SELECT COUNT(*) as count 
        FROM Vente 
        WHERE id_pizza = ?
    """;

    private final Connection connection;
    private final IngredientDAO ingredientDAO;

    public PizzaDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.ingredientDAO = new IngredientDAO();
    }

    private boolean isPizzaInUse(int pizzaId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(CHECK_PIZZA_IN_USE)) {
            pstmt.setInt(1, pizzaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    public void create(Pizza pizza) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_PIZZA, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, pizza.getNom());
            pstmt.setBigDecimal(2, pizza.getPrixBase());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pizza.setId(rs.getInt(1));
                }
            }

            // Mettre à jour les ingrédients
            if (!pizza.getIngredients().isEmpty()) {
                ingredientDAO.updatePizzaIngredients(pizza.getId(), pizza.getIngredients());
            }
        }
    }

    public void update(Pizza pizza) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_PIZZA)) {
            pstmt.setString(1, pizza.getNom());
            pstmt.setBigDecimal(2, pizza.getPrixBase());
            pstmt.setInt(3, pizza.getId());
            
            pstmt.executeUpdate();
        }
    }

    public void updateIngredients(Pizza pizza) throws SQLException {
        ingredientDAO.updatePizzaIngredients(pizza.getId(), pizza.getIngredients());
    }

    public void delete(int id) throws SQLException {
        if (isPizzaInUse(id)) {
            throw new SQLException("Impossible de supprimer cette pizza car elle est utilisée dans des commandes.");
        }
        
        // Supprimer d'abord les associations avec les ingrédients
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_PIZZA_INGREDIENTS)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
        
        // Ensuite supprimer la pizza
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_PIZZA)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Pizza> getAllPizzas() throws SQLException {
        List<Pizza> pizzas = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_ALL_PIZZAS);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                pizzas.add(mapResultSetToPizza(rs));
            }
        }
        
        return pizzas;
    }

    public List<Pizza> getAllPizzasWithIngredients() throws SQLException {
        List<Pizza> pizzas = getAllPizzas();
        for (Pizza pizza : pizzas) {
            List<Ingredient> ingredients = ingredientDAO.getIngredientsByPizza(pizza.getId());
            pizza.setIngredients(ingredients);
        }
        return pizzas;
    }

    public Pizza getById(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_PIZZA_BY_ID)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Pizza pizza = mapResultSetToPizza(rs);
                    List<Ingredient> ingredients = ingredientDAO.getIngredientsByPizza(id);
                    pizza.setIngredients(ingredients);
                    return pizza;
                }
            }
        }
        return null;
    }

    private Pizza mapResultSetToPizza(ResultSet rs) throws SQLException {
        Pizza pizza = new Pizza();
        pizza.setId(rs.getInt("id_pizza"));
        pizza.setNom(rs.getString("nom"));
        pizza.setPrixBase(rs.getBigDecimal("prix_base"));
        return pizza;
    }
} 
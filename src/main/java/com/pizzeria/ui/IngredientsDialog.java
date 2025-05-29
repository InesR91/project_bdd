package com.pizzeria.ui;

import com.pizzeria.model.Pizza;
import com.pizzeria.model.Ingredient;
import com.pizzeria.dao.IngredientDAO;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class IngredientsDialog extends JDialog {
    private final Pizza pizza;
    private boolean validated = false;
    private final IngredientDAO ingredientDAO;
    
    private DefaultListModel<Ingredient> availableIngredientsModel;
    private DefaultListModel<Ingredient> selectedIngredientsModel;
    private JList<Ingredient> availableIngredientsList;
    private JList<Ingredient> selectedIngredientsList;
    
    public IngredientsDialog(Frame parent, String title, Pizza pizza) throws SQLException {
        super(parent, title, true);
        this.pizza = pizza;
        this.ingredientDAO = new IngredientDAO();
        
        initComponents();
        loadData();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Panneau principal avec deux listes
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Liste des ingrédients disponibles
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Ingrédients disponibles"));
        availableIngredientsModel = new DefaultListModel<>();
        availableIngredientsList = new JList<>(availableIngredientsModel);
        availablePanel.add(new JScrollPane(availableIngredientsList), BorderLayout.CENTER);
        
        // Liste des ingrédients sélectionnés
        JPanel selectedPanel = new JPanel(new BorderLayout());
        selectedPanel.setBorder(BorderFactory.createTitledBorder("Ingrédients de la pizza"));
        selectedIngredientsModel = new DefaultListModel<>();
        selectedIngredientsList = new JList<>(selectedIngredientsModel);
        selectedPanel.add(new JScrollPane(selectedIngredientsList), BorderLayout.CENTER);
        
        // Boutons pour déplacer les ingrédients
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton addButton = new JButton(">");
        JButton removeButton = new JButton("<");
        
        addButton.addActionListener(e -> addSelectedIngredients());
        removeButton.addActionListener(e -> removeSelectedIngredients());
        
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        
        // Ajout des composants au panneau principal
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(availablePanel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.0;
        mainPanel.add(buttonPanel, gbc);
        
        gbc.gridx = 2; gbc.weightx = 1.0;
        mainPanel.add(selectedPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Boutons OK/Annuler
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Annuler");
        
        okButton.addActionListener(e -> {
            updatePizzaIngredients();
            validated = true;
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        bottomButtonPanel.add(okButton);
        bottomButtonPanel.add(cancelButton);
        add(bottomButtonPanel, BorderLayout.SOUTH);
        
        // Définir une taille raisonnable
        setPreferredSize(new Dimension(600, 400));
    }
    
    private void loadData() throws SQLException {
        // Charger tous les ingrédients disponibles
        List<Ingredient> allIngredients = ingredientDAO.getAllIngredients();
        List<Ingredient> pizzaIngredients = pizza.getIngredients();
        
        // Remplir la liste des ingrédients disponibles
        for (Ingredient ingredient : allIngredients) {
            if (!pizzaIngredients.contains(ingredient)) {
                availableIngredientsModel.addElement(ingredient);
            }
        }
        
        // Remplir la liste des ingrédients sélectionnés
        for (Ingredient ingredient : pizzaIngredients) {
            selectedIngredientsModel.addElement(ingredient);
        }
    }
    
    private void addSelectedIngredients() {
        List<Ingredient> selectedIngredients = availableIngredientsList.getSelectedValuesList();
        for (Ingredient ingredient : selectedIngredients) {
            availableIngredientsModel.removeElement(ingredient);
            selectedIngredientsModel.addElement(ingredient);
        }
    }
    
    private void removeSelectedIngredients() {
        List<Ingredient> selectedIngredients = selectedIngredientsList.getSelectedValuesList();
        for (Ingredient ingredient : selectedIngredients) {
            selectedIngredientsModel.removeElement(ingredient);
            availableIngredientsModel.addElement(ingredient);
        }
    }
    
    private void updatePizzaIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < selectedIngredientsModel.size(); i++) {
            ingredients.add(selectedIngredientsModel.getElementAt(i));
        }
        pizza.setIngredients(ingredients);
    }
    
    public boolean isValidated() {
        return validated;
    }
} 
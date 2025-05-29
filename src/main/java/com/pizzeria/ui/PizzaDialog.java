package com.pizzeria.ui;

import com.pizzeria.model.Pizza;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class PizzaDialog extends JDialog {
    private final Pizza pizza;
    private boolean validated = false;
    
    private JTextField nomField;
    private JTextField prixField;
    
    public PizzaDialog(Frame parent, String title, Pizza pizza) {
        super(parent, title, true);
        this.pizza = pizza;
        
        initComponents();
        loadData();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Panneau principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nom
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        nomField = new JTextField(20);
        mainPanel.add(nomField, gbc);
        
        // Prix
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Prix de base:"), gbc);
        gbc.gridx = 1;
        prixField = new JTextField(20);
        mainPanel.add(prixField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Annuler");
        
        okButton.addActionListener(e -> {
            if (validateForm()) {
                updatePizza();
                validated = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        if (pizza.getId() != 0) {
            nomField.setText(pizza.getNom());
            prixField.setText(pizza.getPrixBase().toString());
        }
    }
    
    private boolean validateForm() {
        if (nomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            BigDecimal prix = new BigDecimal(prixField.getText().trim());
            if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Le prix doit être supérieur à 0", "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le prix doit être un nombre valide", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void updatePizza() {
        pizza.setNom(nomField.getText().trim());
        pizza.setPrixBase(new BigDecimal(prixField.getText().trim()));
    }
    
    public boolean isValidated() {
        return validated;
    }
} 
package com.pizzeria.ui;

import com.pizzeria.model.Livreur;
import javax.swing.*;
import java.awt.*;

public class LivreurDialog extends JDialog {
    private final Livreur livreur;
    private boolean validated = false;
    
    private JTextField nomField;
    private JTextField prenomField;
    private JTextField telephoneField;
    
    public LivreurDialog(Frame parent, String title, Livreur livreur) {
        super(parent, title, true);
        this.livreur = livreur;
        
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
        
        // Prénom
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1;
        prenomField = new JTextField(20);
        mainPanel.add(prenomField, gbc);
        
        // Téléphone
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        telephoneField = new JTextField(20);
        mainPanel.add(telephoneField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Annuler");
        
        okButton.addActionListener(e -> {
            if (validateForm()) {
                updateLivreur();
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
        if (livreur.getId() != 0) {
            nomField.setText(livreur.getNom());
            prenomField.setText(livreur.getPrenom());
            telephoneField.setText(livreur.getTelephone());
        }
    }
    
    private boolean validateForm() {
        if (nomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (prenomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le prénom est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void updateLivreur() {
        livreur.setNom(nomField.getText().trim());
        livreur.setPrenom(prenomField.getText().trim());
        livreur.setTelephone(telephoneField.getText().trim());
    }
    
    public boolean isValidated() {
        return validated;
    }
} 
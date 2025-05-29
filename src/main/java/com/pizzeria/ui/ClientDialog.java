package com.pizzeria.ui;

import com.pizzeria.model.Client;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ClientDialog extends JDialog {
    private final Client client;
    private boolean validated = false;
    
    private JTextField nomField;
    private JTextField prenomField;
    private JTextArea adresseArea;
    private JTextField telephoneField;
    private JTextField soldeField;
    
    public ClientDialog(Frame parent, String title, Client client) {
        super(parent, title, true);
        this.client = client;
        
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
        
        // Adresse
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        adresseArea = new JTextArea(3, 20);
        adresseArea.setLineWrap(true);
        adresseArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(adresseArea);
        mainPanel.add(scrollPane, gbc);
        
        // Téléphone
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        telephoneField = new JTextField(20);
        mainPanel.add(telephoneField, gbc);
        
        // Solde
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Solde:"), gbc);
        gbc.gridx = 1;
        soldeField = new JTextField(20);
        mainPanel.add(soldeField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Annuler");
        
        okButton.addActionListener(e -> {
            if (validateForm()) {
                updateClient();
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
        if (client.getId() != 0) {
            nomField.setText(client.getNom());
            prenomField.setText(client.getPrenom());
            adresseArea.setText(client.getAdresse());
            telephoneField.setText(client.getTelephone());
            soldeField.setText(client.getSoldeCompte().toString());
        } else {
            soldeField.setText("0.00");
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
        if (adresseArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "L'adresse est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            new BigDecimal(soldeField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le solde doit être un nombre valide", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void updateClient() {
        client.setNom(nomField.getText().trim());
        client.setPrenom(prenomField.getText().trim());
        client.setAdresse(adresseArea.getText().trim());
        client.setTelephone(telephoneField.getText().trim());
        client.setSoldeCompte(new BigDecimal(soldeField.getText().trim()));
    }
    
    public boolean isValidated() {
        return validated;
    }
} 
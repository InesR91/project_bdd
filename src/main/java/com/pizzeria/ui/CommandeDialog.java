package com.pizzeria.ui;

import com.pizzeria.dao.ClientDAO;
import com.pizzeria.dao.PizzaDAO;
import com.pizzeria.dao.LivreurDAO;
import com.pizzeria.model.Commande;
import com.pizzeria.model.Client;
import com.pizzeria.model.Pizza;
import com.pizzeria.model.Livreur;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CommandeDialog extends JDialog {
    private final Commande commande;
    private boolean validated = false;
    
    private JComboBox<Client> clientCombo;
    private JComboBox<Pizza> pizzaCombo;
    private JComboBox<String> tailleCombo;
    private JTextField montantField;
    private JComboBox<String> statutCombo;
    private JComboBox<Livreur> livreurCombo;
    
    public CommandeDialog(Frame parent, String title, Commande commande) {
        super(parent, title, true);
        this.commande = commande;
        
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
        
        // Client
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 1;
        clientCombo = new JComboBox<>();
        mainPanel.add(clientCombo, gbc);
        
        // Pizza
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Pizza:"), gbc);
        gbc.gridx = 1;
        pizzaCombo = new JComboBox<>();
        mainPanel.add(pizzaCombo, gbc);
        
        // Taille
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Taille:"), gbc);
        gbc.gridx = 1;
        tailleCombo = new JComboBox<>(new String[]{"naine", "humaine", "ogresse"});
        mainPanel.add(tailleCombo, gbc);
        
        // Montant
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Montant:"), gbc);
        gbc.gridx = 1;
        montantField = new JTextField(10);
        montantField.setEditable(false);
        mainPanel.add(montantField, gbc);
        
        // Statut
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Statut:"), gbc);
        gbc.gridx = 1;
        statutCombo = new JComboBox<>(new String[]{"en_attente", "en_cours", "livree", "refusee"});
        mainPanel.add(statutCombo, gbc);
        
        // Livreur
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Livreur:"), gbc);
        gbc.gridx = 1;
        livreurCombo = new JComboBox<>();
        mainPanel.add(livreurCombo, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Annuler");
        
        okButton.addActionListener(e -> {
            if (validateForm()) {
                updateCommande();
                validated = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Listeners pour le calcul automatique du montant
        pizzaCombo.addActionListener(e -> calculateMontant());
        tailleCombo.addActionListener(e -> calculateMontant());
    }
    
    private void loadData() {
        try {
            // Chargement des clients
            ClientDAO clientDAO = new ClientDAO();
            List<Client> clients = clientDAO.getAllClients();
            for (Client client : clients) {
                clientCombo.addItem(client);
            }
            
            // Chargement des pizzas
            PizzaDAO pizzaDAO = new PizzaDAO();
            List<Pizza> pizzas = pizzaDAO.getAllPizzas();
            for (Pizza pizza : pizzas) {
                pizzaCombo.addItem(pizza);
            }
            
            // Chargement des livreurs
            LivreurDAO livreurDAO = new LivreurDAO();
            List<Livreur> livreurs = livreurDAO.getAllLivreurs();
            livreurCombo.addItem(null); // Option "Pas de livreur"
            for (Livreur livreur : livreurs) {
                livreurCombo.addItem(livreur);
            }
            
            // Si c'est une modification, on remplit les champs
            if (commande.getId() != 0) {
                // Sélection du client
                for (int i = 0; i < clientCombo.getItemCount(); i++) {
                    if (clientCombo.getItemAt(i).getId() == commande.getIdClient()) {
                        clientCombo.setSelectedIndex(i);
                        break;
                    }
                }
                
                // Sélection de la pizza
                for (int i = 0; i < pizzaCombo.getItemCount(); i++) {
                    if (pizzaCombo.getItemAt(i).getId() == commande.getIdPizza()) {
                        pizzaCombo.setSelectedIndex(i);
                        break;
                    }
                }
                
                // Sélection de la taille
                tailleCombo.setSelectedItem(commande.getTailleNom());
                
                // Sélection du statut
                statutCombo.setSelectedItem(commande.getStatut());
                
                // Sélection du livreur
                if (commande.getIdLivreur() != null) {
                    for (int i = 0; i < livreurCombo.getItemCount(); i++) {
                        Livreur livreur = livreurCombo.getItemAt(i);
                        if (livreur != null && livreur.getId() == commande.getIdLivreur()) {
                            livreurCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
            
            calculateMontant();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void calculateMontant() {
        Pizza selectedPizza = (Pizza) pizzaCombo.getSelectedItem();
        String selectedTaille = (String) tailleCombo.getSelectedItem();
        
        if (selectedPizza != null && selectedTaille != null) {
            BigDecimal coefficient;
            switch (selectedTaille) {
                case "naine" -> coefficient = new BigDecimal("0.67");
                case "ogresse" -> coefficient = new BigDecimal("1.33");
                default -> coefficient = BigDecimal.ONE;
            }
            
            BigDecimal montant = selectedPizza.getPrixBase().multiply(coefficient);
            montantField.setText(montant.toString() + " €");
        }
    }
    
    private boolean validateForm() {
        if (clientCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (pizzaCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une pizza", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void updateCommande() {
        Client selectedClient = (Client) clientCombo.getSelectedItem();
        Pizza selectedPizza = (Pizza) pizzaCombo.getSelectedItem();
        Livreur selectedLivreur = (Livreur) livreurCombo.getSelectedItem();
        
        commande.setIdClient(selectedClient.getId());
        commande.setIdPizza(selectedPizza.getId());
        commande.setIdTaille(tailleCombo.getSelectedIndex() + 1); // Les IDs commencent à 1
        commande.setStatut(statutCombo.getSelectedItem().toString());
        
        if (selectedLivreur != null) {
            commande.setIdLivreur(selectedLivreur.getId());
        }
        
        // Mise à jour du montant
        String montantStr = montantField.getText().replace(" €", "");
        commande.setMontantInitial(new BigDecimal(montantStr));
        commande.setMontantFinal(new BigDecimal(montantStr)); // Pour l'instant, pas de réduction
    }
    
    public boolean isValidated() {
        return validated;
    }
} 
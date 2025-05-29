package com.pizzeria.ui;

import com.pizzeria.dao.PizzaDAO;
import com.pizzeria.dao.CommandeDAO;
import com.pizzeria.dao.ClientDAO;
import com.pizzeria.dao.LivreurDAO;
import com.pizzeria.model.Pizza;
import com.pizzeria.model.Commande;
import com.pizzeria.model.Client;
import com.pizzeria.model.Livreur;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.List;
import java.sql.Timestamp;

public class MainWindow extends JFrame {
    private final PizzaDAO pizzaDAO;
    private final CommandeDAO commandeDAO;
    private final ClientDAO clientDAO;
    private final LivreurDAO livreurDAO;
    private JTable pizzaTable;
    private DefaultTableModel tableModel;
    private JTabbedPane tabbedPane;
    private JTable commandeTable;
    private DefaultTableModel commandeTableModel;
    private JTable clientTable;
    private DefaultTableModel clientTableModel;
    private JTable livreurTable;
    private DefaultTableModel livreurTableModel;
    private DefaultTableModel livraisonsTableModel;
    private JTable livraisonsTable;

    public MainWindow() {
        PizzaDAO tempPizzaDAO;
        CommandeDAO tempCommandeDAO;
        ClientDAO tempClientDAO;
        LivreurDAO tempLivreurDAO;

        try {
            tempPizzaDAO = new PizzaDAO();
            tempCommandeDAO = new CommandeDAO();
            tempClientDAO = new ClientDAO();
            tempLivreurDAO = new LivreurDAO();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Erreur de connexion à la base de données: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            throw new RuntimeException(e); // Pour satisfaire le compilateur
        }

        // Initialisation des champs finals
        this.pizzaDAO = tempPizzaDAO;
        this.commandeDAO = tempCommandeDAO;
        this.clientDAO = tempClientDAO;
        this.livreurDAO = tempLivreurDAO;

        // Initialisation de l'interface
        initializeUI();
        
        try {
        loadPizzas();
            loadCommandes();
            loadClients();
            loadLivreurs();
            loadLivraisonsEnCours();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        setTitle("RaPizz - Gestion de Pizzeria");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        // Create menu bar
        createMenuBar();

        // Create main panel with tabs
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Commandes", createCommandesPanel());
        tabbedPane.addTab("Clients", createClientsPanel());
        tabbedPane.addTab("Pizzas", createPizzasPanel());
        tabbedPane.addTab("Livraisons", createLivraisonsPanel());
        tabbedPane.addTab("Statistiques", createStatistiquesPanel());

        add(tabbedPane);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Fichier
        JMenu menuFile = new JMenu("Fichier");
        JMenuItem menuItemExit = new JMenuItem("Quitter");
        menuItemExit.addActionListener(e -> System.exit(0));
        menuFile.add(menuItemExit);
        
        // Menu Edition
        JMenu menuEdit = new JMenu("Edition");
        JMenuItem menuItemPreferences = new JMenuItem("Préférences");
        menuEdit.add(menuItemPreferences);
        
        // Menu Aide
        JMenu menuHelp = new JMenu("Aide");
        JMenuItem menuItemAbout = new JMenuItem("À propos");
        menuHelp.add(menuItemAbout);
        
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);
    }

    private JPanel createCommandesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Toolbar pour les actions
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JButton newButton = new JButton("Nouvelle commande");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        JButton refreshButton = new JButton("Rafraîchir");
        
        newButton.addActionListener(e -> createCommande());
        editButton.addActionListener(e -> editCommande());
        deleteButton.addActionListener(e -> deleteCommande());
        refreshButton.addActionListener(e -> {
            try {
                loadCommandes();
                loadLivraisonsEnCours();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du rafraîchissement: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        toolbar.add(newButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.addSeparator();
        toolbar.add(refreshButton);
        
        // Table des commandes
        String[] columns = {"N°", "Client", "Pizza", "Taille", "Statut", "Montant", "Livreur"};
        commandeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        commandeTable = new JTable(commandeTableModel);
        JScrollPane scrollPane = new JScrollPane(commandeTable);
        
        // Panneau de filtres
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Statut:"));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tous", "En attente", "En cours", "Livrée", "Refusée"});
        statusFilter.addActionListener(e -> filterCommandes(statusFilter.getSelectedItem().toString()));
        filterPanel.add(statusFilter);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void createCommande() {
        Commande newCommande = new Commande();
        CommandeDialog dialog = new CommandeDialog(this, "Nouvelle commande", newCommande);
        dialog.setVisible(true);
        
        if (dialog.isValidated()) {
            try {
                commandeDAO.create(newCommande);
                loadCommandes();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la création de la commande: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCommande() {
        int selectedRow = commandeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une commande à modifier",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int commandeId = (int) commandeTable.getValueAt(selectedRow, 0);
            Commande commande = commandeDAO.getById(commandeId);
            
            CommandeDialog dialog = new CommandeDialog(this, "Modifier la commande", commande);
            dialog.setVisible(true);
            
            if (dialog.isValidated()) {
                commandeDAO.update(commande);
                loadCommandes();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la modification de la commande: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCommande() {
        int selectedRow = commandeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une commande à supprimer",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int commandeId = (int) commandeTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer cette commande ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                commandeDAO.delete(commandeId);
                loadCommandes();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression de la commande: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadCommandes() throws SQLException {
        List<Commande> commandes = commandeDAO.getAllCommandes();
        commandeTableModel.setRowCount(0);
        for (Commande commande : commandes) {
            Object[] row = {
                commande.getId(),
                commande.getClientNom(),
                commande.getPizzaNom(),
                commande.getTailleNom(),
                commande.getStatut(),
                commande.getMontantFinal() + " €",
                commande.getLivreurNom() != null ? commande.getLivreurNom() : "-"
            };
            commandeTableModel.addRow(row);
        }
    }

    private void filterCommandes(String status) {
        try {
            List<Commande> commandes = commandeDAO.getAllCommandes();
            commandeTableModel.setRowCount(0);
            for (Commande commande : commandes) {
                if (status.equals("Tous") || commande.getStatut().equalsIgnoreCase(status)) {
                    Object[] row = {
                        commande.getId(),
                        commande.getClientNom(),
                        commande.getPizzaNom(),
                        commande.getTailleNom(),
                        commande.getStatut(),
                        commande.getMontantFinal() + " €",
                        commande.getLivreurNom() != null ? commande.getLivreurNom() : "-"
                    };
                    commandeTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du filtrage des commandes: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Toolbar pour les actions
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JButton newButton = new JButton("Nouveau client");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        JButton refreshButton = new JButton("Rafraîchir");
        
        newButton.addActionListener(e -> createClient());
        editButton.addActionListener(e -> editClient());
        deleteButton.addActionListener(e -> deleteClient());
        refreshButton.addActionListener(e -> {
            try {
                loadClients();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du rafraîchissement des données: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        toolbar.add(newButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.add(new JToolBar.Separator());
        toolbar.add(refreshButton);
        
        // Panneau de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher:"));
        JTextField searchField = new JTextField(20);
        searchField.addActionListener(e -> searchClients(searchField.getText()));
        searchPanel.add(searchField);
        
        // Table des clients
        String[] columns = {"ID", "Nom", "Prénom", "Téléphone", "Solde", "Nb Pizzas"};
        clientTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 5) return Integer.class;
                if (column == 4) return BigDecimal.class;
                return String.class;
            }
        };
        
        clientTable = new JTable(clientTableModel);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.setAutoCreateRowSorter(true);
        
        // Personnalisation de l'affichage des colonnes
        clientTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof BigDecimal) {
                    setText(value.toString() + " €");
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(clientTable);
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolbar, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void createClient() {
        Client newClient = new Client();
        ClientDialog dialog = new ClientDialog(this, "Nouveau client", newClient);
        dialog.setVisible(true);
        
        if (dialog.isValidated()) {
            try {
                clientDAO.create(newClient);
                loadClients();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la création du client: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un client à modifier",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int clientId = (int) clientTable.getValueAt(selectedRow, 0);
            Client client = clientDAO.getById(clientId);
            
            ClientDialog dialog = new ClientDialog(this, "Modifier le client", client);
            dialog.setVisible(true);
            
            if (dialog.isValidated()) {
                clientDAO.update(client);
                loadClients();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la modification du client: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un client à supprimer",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int clientId = (int) clientTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer ce client ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                clientDAO.delete(clientId);
                loadClients();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression du client: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchClients(String searchTerm) {
        try {
            List<Client> clients;
            if (searchTerm.trim().isEmpty()) {
                clients = clientDAO.getAllClients();
            } else {
                clients = clientDAO.searchClients(searchTerm);
            }
            
            clientTableModel.setRowCount(0);
            for (Client client : clients) {
                Object[] row = {
                    client.getId(),
                    client.getNom(),
                    client.getPrenom(),
                    client.getTelephone(),
                    client.getSoldeCompte(),
                    client.getNbPizzasAchetees()
                };
                clientTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la recherche: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadClients() throws SQLException {
        List<Client> clients = clientDAO.getAllClients();
        clientTableModel.setRowCount(0);
        for (Client client : clients) {
            Object[] row = {
                client.getId(),
                client.getNom(),
                client.getPrenom(),
                client.getTelephone(),
                client.getSoldeCompte(),
                client.getNbPizzasAchetees()
            };
            clientTableModel.addRow(row);
        }
    }

    private JPanel createPizzasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Toolbar pour les actions
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JButton newButton = new JButton("Nouvelle pizza");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        JButton ingredientsButton = new JButton("Gérer les ingrédients");
        
        newButton.addActionListener(e -> createPizza());
        editButton.addActionListener(e -> editPizza());
        deleteButton.addActionListener(e -> deletePizza());
        ingredientsButton.addActionListener(e -> gererIngredients());
        
        toolbar.add(newButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.add(ingredientsButton);
        
        // Table des pizzas existante
        String[] columns = {"ID", "Nom", "Prix", "Ingrédients"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pizzaTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(pizzaTable);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void createPizza() {
        Pizza newPizza = new Pizza();
        PizzaDialog dialog = new PizzaDialog(this, "Nouvelle pizza", newPizza);
        dialog.setVisible(true);
        
        if (dialog.isValidated()) {
            try {
                pizzaDAO.create(newPizza);
                loadPizzas();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la création de la pizza: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editPizza() {
        int selectedRow = pizzaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une pizza à modifier",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int pizzaId = (int) pizzaTable.getValueAt(selectedRow, 0);
            Pizza pizza = pizzaDAO.getById(pizzaId);
            
            PizzaDialog dialog = new PizzaDialog(this, "Modifier la pizza", pizza);
            dialog.setVisible(true);
            
            if (dialog.isValidated()) {
                pizzaDAO.update(pizza);
                loadPizzas();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la modification de la pizza: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePizza() {
        int selectedRow = pizzaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une pizza à supprimer",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int pizzaId = (int) pizzaTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer cette pizza ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                pizzaDAO.delete(pizzaId);
                loadPizzas();
            } catch (SQLException e) {
                String message = e.getMessage().contains("utilisée dans des commandes") ?
                    "Cette pizza ne peut pas être supprimée car elle a déjà été commandée." :
                    "Erreur lors de la suppression de la pizza: " + e.getMessage();
                
                JOptionPane.showMessageDialog(this,
                    message,
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void gererIngredients() {
        int selectedRow = pizzaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une pizza pour gérer ses ingrédients",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int pizzaId = (int) pizzaTable.getValueAt(selectedRow, 0);
            Pizza pizza = pizzaDAO.getById(pizzaId);
            
            IngredientsDialog dialog = new IngredientsDialog(this, "Gérer les ingrédients", pizza);
            dialog.setVisible(true);
            
            if (dialog.isValidated()) {
                pizzaDAO.updateIngredients(pizza);
                loadPizzas();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la gestion des ingrédients: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPizzas() throws SQLException {
            List<Pizza> pizzas = pizzaDAO.getAllPizzasWithIngredients();
            tableModel.setRowCount(0);
            for (Pizza pizza : pizzas) {
                Object[] row = {
                    pizza.getId(),
                    pizza.getNom(),
                    pizza.getPrixBase(),
                pizza.getIngredientsAsString()
            };
            tableModel.addRow(row);
        }
    }

    private JPanel createLivraisonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panneau supérieur avec deux sections
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Section des livreurs
        JPanel livreursPanel = new JPanel(new BorderLayout());
        livreursPanel.setBorder(BorderFactory.createTitledBorder("Livreurs"));
        
        // Toolbar pour les livreurs
        JToolBar livreurToolbar = new JToolBar();
        livreurToolbar.setFloatable(false);
        
        JButton newLivreurButton = new JButton("Nouveau livreur");
        JButton editLivreurButton = new JButton("Modifier");
        JButton deleteLivreurButton = new JButton("Supprimer");
        
        newLivreurButton.addActionListener(e -> createLivreur());
        editLivreurButton.addActionListener(e -> editLivreur());
        deleteLivreurButton.addActionListener(e -> deleteLivreur());
        
        livreurToolbar.add(newLivreurButton);
        livreurToolbar.add(editLivreurButton);
        livreurToolbar.add(deleteLivreurButton);
        
        // Table des livreurs
        String[] livreurColumns = {"ID", "Nom", "Prénom", "Téléphone", "Retards", "Statut"};
        livreurTableModel = new DefaultTableModel(livreurColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 4) return Integer.class;
                return String.class;
            }
        };
        
        livreurTable = new JTable(livreurTableModel);
        livreurTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        livreursPanel.add(livreurToolbar, BorderLayout.NORTH);
        livreursPanel.add(new JScrollPane(livreurTable), BorderLayout.CENTER);
        
        // Section des livraisons en cours
        JPanel livraisonsPanel = new JPanel(new BorderLayout());
        livraisonsPanel.setBorder(BorderFactory.createTitledBorder("Livraisons en cours"));
        
        // Table des livraisons
        String[] columns = {"N° Commande", "Client", "Adresse", "Livreur", "Temps écoulé"};
        livraisonsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        livraisonsTable = new JTable(livraisonsTableModel);
        
        // Boutons d'action pour les livraisons
        JToolBar livraisonsToolbar = new JToolBar();
        livraisonsToolbar.setFloatable(false);
        
        JButton terminerButton = new JButton("Terminer la livraison");
        JButton retardButton = new JButton("Signaler un retard");
        JButton rafraichirButton = new JButton("Rafraîchir");
        
        terminerButton.addActionListener(e -> terminerLivraison());
        retardButton.addActionListener(e -> signalerRetard());
        rafraichirButton.addActionListener(e -> loadLivraisonsEnCours());
        
        livraisonsToolbar.add(terminerButton);
        livraisonsToolbar.add(retardButton);
        livraisonsToolbar.add(rafraichirButton);
        
        livraisonsPanel.add(livraisonsToolbar, BorderLayout.NORTH);
        livraisonsPanel.add(new JScrollPane(livraisonsTable), BorderLayout.CENTER);
        
        // Ajout des deux sections au panneau supérieur
        topPanel.add(livreursPanel);
        topPanel.add(livraisonsPanel);
        
        panel.add(topPanel, BorderLayout.CENTER);

        // Timer pour rafraîchir automatiquement les livraisons toutes les minutes
        Timer timer = new Timer(60000, e -> loadLivraisonsEnCours());
        timer.start();
        
        return panel;
    }

    private void createLivreur() {
        Livreur newLivreur = new Livreur();
        LivreurDialog dialog = new LivreurDialog(this, "Nouveau livreur", newLivreur);
        dialog.setVisible(true);
        
        if (dialog.isValidated()) {
            try {
                livreurDAO.create(newLivreur);
                loadLivreurs();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la création du livreur: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editLivreur() {
        int selectedRow = livreurTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un livreur à modifier",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int livreurId = (int) livreurTable.getValueAt(selectedRow, 0);
            Livreur livreur = livreurDAO.getById(livreurId);
            
            LivreurDialog dialog = new LivreurDialog(this, "Modifier le livreur", livreur);
            dialog.setVisible(true);
            
            if (dialog.isValidated()) {
                livreurDAO.update(livreur);
                loadLivreurs();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la modification du livreur: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteLivreur() {
        int selectedRow = livreurTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un livreur à supprimer",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int livreurId = (int) livreurTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer ce livreur ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                livreurDAO.delete(livreurId);
                loadLivreurs();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression du livreur: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadLivraisonsEnCours() {
        try {
            List<Commande> livraisons = commandeDAO.getLivraisonsEnCours();
            livraisonsTableModel.setRowCount(0);
            for (Commande livraison : livraisons) {
                Object[] row = {
                    livraison.getId(),
                    livraison.getClientNom(),
                    livraison.getClientAdresse(),
                    livraison.getLivreurNom(),
                    livraison.getTempsEcoule() + " min"
                };
                livraisonsTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des livraisons: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void terminerLivraison() {
        int selectedRow = livraisonsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une livraison à terminer",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int commandeId = (int) livraisonsTable.getValueAt(selectedRow, 0);
            Commande commande = commandeDAO.getById(commandeId);
            commande.setStatut("livree");
            commande.setDateLivraison(new Timestamp(System.currentTimeMillis()));
            
            commandeDAO.update(commande);
            loadLivreurs();
            loadLivraisonsEnCours();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la terminaison de la livraison: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void signalerRetard() {
        int selectedRow = livraisonsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une livraison en retard",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int commandeId = (int) livraisonsTable.getValueAt(selectedRow, 0);
            Commande commande = commandeDAO.getById(commandeId);
            
            // Incrémente le nombre de retards du livreur
            livreurDAO.incrementRetards(commande.getIdLivreur());
            
            // Met à jour le statut de la commande
            commande.setStatut("en_retard");
            commandeDAO.update(commande);
            
            loadLivreurs();
            loadLivraisonsEnCours();
            
            JOptionPane.showMessageDialog(this,
                "Le retard a été enregistré",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du signalement du retard: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLivreurs() throws SQLException {
        List<Livreur> livreurs = livreurDAO.getAllLivreurs();
        livreurTableModel.setRowCount(0);
        for (Livreur livreur : livreurs) {
            Object[] row = {
                livreur.getId(),
                livreur.getNom(),
                livreur.getPrenom(),
                livreur.getTelephone(),
                livreur.getNombreRetards(),
                livreur.isDisponible() ? "Disponible" : "En livraison"
            };
            livreurTableModel.addRow(row);
        }
    }

    private JPanel createStatistiquesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panneau des statistiques
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Cartes des statistiques
        statsPanel.add(createStatCard("Chiffre d'affaires", "0 €"));
        statsPanel.add(createStatCard("Commandes du jour", "0"));
        statsPanel.add(createStatCard("Pizza la plus vendue", "-"));
        statsPanel.add(createStatCard("Temps moyen de livraison", "0 min"));
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        return panel;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainWindow().setVisible(true);
        });
    }
} 
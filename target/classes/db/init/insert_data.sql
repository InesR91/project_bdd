-- Fichier d'insertion des données initiales
-- Ce fichier doit être exécuté après create_tables.sql

-- Insertion des tailles de pizza
INSERT INTO Taille (nom, multiplicateur) VALUES 
    ('Petite', 0.75),
    ('Moyenne', 1.0),
    ('Grande', 1.5)
ON CONFLICT (nom) DO UPDATE SET multiplicateur = EXCLUDED.multiplicateur;

-- Insertion des ingrédients de base
INSERT INTO Ingredient (nom, stock, unite, nb_utilisations) VALUES 
    ('Sauce tomate', 1000.0, 'cl', 0),
    ('Mozzarella', 1000.0, 'g', 0),
    ('Jambon', 1000.0, 'g', 0),
    ('Champignons', 1000.0, 'g', 0),
    ('Olives', 1000.0, 'g', 0),
    ('Basilic', 1000.0, 'g', 0),
    ('Pepperoni', 1000.0, 'g', 0),
    ('Oignons', 1000.0, 'g', 0),
    ('Poivrons', 1000.0, 'g', 0),
    ('Anchois', 1000.0, 'g', 0)
ON CONFLICT (nom) DO UPDATE SET 
    stock = Ingredient.stock + EXCLUDED.stock,
    nb_utilisations = Ingredient.nb_utilisations;

-- Insertion des pizzas de base
INSERT INTO Pizza (nom, prix_base) VALUES 
    ('Margherita', 8.0),
    ('Regina', 10.0),
    ('Napolitaine', 9.0),
    ('Pepperoni', 11.0),
    ('Végétarienne', 9.5)
ON CONFLICT (nom) DO UPDATE SET prix_base = EXCLUDED.prix_base;

-- Association des ingrédients aux pizzas
INSERT INTO Pizza_Ingredient (id_pizza, id_ingredient, quantite) VALUES 
    -- Margherita
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Margherita'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Sauce tomate'), 100.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Margherita'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Mozzarella'), 125.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Margherita'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Basilic'), 10.0),
     
    -- Regina
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Regina'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Sauce tomate'), 100.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Regina'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Mozzarella'), 125.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Regina'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Jambon'), 75.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Regina'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Champignons'), 50.0),

    -- Napolitaine
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Napolitaine'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Sauce tomate'), 100.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Napolitaine'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Mozzarella'), 125.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Napolitaine'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Anchois'), 50.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Napolitaine'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Olives'), 30.0),

    -- Pepperoni
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Pepperoni'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Sauce tomate'), 100.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Pepperoni'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Mozzarella'), 125.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Pepperoni'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Pepperoni'), 100.0),

    -- Végétarienne
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Végétarienne'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Sauce tomate'), 100.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Végétarienne'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Mozzarella'), 125.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Végétarienne'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Champignons'), 50.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Végétarienne'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Poivrons'), 50.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Végétarienne'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Oignons'), 40.0),
    ((SELECT id_pizza FROM Pizza WHERE nom = 'Végétarienne'), 
     (SELECT id_ingredient FROM Ingredient WHERE nom = 'Olives'), 30.0)
ON CONFLICT (id_pizza, id_ingredient) DO UPDATE SET quantite = EXCLUDED.quantite;

-- Insertion des véhicules
INSERT INTO Vehicule (immatriculation, type) VALUES 
    ('AA-123-BB', 'Scooter'),
    ('CC-456-DD', 'Scooter'),
    ('EE-789-FF', 'Vélo électrique'),
    ('GG-012-HH', 'Scooter'),
    ('II-345-JJ', 'Vélo électrique')
ON CONFLICT (immatriculation) DO UPDATE SET type = EXCLUDED.type;

-- Insertion de quelques livreurs
INSERT INTO Livreur (nom, prenom, telephone) VALUES 
    ('Dupont', 'Jean', '0601020304'),
    ('Martin', 'Sophie', '0605060708'),
    ('Dubois', 'Pierre', '0609101112'),
    ('Leroy', 'Marie', '0613141516')
ON CONFLICT (telephone) DO UPDATE SET 
    nom = EXCLUDED.nom,
    prenom = EXCLUDED.prenom;

-- Insertion des clients
INSERT INTO Client (nom, prenom, adresse, telephone, solde_compte, nb_pizzas_achetees) VALUES 
    ('Bernard', 'Alice', '12 rue des Lilas, 75001 Paris', '0601234567', 50.0, 0),
    ('Thomas', 'Marc', '45 avenue de la République, 75011 Paris', '0602345678', 30.0, 0),
    ('Petit', 'Sophie', '8 rue du Commerce, 75015 Paris', '0603456789', 25.0, 0),
    ('Robert', 'Julie', '23 rue de la Paix, 75002 Paris', '0604567890', 40.0, 0),
    ('Durand', 'Pierre', '56 boulevard Saint-Michel, 75005 Paris', '0605678901', 35.0, 0)
ON CONFLICT (telephone) DO UPDATE SET 
    nom = EXCLUDED.nom,
    prenom = EXCLUDED.prenom,
    adresse = EXCLUDED.adresse,
    solde_compte = Client.solde_compte;

-- Insertion de quelques commandes
INSERT INTO Vente (id_client, id_pizza, id_taille, id_livreur, id_vehicule, date_commande, montant_initial, montant_final, statut) VALUES 
    ((SELECT id_client FROM Client WHERE telephone = '0601234567'),
     (SELECT id_pizza FROM Pizza WHERE nom = 'Margherita'),
     (SELECT id_taille FROM Taille WHERE nom = 'Moyenne'),
     (SELECT id_livreur FROM Livreur WHERE telephone = '0601020304'),
     (SELECT id_vehicule FROM Vehicule WHERE immatriculation = 'AA-123-BB'),
     CURRENT_TIMESTAMP - INTERVAL '1 hour',
     8.0,
     8.0,
     'livré'),

    ((SELECT id_client FROM Client WHERE telephone = '0602345678'),
     (SELECT id_pizza FROM Pizza WHERE nom = 'Regina'),
     (SELECT id_taille FROM Taille WHERE nom = 'Grande'),
     (SELECT id_livreur FROM Livreur WHERE telephone = '0605060708'),
     (SELECT id_vehicule FROM Vehicule WHERE immatriculation = 'CC-456-DD'),
     CURRENT_TIMESTAMP - INTERVAL '30 minutes',
     15.0,
     15.0,
     'en_cours'),

    ((SELECT id_client FROM Client WHERE telephone = '0603456789'),
     (SELECT id_pizza FROM Pizza WHERE nom = 'Pepperoni'),
     (SELECT id_taille FROM Taille WHERE nom = 'Petite'),
     (SELECT id_livreur FROM Livreur WHERE telephone = '0609101112'),
     (SELECT id_vehicule FROM Vehicule WHERE immatriculation = 'EE-789-FF'),
     CURRENT_TIMESTAMP - INTERVAL '15 minutes',
     8.25,
     8.25,
     'en_preparation'); 
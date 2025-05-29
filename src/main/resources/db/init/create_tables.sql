-- Fichier de création des tables de la base de données
-- Ce fichier doit être exécuté en premier

-- Création des tables
CREATE TABLE IF NOT EXISTS Client (
    id_client SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    adresse TEXT NOT NULL,
    telephone VARCHAR(15) NOT NULL,
    solde_compte DECIMAL(10,2) DEFAULT 0.0,
    nb_pizzas_achetees INTEGER DEFAULT 0,
    UNIQUE(telephone)
);

CREATE TABLE IF NOT EXISTS Taille (
    id_taille SERIAL PRIMARY KEY,
    nom VARCHAR(20) NOT NULL,
    multiplicateur DECIMAL(3,2) DEFAULT 1.0,
    UNIQUE(nom)
);

CREATE TABLE IF NOT EXISTS Ingredient (
    id_ingredient SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    stock DECIMAL(10,2) DEFAULT 0.0,
    unite VARCHAR(20) NOT NULL,
    nb_utilisations INTEGER DEFAULT 0,
    UNIQUE(nom)
);

CREATE TABLE IF NOT EXISTS Pizza (
    id_pizza SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prix_base DECIMAL(10,2) NOT NULL,
    UNIQUE(nom)
);

CREATE TABLE IF NOT EXISTS Pizza_Ingredient (
    id_pizza INTEGER REFERENCES Pizza(id_pizza),
    id_ingredient INTEGER REFERENCES Ingredient(id_ingredient),
    quantite DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_pizza, id_ingredient)
);

CREATE TABLE IF NOT EXISTS Vehicule (
    id_vehicule SERIAL PRIMARY KEY,
    immatriculation VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    disponible BOOLEAN DEFAULT true,
    UNIQUE(immatriculation)
);

CREATE TABLE IF NOT EXISTS Livreur (
    id_livreur SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    telephone VARCHAR(15) NOT NULL,
    nombre_retards INTEGER DEFAULT 0,
    disponible BOOLEAN DEFAULT true,
    UNIQUE(telephone)
);

CREATE TABLE IF NOT EXISTS Vente (
    id_vente SERIAL PRIMARY KEY,
    id_client INTEGER REFERENCES Client(id_client),
    id_pizza INTEGER REFERENCES Pizza(id_pizza),
    id_taille INTEGER REFERENCES Taille(id_taille),
    id_livreur INTEGER REFERENCES Livreur(id_livreur),
    id_vehicule INTEGER REFERENCES Vehicule(id_vehicule),
    date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_livraison TIMESTAMP,
    montant_initial DECIMAL(10,2) NOT NULL,
    montant_final DECIMAL(10,2) NOT NULL,
    statut VARCHAR(20) DEFAULT 'en_attente'
); 
-- Script de réinitialisation sécurisée de la base de données

-- 1. Fermer toutes les connexions existantes sauf la nôtre
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = current_database()
  AND pid <> pg_backend_pid();

-- 2. Supprimer les triggers existants
DROP TRIGGER IF EXISTS trg_update_livreur_disponibilite ON Vente;
DROP TRIGGER IF EXISTS trg_update_ingredient_stock ON Vente;
DROP TRIGGER IF EXISTS trg_update_client_statistiques ON Vente;
DROP TRIGGER IF EXISTS trg_check_ingredients_disponibilite ON Vente;

-- 3. Supprimer les fonctions existantes
DROP FUNCTION IF EXISTS update_livreur_disponibilite() CASCADE;
DROP FUNCTION IF EXISTS update_ingredient_stock() CASCADE;
DROP FUNCTION IF EXISTS update_client_statistiques() CASCADE;
DROP FUNCTION IF EXISTS check_ingredients_disponibilite() CASCADE;
DROP FUNCTION IF EXISTS calculer_montant_commande(INTEGER, INTEGER) CASCADE;
DROP FUNCTION IF EXISTS verifier_disponibilite_ingredients(INTEGER, INTEGER) CASCADE;
DROP FUNCTION IF EXISTS get_next_available_livreur() CASCADE;

-- 4. Supprimer les tables dans l'ordre correct
DROP TABLE IF EXISTS Vente CASCADE;
DROP TABLE IF EXISTS Pizza_Ingredient CASCADE;
DROP TABLE IF EXISTS Pizza CASCADE;
DROP TABLE IF EXISTS Ingredient CASCADE;
DROP TABLE IF EXISTS Taille CASCADE;
DROP TABLE IF EXISTS Livreur CASCADE;
DROP TABLE IF EXISTS Vehicule CASCADE;
DROP TABLE IF EXISTS Client CASCADE;

-- 5. Réinitialiser les séquences
DROP SEQUENCE IF EXISTS client_id_client_seq CASCADE;
DROP SEQUENCE IF EXISTS taille_id_taille_seq CASCADE;
DROP SEQUENCE IF EXISTS ingredient_id_ingredient_seq CASCADE;
DROP SEQUENCE IF EXISTS pizza_id_pizza_seq CASCADE;
DROP SEQUENCE IF EXISTS vehicule_id_vehicule_seq CASCADE;
DROP SEQUENCE IF EXISTS livreur_id_livreur_seq CASCADE;
DROP SEQUENCE IF EXISTS vente_id_vente_seq CASCADE; 
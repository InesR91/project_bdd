-- Script de nettoyage de la base de données

-- Suppression des commandes sans client valide
DELETE FROM Vente
WHERE id_client NOT IN (SELECT id_client FROM Client);

-- Suppression des commandes avec des pizzas qui n'existent plus
DELETE FROM Vente
WHERE id_pizza NOT IN (SELECT id_pizza FROM Pizza);

-- Suppression des commandes avec des tailles qui n'existent plus
DELETE FROM Vente
WHERE id_taille NOT IN (SELECT id_taille FROM Taille);

-- Mise à jour des commandes avec des livreurs qui n'existent plus
UPDATE Vente
SET id_livreur = NULL
WHERE id_livreur IS NOT NULL 
AND id_livreur NOT IN (SELECT id_livreur FROM Livreur);

-- Mise à jour des commandes avec des véhicules qui n'existent plus
UPDATE Vente
SET id_vehicule = NULL
WHERE id_vehicule IS NOT NULL 
AND id_vehicule NOT IN (SELECT id_vehicule FROM Vehicule);

-- Mise à jour du statut des commandes anciennes sans livraison
UPDATE Vente
SET statut = 'annulee'
WHERE date_commande < CURRENT_TIMESTAMP - INTERVAL '24 hours'
AND statut = 'en_attente'; 
-- Fichier des requêtes utilitaires

-- Liste de tous les clients
SELECT id_client, nom, prenom, adresse, telephone, solde_compte, nb_pizzas_achetees
FROM Client
ORDER BY nom, prenom;

-- Recherche de clients par nom ou prénom
SELECT id_client, nom, prenom, adresse, telephone, solde_compte, nb_pizzas_achetees
FROM Client
ORDER BY nom, prenom;

-- Liste des commandes en cours avec temps écoulé
SELECT v.id_vente, c.nom as client_nom, c.prenom as client_prenom,
       c.adresse as client_adresse,
       l.nom as livreur_nom, l.prenom as livreur_prenom,
       EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - v.date_commande))/60 as temps_ecoule
FROM Vente v
JOIN Client c ON v.id_client = c.id_client
JOIN Livreur l ON v.id_livreur = l.id_livreur
WHERE v.statut = 'en_cours'
ORDER BY v.date_commande ASC;

-- Liste des ingrédients à réapprovisionner
SELECT nom, stock, unite
FROM Ingredient
WHERE stock < 200.0
ORDER BY stock ASC;

-- Liste des livreurs disponibles
SELECT id_livreur, nom, prenom, nombre_retards
FROM Livreur
WHERE disponible = true
ORDER BY nombre_retards ASC;

-- Liste des véhicules disponibles
SELECT id_vehicule, immatriculation, type
FROM Vehicule
WHERE disponible = true;

-- Historique des commandes d'un client
SELECT v.id_vente, v.date_commande, p.nom as pizza_nom, 
       t.nom as taille_nom, v.montant_final, v.statut
FROM Vente v
JOIN Pizza p ON v.id_pizza = p.id_pizza
JOIN Taille t ON v.id_taille = t.id_taille
WHERE v.id_client = 1  -- Remplacez 1 par l'ID du client souhaité
ORDER BY v.date_commande DESC;

-- Détails d'une commande
SELECT v.id_vente, v.date_commande, v.date_livraison,
       c.nom as client_nom, c.prenom as client_prenom,
       p.nom as pizza_nom, t.nom as taille_nom,
       l.nom as livreur_nom, l.prenom as livreur_prenom,
       ve.immatriculation as vehicule,
       v.montant_initial, v.montant_final, v.statut
FROM Vente v
JOIN Client c ON v.id_client = c.id_client
JOIN Pizza p ON v.id_pizza = p.id_pizza
JOIN Taille t ON v.id_taille = t.id_taille
LEFT JOIN Livreur l ON v.id_livreur = l.id_livreur
LEFT JOIN Vehicule ve ON v.id_vehicule = ve.id_vehicule
WHERE v.id_vente = 1;  -- Remplacez 1 par l'ID de la commande souhaitée 
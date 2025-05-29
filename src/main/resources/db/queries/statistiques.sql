-- Fichier des requêtes pour les statistiques

-- Chiffre d'affaires total
SELECT SUM(montant_final) as chiffre_affaires_total
FROM Vente
WHERE statut = 'livree';

-- Chiffre d'affaires par jour
SELECT DATE(date_commande) as jour,
       SUM(montant_final) as chiffre_affaires
FROM Vente
WHERE statut = 'livree'
GROUP BY jour
ORDER BY jour DESC;

-- Nombre de pizzas vendues par type
SELECT p.nom as pizza,
       COUNT(*) as nombre_ventes,
       SUM(v.montant_final) as chiffre_affaires
FROM Vente v
JOIN Pizza p ON v.id_pizza = p.id_pizza
WHERE v.statut = 'livree'
GROUP BY p.nom
ORDER BY nombre_ventes DESC;

-- Performance des livreurs
SELECT l.nom, l.prenom,
       COUNT(*) as livraisons_effectuees,
       AVG(EXTRACT(EPOCH FROM (v.date_livraison - v.date_commande))/60) as temps_moyen_minutes,
       l.nombre_retards
FROM Vente v
JOIN Livreur l ON v.id_livreur = l.id_livreur
WHERE v.statut = 'livree'
GROUP BY l.id_livreur, l.nom, l.prenom, l.nombre_retards
ORDER BY livraisons_effectuees DESC;

-- Top 10 des clients fidèles
SELECT c.nom, c.prenom,
       c.nb_pizzas_achetees,
       SUM(v.montant_final) as montant_total_depense
FROM Client c
JOIN Vente v ON c.id_client = v.id_client
WHERE v.statut = 'livree'
GROUP BY c.id_client, c.nom, c.prenom, c.nb_pizzas_achetees
ORDER BY c.nb_pizzas_achetees DESC
LIMIT 10;

-- Statistiques par taille de pizza
SELECT t.nom as taille,
       COUNT(*) as nombre_ventes,
       SUM(v.montant_final) as chiffre_affaires
FROM Vente v
JOIN Taille t ON v.id_taille = t.id_taille
WHERE v.statut = 'livree'
GROUP BY t.nom
ORDER BY nombre_ventes DESC;

-- Temps moyen de livraison par période
SELECT 
    CASE 
        WHEN EXTRACT(HOUR FROM date_commande) BETWEEN 11 AND 14 THEN 'Déjeuner'
        WHEN EXTRACT(HOUR FROM date_commande) BETWEEN 18 AND 22 THEN 'Dîner'
        ELSE 'Hors pic'
    END as periode,
    AVG(EXTRACT(EPOCH FROM (date_livraison - date_commande))/60) as temps_moyen_minutes,
    COUNT(*) as nombre_livraisons
FROM Vente
WHERE statut = 'livree'
GROUP BY periode
ORDER BY nombre_livraisons DESC; 
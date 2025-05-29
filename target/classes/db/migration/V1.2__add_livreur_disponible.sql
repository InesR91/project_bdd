-- Migration V1.2 : Ajout de la colonne disponible pour les livreurs
ALTER TABLE Livreur ADD COLUMN IF NOT EXISTS disponible BOOLEAN DEFAULT true;

-- Mise à jour des livreurs existants
UPDATE Livreur SET disponible = true;

-- Mise à jour des livreurs qui sont actuellement en livraison
UPDATE Livreur 
SET disponible = false 
WHERE id_livreur IN (
    SELECT DISTINCT id_livreur 
    FROM Vente 
    WHERE statut = 'en_cours'
); 
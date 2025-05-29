-- Ajout de la colonne multiplicateur
ALTER TABLE Taille ADD COLUMN IF NOT EXISTS multiplicateur DECIMAL(3,2) DEFAULT 1.0;

-- Mise à jour des valeurs par défaut
UPDATE Taille SET multiplicateur = 1.5 WHERE nom = 'Grande';
UPDATE Taille SET multiplicateur = 1.0 WHERE nom = 'Moyenne';
UPDATE Taille SET multiplicateur = 0.75 WHERE nom = 'Petite'; 
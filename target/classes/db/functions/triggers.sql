-- Triggers pour la gestion automatique de la base de données

-- Supprimer tous les triggers existants
DROP TRIGGER IF EXISTS trg_update_livreur_disponibilite ON Vente;
DROP TRIGGER IF EXISTS trg_update_ingredient_stock ON Vente;
DROP TRIGGER IF EXISTS trg_update_client_statistiques ON Vente;
DROP TRIGGER IF EXISTS trg_check_ingredients_disponibilite ON Vente;

-- Supprimer toutes les fonctions existantes
DROP FUNCTION IF EXISTS update_livreur_disponibilite() CASCADE;
DROP FUNCTION IF EXISTS update_ingredient_stock() CASCADE;
DROP FUNCTION IF EXISTS update_client_statistiques() CASCADE;
DROP FUNCTION IF EXISTS check_ingredients_disponibilite() CASCADE;

-- Trigger pour mettre à jour la disponibilité du livreur
CREATE OR REPLACE FUNCTION update_livreur_disponibilite()
RETURNS TRIGGER AS $$
BEGIN
    -- Si une nouvelle commande est créée
    IF (TG_OP = 'INSERT') THEN
        -- Vérifier si le livreur est disponible
        IF NOT EXISTS (SELECT 1 FROM Livreur WHERE id_livreur = NEW.id_livreur AND disponible = TRUE) THEN
            RAISE EXCEPTION 'Le livreur n''est pas disponible';
        END IF;
        -- Marquer le livreur comme non disponible
        UPDATE Livreur SET disponible = FALSE 
        WHERE id_livreur = NEW.id_livreur;
    
    -- Si une commande est terminée
    ELSIF (TG_OP = 'UPDATE') THEN
        -- Rendre le livreur disponible sans condition
        UPDATE Livreur SET disponible = TRUE
        WHERE id_livreur = NEW.id_livreur;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_livreur_disponibilite
AFTER INSERT OR UPDATE ON Vente
FOR EACH ROW
EXECUTE FUNCTION update_livreur_disponibilite();

-- Trigger pour mettre à jour les stocks d'ingrédients et leurs utilisations
CREATE OR REPLACE FUNCTION update_ingredient_stock()
RETURNS TRIGGER AS $$
DECLARE
    r_ingredient RECORD;
BEGIN
    -- Pour chaque ingrédient de la pizza
    FOR r_ingredient IN (
        SELECT 
            pi.id_ingredient,
            pi.quantite,
            CASE 
                WHEN t.nom = 'Grande' THEN pi.quantite * 1.5
                WHEN t.nom = 'Moyenne' THEN pi.quantite
                WHEN t.nom = 'Petite' THEN pi.quantite * 0.75
                ELSE pi.quantite
            END as quantite_finale
        FROM Pizza_Ingredient pi
        JOIN Taille t ON t.id_taille = NEW.id_taille
        WHERE pi.id_pizza = NEW.id_pizza
    ) LOOP
        -- Mettre à jour le stock et le nombre d'utilisations
        UPDATE Ingredient 
        SET stock = stock - r_ingredient.quantite_finale,
            nb_utilisations = nb_utilisations + 1
        WHERE id_ingredient = r_ingredient.id_ingredient;
    END LOOP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_ingredient_stock
AFTER INSERT ON Vente
FOR EACH ROW
EXECUTE FUNCTION update_ingredient_stock();

-- Trigger pour mettre à jour les statistiques client
CREATE OR REPLACE FUNCTION update_client_statistiques()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Client 
    SET nb_pizzas_achetees = nb_pizzas_achetees + 1,
        solde_compte = solde_compte - NEW.montant_final
    WHERE id_client = NEW.id_client;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_client_statistiques
AFTER INSERT ON Vente
FOR EACH ROW
EXECUTE FUNCTION update_client_statistiques();

-- Trigger pour vérifier la disponibilité des ingrédients avant une commande
CREATE OR REPLACE FUNCTION check_ingredients_disponibilite()
RETURNS TRIGGER AS $$
DECLARE
    stock_insuffisant BOOLEAN := FALSE;
    ingredient_manquant VARCHAR(50);
BEGIN
    SELECT INTO stock_insuffisant, ingredient_manquant
        CASE WHEN MIN(i.stock - (
            CASE 
                WHEN t.nom = 'Grande' THEN pi.quantite * 1.5
                WHEN t.nom = 'Moyenne' THEN pi.quantite
                WHEN t.nom = 'Petite' THEN pi.quantite * 0.75
                ELSE pi.quantite
            END
        )) < 0 THEN TRUE ELSE FALSE END,
        MIN(i.nom)
    FROM Pizza_Ingredient pi
    JOIN Ingredient i ON i.id_ingredient = pi.id_ingredient
    JOIN Taille t ON t.id_taille = NEW.id_taille
    WHERE pi.id_pizza = NEW.id_pizza
    HAVING MIN(i.stock - (
        CASE 
            WHEN t.nom = 'Grande' THEN pi.quantite * 1.5
            WHEN t.nom = 'Moyenne' THEN pi.quantite
            WHEN t.nom = 'Petite' THEN pi.quantite * 0.75
            ELSE pi.quantite
        END
    )) < 0;

    IF stock_insuffisant THEN
        RAISE EXCEPTION 'Stock insuffisant pour l''ingrédient: %', ingredient_manquant;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_ingredients_disponibilite
BEFORE INSERT ON Vente
FOR EACH ROW
EXECUTE FUNCTION check_ingredients_disponibilite(); 
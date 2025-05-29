-- Fichier des procédures stockées et fonctions

-- Fonction pour calculer le montant total d'une commande
CREATE OR REPLACE FUNCTION calculer_montant_commande(
    p_id_pizza INTEGER,
    p_id_taille INTEGER
) RETURNS DECIMAL(10,2) AS $$
DECLARE
    v_prix_base DECIMAL(10,2);
    v_multiplicateur DECIMAL(3,2);
BEGIN
    SELECT prix_base INTO v_prix_base
    FROM Pizza
    WHERE id_pizza = p_id_pizza;
    
    SELECT multiplicateur INTO v_multiplicateur
    FROM Taille
    WHERE id_taille = p_id_taille;
    
    RETURN v_prix_base * v_multiplicateur;
END;
$$ LANGUAGE plpgsql;

-- Procédure pour mettre à jour le stock des ingrédients lors d'une commande
CREATE OR REPLACE PROCEDURE update_ingredients_stock(
    p_id_vente INTEGER
) AS $$
DECLARE
    r_ingredient RECORD;
BEGIN
    FOR r_ingredient IN (
        SELECT 
            pi.id_ingredient,
            CASE 
                WHEN t.nom = 'Grande' THEN pi.quantite * 1.5
                WHEN t.nom = 'Moyenne' THEN pi.quantite * 1.0
                WHEN t.nom = 'Petite' THEN pi.quantite * 0.75
                ELSE pi.quantite
            END as quantite_totale
        FROM Pizza_Ingredient pi
        JOIN Vente v ON v.id_pizza = pi.id_pizza
        JOIN Taille t ON t.id_taille = v.id_taille
        WHERE v.id_vente = p_id_vente
    ) LOOP
        UPDATE Ingredient 
        SET stock = stock - r_ingredient.quantite_totale
        WHERE id_ingredient = r_ingredient.id_ingredient;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Fonction pour vérifier la disponibilité des ingrédients
CREATE OR REPLACE FUNCTION verifier_disponibilite_ingredients(
    p_id_pizza INTEGER,
    p_id_taille INTEGER
) RETURNS BOOLEAN AS $$
DECLARE
    r_ingredient RECORD;
    v_quantite_necessaire DECIMAL(10,2);
BEGIN
    FOR r_ingredient IN (
        SELECT i.id_ingredient, i.stock, pi.quantite,
               CASE 
                   WHEN t.nom = 'Grande' THEN pi.quantite * 1.5
                   WHEN t.nom = 'Moyenne' THEN pi.quantite * 1.0
                   WHEN t.nom = 'Petite' THEN pi.quantite * 0.75
                   ELSE pi.quantite
               END as quantite_necessaire
        FROM Pizza_Ingredient pi
        JOIN Ingredient i ON i.id_ingredient = pi.id_ingredient
        CROSS JOIN Taille t
        WHERE pi.id_pizza = p_id_pizza AND t.id_taille = p_id_taille
    ) LOOP
        IF r_ingredient.stock < r_ingredient.quantite_necessaire THEN
            RETURN FALSE;
        END IF;
    END LOOP;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Procédure pour mettre à jour les statistiques du client
CREATE OR REPLACE PROCEDURE update_client_stats(
    p_id_client INTEGER,
    p_montant DECIMAL(10,2)
) AS $$
BEGIN
    UPDATE Client 
    SET nb_pizzas_achetees = nb_pizzas_achetees + 1,
        solde_compte = solde_compte - p_montant
    WHERE id_client = p_id_client;
END;
$$ LANGUAGE plpgsql;

-- Fonction pour obtenir le prochain livreur disponible
CREATE OR REPLACE FUNCTION get_next_available_livreur() 
RETURNS INTEGER AS $$
DECLARE
    v_id_livreur INTEGER;
BEGIN
    SELECT id_livreur INTO v_id_livreur
    FROM Livreur
    WHERE disponible = true
    ORDER BY nombre_retards ASC, RANDOM()
    LIMIT 1;
    
    RETURN v_id_livreur;
END;
$$ LANGUAGE plpgsql; 
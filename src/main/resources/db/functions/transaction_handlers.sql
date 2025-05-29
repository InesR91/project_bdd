-- Gestionnaires de transactions pour éviter les déconnexions

-- Fonction pour gérer l'insertion d'un client de manière sécurisée
CREATE OR REPLACE FUNCTION insert_client_secure(
    p_nom VARCHAR(50),
    p_prenom VARCHAR(50),
    p_adresse TEXT,
    p_telephone VARCHAR(15),
    p_solde_compte DECIMAL(10,2) DEFAULT 0.0
) RETURNS INTEGER AS $$
DECLARE
    v_client_id INTEGER;
BEGIN
    -- Début de la transaction
    BEGIN
        INSERT INTO Client (nom, prenom, adresse, telephone, solde_compte, nb_pizzas_achetees)
        VALUES (p_nom, p_prenom, p_adresse, p_telephone, p_solde_compte, 0)
        RETURNING id_client INTO v_client_id;

        -- Si tout va bien, on valide la transaction
        RETURN v_client_id;
    EXCEPTION WHEN OTHERS THEN
        -- En cas d'erreur, on annule la transaction
        RAISE NOTICE 'Erreur lors de l''insertion du client: %', SQLERRM;
        RETURN NULL;
    END;
END;
$$ LANGUAGE plpgsql;

-- Fonction pour mettre à jour un client de manière sécurisée
CREATE OR REPLACE FUNCTION update_client_secure(
    p_id_client INTEGER,
    p_nom VARCHAR(50),
    p_prenom VARCHAR(50),
    p_adresse TEXT,
    p_telephone VARCHAR(15),
    p_solde_compte DECIMAL(10,2)
) RETURNS BOOLEAN AS $$
BEGIN
    -- Début de la transaction
    BEGIN
        UPDATE Client 
        SET nom = p_nom,
            prenom = p_prenom,
            adresse = p_adresse,
            telephone = p_telephone,
            solde_compte = p_solde_compte
        WHERE id_client = p_id_client;

        -- Si tout va bien, on valide la transaction
        RETURN TRUE;
    EXCEPTION WHEN OTHERS THEN
        -- En cas d'erreur, on annule la transaction
        RAISE NOTICE 'Erreur lors de la mise à jour du client: %', SQLERRM;
        RETURN FALSE;
    END;
END;
$$ LANGUAGE plpgsql;

-- Fonction pour supprimer un client de manière sécurisée
CREATE OR REPLACE FUNCTION delete_client_secure(
    p_id_client INTEGER
) RETURNS BOOLEAN AS $$
BEGIN
    -- Début de la transaction
    BEGIN
        -- Vérifier si le client a des commandes
        IF EXISTS (SELECT 1 FROM Vente WHERE id_client = p_id_client) THEN
            RAISE NOTICE 'Le client a des commandes associées et ne peut pas être supprimé';
            RETURN FALSE;
        END IF;

        DELETE FROM Client WHERE id_client = p_id_client;
        
        -- Si tout va bien, on valide la transaction
        RETURN TRUE;
    EXCEPTION WHEN OTHERS THEN
        -- En cas d'erreur, on annule la transaction
        RAISE NOTICE 'Erreur lors de la suppression du client: %', SQLERRM;
        RETURN FALSE;
    END;
END;
$$ LANGUAGE plpgsql;

-- Fonction pour terminer une livraison de manière sécurisée
CREATE OR REPLACE FUNCTION terminer_livraison(
    p_id_vente INTEGER
) RETURNS BOOLEAN AS $$
DECLARE
    v_statut VARCHAR(20);
BEGIN
    -- Vérifier si la vente existe et récupérer son statut
    SELECT statut INTO v_statut
    FROM Vente
    WHERE id_vente = p_id_vente;

    -- Si la vente n'existe pas
    IF NOT FOUND THEN
        RAISE NOTICE 'La commande % n''existe pas', p_id_vente;
        RETURN FALSE;
    END IF;

    -- Si la commande est déjà livrée
    IF v_statut = 'livré' THEN
        RAISE NOTICE 'La commande % est déjà livrée', p_id_vente;
        RETURN FALSE;
    END IF;

    -- Mettre à jour le statut de la commande
    UPDATE Vente 
    SET statut = 'livré',
        date_livraison = CURRENT_TIMESTAMP
    WHERE id_vente = p_id_vente;

    RETURN TRUE;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Erreur lors de la terminaison de la livraison: %', SQLERRM;
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql;

-- Exemple d'utilisation :
COMMENT ON FUNCTION insert_client_secure IS 'Pour créer un client:
SELECT insert_client_secure(''Dupont'', ''Jean'', ''1 rue de Paris'', ''0601020304'', 50.0);';

COMMENT ON FUNCTION update_client_secure IS 'Pour mettre à jour un client:
SELECT update_client_secure(1, ''Dupont'', ''Jean'', ''2 rue de Paris'', ''0601020304'', 100.0);';

COMMENT ON FUNCTION delete_client_secure IS 'Pour supprimer un client:
SELECT delete_client_secure(1);';

COMMENT ON FUNCTION terminer_livraison IS 'Pour terminer une livraison:
SELECT terminer_livraison(1); -- Où 1 est l''ID de la vente à terminer'; 
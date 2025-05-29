# Conception de la Base de Données RaPizz

## Modèle Conceptuel de Données (MCD)

### Entités

1. **CLIENT**
   - id_client (Clé primaire)
   - nom
   - prenom
   - adresse
   - telephone
   - solde_compte
   - nb_pizzas_achetees

2. **PIZZA**
   - id_pizza (Clé primaire)
   - nom
   - prix_base
   - nb_commandes

3. **INGREDIENT**
   - id_ingredient (Clé primaire)
   - nom
   - stock
   - nb_utilisations

4. **LIVREUR**
   - id_livreur (Clé primaire)
   - nom
   - prenom
   - telephone
   - nombre_retards

5. **VEHICULE**
   - id_vehicule (Clé primaire)
   - type_vehicule
   - immatriculation
   - disponible

6. **TAILLE**
   - id_taille (Clé primaire)
   - nom
   - coefficient_prix

7. **VENTE**
   - id_vente (Clé primaire)
   - date_commande
   - date_livraison
   - duree_livraison
   - montant_initial
   - montant_final
   - gratuite_retard
   - gratuite_fidelite
   - statut

### Associations

1. **COMPOSE** (PIZZA - INGREDIENT)
   - Cardinalité : N:M
   - Attribut : quantite

2. **COMMANDE** (CLIENT - VENTE)
   - Cardinalité : 1:N

3. **LIVRE** (LIVREUR - VENTE)
   - Cardinalité : 1:N

4. **UTILISE** (VEHICULE - VENTE)
   - Cardinalité : 1:N

5. **CONCERNE** (PIZZA - VENTE)
   - Cardinalité : 1:N

6. **DIMENSIONNE** (TAILLE - VENTE)
   - Cardinalité : 1:N

## Règles de Gestion

1. **Gestion des Comptes Clients**
   - Un client doit avoir un solde positif pour commander
   - Le solde est débité après la livraison
   - La 10ème pizza est gratuite

2. **Gestion des Livraisons**
   - Une livraison dépassant 30 minutes est gratuite
   - Un livreur peut utiliser différents véhicules
   - Le temps de livraison est calculé entre date_commande et date_livraison

3. **Gestion des Prix**
   - Prix final = prix_base × coefficient_taille
   - Taille "naine" : -33% du prix de base
   - Taille "humaine" : prix de base
   - Taille "ogresse" : +33% du prix de base

4. **Contraintes Métier**
   - Une vente ne concerne qu'une seule pizza
   - Les pizzas sont standardisées (pas de personnalisation)
   - Le stock des ingrédients doit être géré
   - Un véhicule ne peut être utilisé que par un livreur à la fois

## Triggers et Procédures Stockées

1. **Triggers**
   - Vérification du solde client
   - Mise à jour des compteurs (pizzas, ingrédients)
   - Gestion des pizzas gratuites
   - Mise à jour du statut des véhicules

2. **Procédures Stockées**
   - Calcul du prix final
   - Vérification de la disponibilité des ingrédients
   - Mise à jour du solde client
   - Gestion de la fidélité 
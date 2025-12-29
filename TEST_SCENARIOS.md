# Scénarios de Test - Plateforme de Gestion de Projet Collaborative

## Vue d'ensemble
Ce document décrit 7 scénarios de test complets pour valider toutes les fonctionnalités du système de gestion de projet.

---

## Scénario 1 : Gestion de l'équipe

### Objectif
Valider la gestion des membres de l'équipe avec des compétences variées et leurs disponibilités.

### Données de test
- **5 membres** avec profils diversifiés :
  1. **Alice Johnson** - Développeuse Senior Java/Database
     - Email: alice.johnson@example.com
     - Disponibilité: 40h/semaine
     - Compétences: Java (niveau 5), Database Design (niveau 4)
  
  2. **Bob Smith** - Développeur Frontend/Designer
     - Email: bob.smith@example.com
     - Disponibilité: 35h/semaine
     - Compétences: Frontend Development (niveau 5), UI/UX Design (niveau 4)
  
  3. **Carol Williams** - Testeuse QA
     - Email: carol.williams@example.com
     - Disponibilité: 40h/semaine
     - Compétences: Testing (niveau 5), Documentation (niveau 4)
  
  4. **David Brown** - Développeur Backend/API
     - Email: david.brown@example.com
     - Disponibilité: 30h/semaine
     - Compétences: Java (niveau 4), API Development (niveau 5)
  
  5. **Emma Davis** - Designer UI/UX
     - Email: emma.davis@example.com
     - Disponibilité: 40h/semaine
     - Compétences: UI/UX Design (niveau 5), Frontend (niveau 3)

### Procédure de test

#### Étape 1.1 : Ajout des membres
```bash
# Via l'interface web ou API
POST /api/members/
{
  "name": "Alice Johnson",
  "email": "alice.johnson@example.com",
  "weeklyAvailability": 40
}
# Répéter pour les 5 membres
```

#### Étape 1.2 : Assignation des compétences
```bash
# Pour chaque membre, ajouter ses compétences
POST /api/members/{memberId}/skills
{
  "skillId": 1,
  "proficiencyLevel": 5
}
```

#### Étape 1.3 : Vérifications
- [ ] Tous les membres sont visibles dans la page "Team"
- [ ] Les compétences sont correctement affichées avec leurs niveaux
- [ ] Les disponibilités hebdomadaires sont correctes
- [ ] La charge de travail initiale est à 0%
- [ ] Les emails sont uniques et valides

### Critères de succès
✅ 5 membres créés avec succès
✅ Chaque membre a au moins 2 compétences
✅ Les niveaux de compétence sont entre 1 et 5
✅ La somme des disponibilités = 185h/semaine
✅ Pas de doublons d'emails

---

## Scénario 2 : Création de projet

### Objectif
Créer un projet complexe avec 10 tâches interdépendantes, des durées variées et des compétences requises.

### Données de test

**Projet : "E-commerce Platform Development"**
- Date de début : 2025-01-01
- Date de fin : 2025-03-31
- Description : Développement d'une plateforme e-commerce complète

### 10 Tâches avec dépendances

1. **Database Schema Design** (URGENT)
   - Durée : 16h
   - Compétences : Database Design (niveau 4)
   - Dépendances : Aucune
   - Statut : TODO

2. **Backend API Development** (HIGH)
   - Durée : 40h
   - Compétences : Java (niveau 4), API Development (niveau 4)
   - Dépendances : Task #1
   - Statut : TODO

3. **Frontend Architecture** (HIGH)
   - Durée : 24h
   - Compétences : Frontend Development (niveau 4)
   - Dépendances : Aucune
   - Statut : TODO

4. **UI/UX Design** (HIGH)
   - Durée : 32h
   - Compétences : UI/UX Design (niveau 4)
   - Dépendances : Aucune
   - Statut : TODO

5. **Product Catalog Module** (MEDIUM)
   - Durée : 30h
   - Compétences : Java (niveau 3), Frontend (niveau 3)
   - Dépendances : Task #2, Task #3
   - Statut : TODO

6. **Shopping Cart Implementation** (MEDIUM)
   - Durée : 28h
   - Compétences : Java (niveau 3), Frontend (niveau 4)
   - Dépendances : Task #2, Task #3
   - Statut : TODO

7. **Payment Integration** (URGENT)
   - Durée : 35h
   - Compétences : Java (niveau 5), API Development (niveau 4)
   - Dépendances : Task #2
   - Statut : TODO

8. **User Authentication System** (HIGH)
   - Durée : 25h
   - Compétences : Java (niveau 4), Database Design (niveau 3)
   - Dépendances : Task #1, Task #2
   - Statut : TODO

9. **Comprehensive Testing** (HIGH)
   - Durée : 45h
   - Compétences : Testing (niveau 4)
   - Dépendances : Task #5, Task #6, Task #7, Task #8
   - Statut : TODO

10. **Documentation & User Manual** (MEDIUM)
    - Durée : 20h
    - Compétences : Documentation (niveau 3)
    - Dépendances : Task #9
    - Statut : TODO

### Procédure de test

#### Étape 2.1 : Créer le projet
```bash
POST /api/projects/
{
  "name": "E-commerce Platform Development",
  "description": "Complete e-commerce platform",
  "startDate": "2025-01-01",
  "endDate": "2025-03-31"
}
```

#### Étape 2.2 : Créer les tâches
```bash
POST /api/projects/{projectId}/tasks
{
  "title": "Database Schema Design",
  "description": "Design complete database schema",
  "estimatedHours": 16,
  "priority": "URGENT",
  "status": "TODO"
}
# Répéter pour les 10 tâches
```

#### Étape 2.3 : Ajouter les compétences requises
```bash
POST /api/tasks/{taskId}/skills
{
  "skillId": 5,
  "requiredLevel": 4
}
```

#### Étape 2.4 : Définir les dépendances
```bash
POST /api/tasks/{taskId}/dependencies
{
  "dependsOnTaskId": 1
}
```

#### Étape 2.5 : Vérifications
- [ ] Le projet est créé avec toutes les métadonnées
- [ ] 10 tâches sont visibles dans le projet
- [ ] Les dépendances forment un graphe cohérent (pas de cycles)
- [ ] Les durées totales = 295h
- [ ] Les priorités sont correctement définies
- [ ] Les compétences requises sont liées aux tâches

### Critères de succès
✅ 1 projet créé avec succès
✅ 10 tâches créées avec durées et priorités
✅ 15 relations de dépendances définies
✅ Pas de dépendances circulaires
✅ Structure hiérarchique valide et navigable

---

## Scénario 3 : Répartition automatique

### Objectif
Tester l'algorithme d'affectation automatique des tâches et vérifier l'équilibre des charges.

### Pré-requis
- Scénario 1 complété (5 membres)
- Scénario 2 complété (1 projet, 10 tâches)

### Procédure de test

#### Étape 3.1 : Lancer l'allocation automatique
```bash
POST /api/projects/{projectId}/allocate
```

#### Étape 3.2 : Observer les résultats
L'algorithme devrait :
1. Affecter les tâches sans dépendances en premier
2. Respecter les compétences requises
3. Équilibrer la charge entre les membres
4. Prioriser les tâches urgentes

#### Étape 3.3 : Vérifications

**Affectations attendues (approximatives) :**
- **Alice** (40h disponible) :
  - Database Schema Design (16h) - Compétence Database
  - User Authentication System (25h) - Compétence Java/Database
  - Total : 41h (~102% - légère surcharge acceptable)

- **Bob** (35h disponible) :
  - Frontend Architecture (24h) - Compétence Frontend
  - Product Catalog Module (30h, partie frontend)
  - Total : ~35h (~100%)

- **Carol** (40h disponible) :
  - Comprehensive Testing (45h) - Compétence Testing
  - Total : 45h (~112% - surcharge)

- **David** (30h disponible) :
  - Backend API Development (40h) - Compétence API
  - Total : 40h (~133% - surcharge)

- **Emma** (40h disponible) :
  - UI/UX Design (32h) - Compétence Design
  - Shopping Cart (partie design)
  - Total : ~40h (~100%)

#### Étape 3.4 : Validation des règles

- [ ] Toutes les tâches sans dépendances non satisfaites sont affectées
- [ ] Les compétences requises correspondent aux compétences des membres
- [ ] Aucun membre n'a plus de 150% de charge (limite critique)
- [ ] L'écart de charge entre membres est raisonnable (<40%)
- [ ] Les tâches urgentes sont priorisées

### Critères de succès
✅ Au moins 8/10 tâches affectées (80%)
✅ Charge moyenne par membre : 50-120%
✅ Respect des compétences requises (100%)
✅ Pas de violation de dépendances
✅ Rapport d'allocation généré avec détails

### Métriques attendues
```json
{
  "assignedTasks": 8-10,
  "failedTasks": 0-2,
  "averageWorkload": "85%",
  "workloadVariance": "<30%",
  "skillMatchRate": "100%"
}
```

---

## Scénario 4 : Détection de surcharge

### Objectif
Vérifier que le système détecte et alerte sur les surcharges de travail.

### Procédure de test

#### Étape 4.1 : Affectation manuelle excessive
```bash
# Affecter 3-4 tâches importantes à un seul membre (Carol)
PUT /api/tasks/5
{
  "assignedMemberId": 3,  // Carol
  "status": "IN_PROGRESS"
}

PUT /api/tasks/6
{
  "assignedMemberId": 3,  // Carol
  "status": "TODO"
}

PUT /api/tasks/9
{
  "assignedMemberId": 3,  // Carol
  "status": "TODO"
}
# Total : 30 + 28 + 45 = 103h pour 40h disponible (257%)
```

#### Étape 4.2 : Vérifier les alertes
```bash
GET /api/alerts/
```

**Alerte attendue :**
```json
{
  "type": "OVERLOAD",
  "severity": "CRITICAL",
  "message": "Carol Williams is overloaded: 103.0h assigned / 40h available (257.5%)",
  "memberId": 3,
  "memberName": "Carol Williams",
  "timestamp": "2025-12-22T10:30:00"
}
```

#### Étape 4.3 : Vérifications
- [ ] Une alerte de type "OVERLOAD" est générée
- [ ] La sévérité est "CRITICAL" (>150%)
- [ ] Le message indique le membre, les heures et le pourcentage
- [ ] L'alerte apparaît dans le tableau de bord
- [ ] Un badge rouge s'affiche sur l'icône d'alertes
- [ ] Le membre apparaît en rouge dans la vue "Team"

#### Étape 4.4 : Test des seuils
- **Charge normale** (0-100%) : Pas d'alerte
- **Charge élevée** (100-150%) : Alerte WARNING (jaune)
- **Surcharge critique** (>150%) : Alerte CRITICAL (rouge)

### Critères de succès
✅ Alerte générée automatiquement
✅ Calcul correct du pourcentage de charge
✅ Sévérité appropriée selon le seuil
✅ Notification visible dans l'interface
✅ Données sauvegardées dans la base de données

---

## Scénario 5 : Modification en cours de projet

### Objectif
Tester l'ajout d'une tâche urgente en cours de projet et la réaffectation intelligente.

### Contexte
Le projet E-commerce est en cours, plusieurs tâches sont IN_PROGRESS ou COMPLETED.

### Procédure de test

#### Étape 5.1 : Simuler un projet en cours
```bash
# Marquer certaines tâches comme complétées
PUT /api/tasks/1
{
  "status": "COMPLETED"
}

PUT /api/tasks/3
{
  "status": "IN_PROGRESS"
}

PUT /api/tasks/4
{
  "status": "IN_PROGRESS"
}
```

#### Étape 5.2 : Ajouter une tâche urgente
```bash
POST /api/projects/{projectId}/tasks
{
  "title": "Critical Security Vulnerability Fix",
  "description": "Fix SQL injection vulnerability in login",
  "estimatedHours": 12,
  "priority": "URGENT",
  "status": "TODO",
  "deadline": "2025-01-05"  // Dans 3 jours !
}

# Ajouter compétence requise
POST /api/tasks/{newTaskId}/skills
{
  "skillId": 1,  // Java
  "requiredLevel": 5
}
```

#### Étape 5.3 : Réaffectation intelligente
```bash
POST /api/projects/{projectId}/reallocate
```

**Comportement attendu :**
1. Le système identifie la tâche urgente
2. Cherche le membre le plus qualifié (Alice - Java niveau 5)
3. Évalue la charge actuelle d'Alice
4. Si Alice est surchargée, suggère de reporter une tâche moins prioritaire
5. Affecte la tâche urgente à Alice
6. Génère une alerte de changement de planning

#### Étape 5.4 : Vérifications
- [ ] La tâche urgente est créée avec succès
- [ ] Elle apparaît en haut de la liste (tri par priorité)
- [ ] Le système l'affecte à un membre qualifié
- [ ] Une alerte "PRIORITY_CHANGE" est générée
- [ ] La timeline est mise à jour
- [ ] Les autres tâches sont réorganisées si nécessaire

### Critères de succès
✅ Tâche urgente ajoutée et visible
✅ Affectation automatique à un membre qualifié
✅ Respect du deadline dans la planification
✅ Alertes générées pour les changements
✅ Cohérence maintenue avec les dépendances existantes

---

## Scénario 6 : Visualisation

### Objectif
Vérifier la qualité et l'exhaustivité des visualisations (timeline et charges).

### Procédure de test

#### Étape 6.1 : Afficher la timeline du projet
1. Naviguer vers la page "Timeline"
2. Sélectionner le projet "E-commerce Platform"
3. Observer la timeline

**Éléments à vérifier :**
- [ ] Toutes les tâches sont affichées avec leurs dates
- [ ] Les dépendances sont visuellement représentées (flèches)
- [ ] Les couleurs indiquent le statut (TODO, IN_PROGRESS, COMPLETED)
- [ ] Les membres assignés sont visibles sur chaque tâche
- [ ] La timeline est interactive (zoom, scroll)
- [ ] Les tâches critiques sont mises en évidence
- [ ] Le chemin critique est identifiable
- [ ] Les dates de début/fin sont cohérentes

#### Étape 6.2 : Visualiser les charges par membre
```bash
GET /api/statistics/workload
```

**Affichage attendu :**
```
┌─────────────────┬──────────┬───────────┬────────────┐
│ Membre          │ Dispo    │ Charge    │ %          │
├─────────────────┼──────────┼───────────┼────────────┤
│ Alice Johnson   │ 40h      │ 41h       │ 102% 🟡   │
│ Bob Smith       │ 35h      │ 35h       │ 100% 🟢   │
│ Carol Williams  │ 40h      │ 103h      │ 257% 🔴   │
│ David Brown     │ 30h      │ 40h       │ 133% 🟡   │
│ Emma Davis      │ 40h      │ 40h       │ 100% 🟢   │
└─────────────────┴──────────┴───────────┴────────────┘
```

#### Étape 6.3 : Graphiques et diagrammes
- [ ] **Graphique en barres** : Charge par membre
- [ ] **Graphique en camembert** : Répartition des tâches par statut
- [ ] **Graphique de Gantt** : Planning temporel
- [ ] **Graphique de burndown** : Progression du projet

#### Étape 6.4 : Vérifications d'exhaustivité
- [ ] Toutes les données sont visibles (pas de tâches manquantes)
- [ ] Les couleurs sont cohérentes et significatives
- [ ] Les légendes sont claires et complètes
- [ ] Les tooltips affichent des détails supplémentaires
- [ ] L'interface est responsive (adapté aux différentes tailles d'écran)

### Critères de succès
✅ Timeline affichée avec toutes les tâches
✅ Dépendances visuellement claires
✅ Charges par membre correctement calculées et affichées
✅ Graphiques lisibles et informatifs
✅ Interface intuitive et professionnelle
✅ Données en temps réel (actualisées)

---

## Scénario 7 : Statistiques

### Objectif
Générer et valider l'exactitude des rapports statistiques du projet.

### Procédure de test

#### Étape 7.1 : Générer les statistiques du projet
```bash
GET /api/statistics/project/{projectId}
```

**Rapport attendu :**
```json
{
  "totalTasks": 10,
  "completedTasks": 2,
  "inProgressTasks": 3,
  "todoTasks": 5,
  "blockedTasks": 0,
  "completionPercentage": 20.0,
  
  "totalEstimatedHours": 295,
  "completedHours": 40,
  "remainingHours": 255,
  
  "priorityDistribution": {
    "URGENT": 2,
    "HIGH": 4,
    "MEDIUM": 4,
    "LOW": 0
  },
  
  "assignedTasks": 8,
  "unassignedTasks": 2
}
```

#### Étape 7.2 : Statistiques de charge de l'équipe
```bash
GET /api/statistics/workload
```

**Rapport attendu :**
```json
{
  "totalMembers": 5,
  "totalAvailability": 185,
  "totalWorkload": 259,
  "averageWorkloadPercentage": 140,
  "overloadedMembers": 2,
  
  "memberDetails": [
    {
      "name": "Alice Johnson",
      "availability": 40,
      "workload": 41,
      "percentage": 102.5,
      "status": "OPTIMAL"
    },
    {
      "name": "Carol Williams",
      "availability": 40,
      "workload": 103,
      "percentage": 257.5,
      "status": "OVERLOADED"
    }
    // ... autres membres
  ]
}
```

#### Étape 7.3 : Validation des calculs

**Test 1 : Pourcentage de complétion**
```
Formule : (completedTasks / totalTasks) × 100
Calcul : (2 / 10) × 100 = 20%
✅ Vérifié
```

**Test 2 : Heures restantes**
```
Formule : totalEstimatedHours - completedHours
Calcul : 295 - 40 = 255h
✅ Vérifié
```

**Test 3 : Charge moyenne**
```
Formule : (totalWorkload / totalAvailability) × 100
Calcul : (259 / 185) × 100 = 140%
✅ Vérifié
```

**Test 4 : Membres surchargés**
```
Critère : workloadPercentage > 150%
Membres : Carol (257%), David (133%)
Compte : 1 membre > 150%
✅ Vérifié
```

#### Étape 7.4 : Rapport d'avancement détaillé
```bash
GET /api/statistics/project/{projectId}/progress
```

**Contenu du rapport :**
- Résumé exécutif
- Graphique de progression
- Risques identifiés
- Recommandations
- Prochaines étapes

#### Étape 7.5 : Vérifications
- [ ] Tous les calculs sont mathématiquement corrects
- [ ] Les pourcentages sont arrondis de manière cohérente
- [ ] Les graphiques correspondent aux données numériques
- [ ] Les tendances sont identifiées (en retard, en avance, à l'heure)
- [ ] Les alertes sont incluses dans le rapport
- [ ] Le rapport est exportable (PDF, Excel)

### Critères de succès
✅ Toutes les statistiques sont générées
✅ Calculs mathématiquement corrects (0% d'erreur)
✅ Données cohérentes entre les différentes vues
✅ Rapport complet et professionnel
✅ Recommandations pertinentes générées
✅ Export fonctionnel

---

## Résumé des Tests

### Checklist globale
- [ ] Scénario 1 : Gestion de l'équipe (5 membres, compétences, disponibilités)
- [ ] Scénario 2 : Création de projet (1 projet, 10 tâches, dépendances)
- [ ] Scénario 3 : Répartition automatique (allocation intelligente)
- [ ] Scénario 4 : Détection de surcharge (alertes)
- [ ] Scénario 5 : Modification en cours (tâche urgente)
- [ ] Scénario 6 : Visualisation (timeline, graphiques)
- [ ] Scénario 7 : Statistiques (rapports, calculs)

### Métriques de réussite globales
```
┌────────────────────────┬──────────┬─────────┐
│ Critère                │ Attendu  │ Obtenu  │
├────────────────────────┼──────────┼─────────┤
│ Membres créés          │ 5        │ __      │
│ Tâches créées          │ 10       │ __      │
│ Tâches affectées       │ 8-10     │ __      │
│ Alertes générées       │ 1-3      │ __      │
│ Précision calculs      │ 100%     │ __      │
│ Timeline fonctionnelle │ Oui      │ __      │
│ Rapports générés       │ Oui      │ __      │
└────────────────────────┴──────────┴─────────┘
```

### Notes d'exécution
- **Date de test** : _______________
- **Testeur** : _______________
- **Version** : _______________
- **Environnement** : Development / Staging / Production

### Bugs et problèmes identifiés
```
ID | Sévérité | Description | Statut
---|----------|-------------|--------
1  |          |             |
2  |          |             |
3  |          |             |
```

---

## Instructions d'exécution

### Prérequis
1. Base de données MySQL en cours d'exécution
2. Schéma créé avec `database/schema.sql`
3. Serveur démarré avec `java -jar target/project-management.jar`
4. Interface web accessible sur http://localhost:8080

### Ordre d'exécution recommandé
1. Exécuter `database/comprehensive_test_data.sql` (données de test)
2. Démarrer le serveur
3. Ouvrir l'interface web
4. Suivre les scénarios dans l'ordre (1 → 7)
5. Noter les résultats dans les checkboxes
6. Générer le rapport final

### Scripts d'automatisation
Voir `test/TestScenariosRunner.java` pour l'exécution automatisée.

---

## Contact et support
Pour toute question sur ces scénarios de test, contactez l'équipe de développement.

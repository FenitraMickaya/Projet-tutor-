
Prérequis :
Android Studio (à installer si ce n'est pas déjà fait)
Instructions
Ouvrez Android Studio.
Sélectionnez Open (Ouvrir) et choisissez le dossier contenant ce projet.
Laissez Android Studio corriger automatiquement les éventuelles incompatibilités lors de l'importation du projet.
Créez un fichier nommé .env à la racine du projet et ajoutez-y la variable GEMINI_API_KEY avec votre clé API Gemini (consultez le fichier .env.example pour voir un exemple).

Supprimez la ligne suivante du fichier app/build.gradle.kts :
signingConfig = signingConfigs.getByName("debugConfig")

Exécutez l'application sur un émulateur Android ou sur un appareil physique connecté.

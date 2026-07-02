import os
import re

files = [
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/WelcomeSelectionScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/LoginScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/RegisterScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/wizard/SetupWizardScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/wizard/ChooseAvatarScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/wizard/ChooseFirstPetScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/wizard/WelcomeScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/NetflixProfilesScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/jugar/LearningAdventureHub.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/home/DictionaryScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/home/StoreScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/home/PetsScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/home/AchievementsScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/parent/ParentInsightCenter.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/parent/ChildDetailScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/onboarding/SplashScreen.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/jugar/WordScannerChallenge.kt",
    "sharedUI/src/commonMain/kotlin/org/alphakids/app/jugar/OCRResultScreen.kt"
]

import_statement = "import org.alphakids.app.theme.circadianBackground\n"

for file_path in files:
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        continue
        
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        
    if "circadianBackground" in content:
        print(f"Already applied in {file_path}")
        continue

    # 1. Add import
    # Find the last import
    import_match = list(re.finditer(r'^import .+$', content, re.MULTILINE))
    if import_match:
        last_import = import_match[-1]
        insert_pos = last_import.end() + 1
        content = content[:insert_pos] + import_statement + content[insert_pos:]
    else:
        # Fallback to after package
        pkg_match = re.search(r'^package .+$', content, re.MULTILINE)
        if pkg_match:
            insert_pos = pkg_match.end() + 1
            content = content[:insert_pos] + "\n" + import_statement + content[insert_pos:]

    # 2. Add Modifier.circadianBackground() to the root layout (first Box, Column, Scaffold, etc after @Composable)
    # We find the first Modifier in the @Composable fun
    
    # Simple regex to find the first modifier definition
    # e.g., modifier = Modifier\n .fillMaxSize()
    # We replace modifier = Modifier with modifier = Modifier.circadianBackground(alpha = 0.3f)
    
    modifier_match = re.search(r'modifier\s*=\s*Modifier(\s*\n?\s*)', content)
    if modifier_match:
        content = content[:modifier_match.end()] + ".circadianBackground(alpha = 0.3f)\n            " + content[modifier_match.end():]
        # Make the background color slightly transparent if it's solid, so we can see the circadian background
        content = re.sub(r'\.background\(MaterialTheme\.colorScheme\.background\)', r'.background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))', content)
        content = re.sub(r'\.background\(AlphaGradients\.vertical\(AlphaGradients\.Adventure\)\)', r'.background(AlphaGradients.vertical(AlphaGradients.Adventure).copy(alpha = 0.8f))', content)
        
        # If it's a Scaffold, it might have a default background. Set containerColor = Color.Transparent
        # Wait, not all have scaffolds.
        if "Scaffold(" in content:
            # check if Color is imported
            if "import androidx.compose.ui.graphics.Color" not in content:
                content = content.replace(import_statement, import_statement + "import androidx.compose.ui.graphics.Color\n")
            
            content = re.sub(r'(Scaffold\(\s*\n)', r'\1        containerColor = Color.Transparent,\n', content)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Applied to {file_path}")
    else:
        print(f"No modifier found in {file_path}")

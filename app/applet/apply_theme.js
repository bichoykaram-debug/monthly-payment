const fs = require('fs');

function applyTo(file) {
    let t = fs.readFileSync(file, 'utf8');
    
    // Backgrounds & Surfaces
    t = t.replace(/Color\(0xFF0F1923\)/g, "Color(0xFF1C1B1F)"); // Dark bg
    t = t.replace(/Color\(0xFF1A2433\)/g, "Color(0xFF2B2930)"); // Dark surface
    t = t.replace(/Color\(0xFF2D3748\)/g, "Color(0xFF4A4458)"); // Boundaries/borders
    
    // Neutralize colored cards to dark surface
    t = t.replace(/Color\(0xFFDCFCE7\)/g, "Color(0xFF2B2930)");
    t = t.replace(/Color\(0xFFDBEAFE\)/g, "Color(0xFF2B2930)");
    t = t.replace(/Color\(0xFFFEF3C7\)/g, "Color(0xFF2B2930)");
    t = t.replace(/Color\(0xFFFEE2E2\)/g, "Color(0xFF2B2930)");

    // Convert Green primary to Light Purple primary
    t = t.replace(/Color\(0xFF2D6A4F\)/g, "Color(0xFFD0BCFF)");
    // Convert Green subtle accent to Dark Purple accent
    t = t.replace(/Color\(0xFFD8F3DC\)/g, "Color(0xFF4A4458)");

    // Replace ANY Color.White text to Dark Purple (because now their bg is Light Purple)
    t = t.replace(/Color\.White/g, "Color(0xFF381E72)");

    // Restore text that needs to be white on dark surfaces
    t = t.replace(/Color\(0xFF381E72\)\s*\n\s*\)/g, "Color.White\n)"); 
    t = t.replace(/text = "مالي الشخصي",([^c]*)color = Color\(0xFF381E72\)/g, 'text = "مالي الشخصي",$1color = Color(0xFFE6E1E5)');
    t = t.replace(/containerColor = Color\(0xFF381E72\), contentColor = Color\.Black/g, 'containerColor = Color(0xFFD0BCFF), contentColor = Color(0xFF381E72)');
    
    // Fix color in Theme.kt mappings
    t = t.replace(/val LightPrimary = Color\(0xFF2D6A4F\)/g, "val LightPrimary = Color(0xFF6750A4)");
    t = t.replace(/val LightPrimaryHover = Color\(0xFF40916C\)/g, "val LightPrimaryHover = Color(0xFF4F378B)");
    t = t.replace(/val LightAccentSubtle = Color\(0xFFD8F3DC\)/g, "val LightAccentSubtle = Color(0xFFF6EDFF)");

    fs.writeFileSync(file, t, 'utf8');
}

applyTo('app/src/main/java/com/example/ui/Screens.kt');
applyTo('app/src/main/java/com/example/MainActivity.kt');
applyTo('app/src/main/java/com/example/ui/Charts.kt');
applyTo('app/src/main/java/com/example/ui/theme/Color.kt');

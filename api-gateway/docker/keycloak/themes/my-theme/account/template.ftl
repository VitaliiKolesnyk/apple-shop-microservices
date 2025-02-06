<#-- Base layout template that can be extended by other templates -->

<!DOCTYPE html>
<html lang="${locale}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>

<div class="kc-header">
</div>

<div class="kc-container">
    <div class="kc-content">
        <#-- Placeholder for specific page content -->
        <@layout.page />
    </div>
</div>

<div class="kc-footer">
    <p>${msg("kcFooterMessage")}</p>
</div>
</body>
</html>

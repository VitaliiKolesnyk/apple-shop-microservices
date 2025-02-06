<#import "template.ftl" as layout>

<#macro page>
    <!DOCTYPE html>
    <html lang="${locale}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${msg("accountTitle")}</title>
        <link rel="stylesheet" href="${url.resourcesPath}/css/styles.css">
    </head>
    <body>

    <div class="kc-header">
        <div class="kc-logo">
            <img src="${url.resourcesPath}/img/logo.png" alt="Custom Logo">
        </div>
        <div class="kc-user-details">
            <span>${user.firstName} ${user.lastName}</span> | <a href="${url.loginAction}?logout">${msg("signOut")}</a>
        </div>
    </div>

    <div class="kc-container">
        <div class="kc-menu">
            <ul>
                <li><a href="${url.accountUrl}">${msg("accountMenuAccount")}</a></li>
                <li><a href="${url.passwordUrl}">${msg("accountMenuPassword")}</a></li>
                <li><a href="${url.identityUrl}">${msg("accountMenuIdentity")}</a></li>
                <li><a href="${url.applicationsUrl}">${msg("accountMenuApplications")}</a></li>
                <li><a href="${url.sessionsUrl}">${msg("accountMenuSessions")}</a></li>
            </ul>
        </div>

        <div class="kc-content">
            <h1>${msg("accountTitle")}</h1>

            <#-- Display any user notifications -->
            <#if state.hasFeedback>
                <div class="kc-feedback ${state.feedbackClass}">
                    ${state.feedbackMessage}
                </div>
            </#if>

            <#-- Display the user details -->
            <div class="kc-profile-section">
                <h2>${msg("accountDetailsTitle")}</h2>
                <p><strong>${msg("username")}:</strong> ${user.username}</p>
                <p><strong>${msg("email")}:</strong> ${user.email}</p>
                <p><strong>${msg("firstName")}:</strong> ${user.firstName}</p>
                <p><strong>${msg("lastName")}:</strong> ${user.lastName}</p>
            </div>

            <#-- Password update section -->
            <div class="kc-profile-section">
                <h2>${msg("passwordUpdateTitle")}</h2>
                <form action="${url.passwordUrl}" method="POST">
                    <label for="currentPassword">${msg("currentPassword")}:</label>
                    <input type="password" id="currentPassword" name="currentPassword">
                    <label for="newPassword">${msg("newPassword")}:</label>
                    <input type="password" id="newPassword" name="newPassword">
                    <button type="submit">${msg("submitPassword")}</button>
                </form>
            </div>

            <#-- Identity provider links -->
            <div class="kc-profile-section">
                <h2>${msg("identityProviderTitle")}</h2>
                <ul>
                    <#list identityProviders as identityProvider>
                        <li><a href="${identityProvider.url}">${identityProvider.displayName}</a></li>
                    </#list>
                </ul>
            </div>
        </div>
    </div>

    </body>
    </html>
</#macro>

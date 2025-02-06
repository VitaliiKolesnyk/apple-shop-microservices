<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
    <!DOCTYPE html>
    <html class="${properties.kcHtmlClass!}">

    <head>
        <meta charset="utf-8">
        <meta name="robots" content="noindex, nofollow">
        <title>${msg("loginTitle", (realm.displayName!''))}</title>
        <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />

        <#if properties.stylesCommon?has_content>
            <#list properties.stylesCommon?split(' ') as style>
                <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
            </#list>
        </#if>
        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
            </#list>
        </#if>

        <!-- Custom styles for login page -->
        <style>
            body {
                color: black; /* Change text color to black */
            }
            .btn {
                background-color: #4CAF50; /* Change button background color */
                color: white; /* Change button text color */
            }
            .btn:hover {
                background-color: #45a049; /* Change button hover color */
            }
        </style>

        <#if properties.scripts?has_content>
            <#list properties.scripts?split(' ') as script>
                <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
            </#list>
        </#if>
        <#if scripts??>
            <#list scripts as script>
                <script src="${script}" type="text/javascript"></script>
            </#list>
        </#if>
    </head>

    <body class="${properties.kcBodyClass!}">
    <div class="${properties.kcLoginClass!}">
        <div id="kc-header" class="${properties.kcHeaderClass!}">
            <h1>${msg("loginAccountTitle")}</h1>
        </div>
        <div class="${properties.kcFormCardClass!}">
            <header class="${properties.kcFormHeaderClass!}">
                <#if realm.internationalizationEnabled && locale.supported?size gt 1>
                    <div class="${properties.kcLocaleMainClass!}" id="kc-locale">
                        <a href="#" id="kc-current-locale-link">${locale.current}</a>
                        <ul class="${properties.kcLocaleListClass!}">
                            <#list locale.supported as l>
                                <li>
                                    <a href="${l.url}">${l.label}</a>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </#if>
                <#if displayRequiredFields>
                    <div class="${properties.kcContentWrapperClass!}">
                        ${msg("requiredFields")}
                    </div>
                </#if>
            </header>
            <div id="kc-content">
                <div id="kc-content-wrapper">
                    <#if displayMessage && message?has_content>
                        <div class="alert-${message.type} ${properties.kcAlertClass!}">
                            <span>${kcSanitize(message.summary)?no_esc}</span>
                        </div>
                    </#if>

                    <#nested "form">

                    <#if auth?has_content && auth.showTryAnotherWayLink()>
                        <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                            <input type="hidden" name="tryAnotherWay" value="on"/>
                            <a href="#" id="try-another-way" onclick="document.forms['kc-select-try-another-way-form'].submit(); return false;">${msg("doTryAnotherWay")}</a>
                        </form>
                    </#if>

                    <#nested "socialProviders">

                    <#if displayInfo>
                        <div id="kc-info" class="${properties.kcSignUpClass!}">
                            <#nested "info">
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </div>
    </body>
    </html>
</#macro>
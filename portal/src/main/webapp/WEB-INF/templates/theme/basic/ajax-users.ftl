<?xml version="1.0"?>
<reply>
<#if users??>
    <users>
        <#list users as user>
            <user
                    email="${user.email}"
                    role="
                        <#switch user.role>
                            <#case "ROLE_GOD">
                                Super User
                            <#break>
                            <#case "ROLE_RESEARCH_USER">
                                 Museometrie Gebruiker
                            <#break>
                            <#case "ROLE_ADMINISTRATOR">
                                  Administrator
                            <#break>
                            <#case "ROLE_USER">
                                  Gewone Gebruiker
                            <#break>
                        </#switch>
                    "
                    firstName="<#if user.firstName??>${user.firstName}</#if>"
                    lastNamme="<#if user.lastName??>${user.lastName}</#if>"
                    userName="<#if user.userName??>${user.userName}</#if>"
                    registrationDate="<#if user.registrationDate??>${user.registrationDate?string("yyyy-MM-dd HH:mm")}</#if>"
                    lastLoginDate="<#if user.lastLogin??>${user.lastLogin?string("yyyy-MM-dd HH:mm")}</#if>"
                    />
        </#list>
    </users>
</#if>
</reply>
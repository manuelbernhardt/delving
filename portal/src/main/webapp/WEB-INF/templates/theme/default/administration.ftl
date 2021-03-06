<#import "spring.ftl" as spring />
<#assign thisPage = "administration.html"/>
<#assign pageId = "ad"/>
<#include "inc_header.ftl"/>
<style type="text/css">.ui-icon{float:left;margin:0 .25em 0 0;}</style>

<div id="header">

    <div id="identity" class="grid_3">
        <h1>Delving</h1>
        <a href="/${portalName}/index.html" title="ABM"><img src="/${portalName}/${portalTheme}/images/abm-logo.jpg" alt="ABM"/></a>
    </div>

    <div class="grid_9">

        <div id="top-bar">
            <div class="inner">
                <@userbar/>
            </div>
        </div>

    </div>

</div>

<div id="main">

    <div id="administration-page" class="grid_12">



            <h2>Gebruikers Administratie</h2>
            <ol>
                <li>Voer een naam of email in het zoek veld om een gebruiker te vinden</li>
                <li>Pas de rol van de gebruiker aan</li>
            </ol>






        <#if !userList??>

            <form method="post" action="administration.html" id="search-form">
                <table >
                    <tr>
                        <td width="150":><h4>Vind een gebruiker</h4></td>
                        <td><input type="text" name="searchPattern"/></td>
                        <td><input type="submit" value="vind"/> </td>
                    </tr>
                </table>
                <#if targetUser??>
                    <p>
                        ${targetUser.email} heeft nu de rol van
                        <#if targetUser.role = 'ROLE_RESEARCH_USER'>
                            Museometrie Gebruiker
                        </#if>
                        <#if targetUser.role = 'ROLE_ADMINISTRATOR'>
                            Administrator
                        </#if>
                        <#if targetUser.role = 'ROLE_USER'>
                            Gewoon Gebruiker
                        </#if>
                    </p>
                </#if>
            </form>
        </#if>

        <#if userList??>
            <table>
                <tr>
                    <th>Email</th>
                    <th>Huidige Rol</th>
                    <th>Nieuwe Rol</th>
                    <th>Zetten</th>
                </tr>
                <#list userList as userEdit>
                    <form method="post" action="administration.html" id="set-form">
                        <input type="hidden" name="userEmail" value="${userEdit.email}"/>
                        <tr>
                            <td width="150">${userEdit.email}</td>
                            <td width="150">
                                <#switch userEdit.role>
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
                            </td>
                            <td width="200">
                                <select name="newRole">
                                    <option>Kies een rol</option>
                                    <#if user.role=="ROLE_GOD"><option value="ROLE_ADMINISTRATOR">Administrator</option></#if>                                    
                                    <option value="ROLE_RESEARCH_USER">Museometrie Gebruiker</option>
                                    <option value="ROLE_USER">Gewone Gebruiker</option>
                                </select>
                            </td>
                            <td><input type="submit" value="zet nu"/> </td>
                        </tr>
                    </form>
                </#list>
            </table>
        </#if>


    </div>

</div>

<#include "inc_footer.ftl"/>

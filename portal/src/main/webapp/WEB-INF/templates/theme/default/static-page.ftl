<#compress>

    <#if pagePathList??>

        <#if RequestParameters.javascript??>

            var tinyMCELinkList = new Array(
            <#list pagePathList as pagePath>
                 ["${pagePath}","${pagePath}"]<#if pagePath_has_next>,</#if>
            </#list>
            );

        <#else>

            <#assign thisPage = "static-page.dml"/>
            <#assign pageId = "static"/>
            <#include "inc_header.ftl"/>

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

            <div id="main" class="static-page">

            <div class="grid_6">
                <h2><@spring.message '_cms.existing.pages' /></h2>
                <table summary="List of existing pages" class="user-options">
                    <#list pagePathList as pagePath>
                        <tr>
                            <td width="300">
                                <a href="${pagePath}?edit=true">
                                <span class="ui-icon ui-icon-document"></span>
                                ${pagePath}</a></td>
                            <td width="85"><a href="${pagePath}?edit=true">
                                <span class="ui-icon ui-icon-pencil"></span>
                                <@spring.message '_cms.edit' /></a>
                            </td>
                            <td width="100">
                                 <a class="delete" id="delete_${pagePath_index}" href="${pagePath}">
                                    <span class="ui-icon ui-icon-trash"></span>
                                      <@spring.message '_cms.delete' />
                                 </a>

                            </td>
                        </tr>
                    </#list>
                </table>
            </div>

            <div class="grid_6">

                <h2><@spring.message '_cms.page.create' /></h2>
                <ol>
                    <li><@spring.message '_cms.page.create.step.1' /></li>
                    <li><@spring.message '_cms.page.create.step.2' /></li>
                </ol>


                <form method="get" action="" id="form-makePage" onsubmit="createPage(this.pagePath.value);return false;">
                    /${portalName}/&#160;<input type="text" value="" name="pagePath" id="pagePath"/>
                    <input type="submit" value="<@spring.message '_cms.create' />" id="makePage"/>
                </form>

           </div>


            <div class="clear"></div>

            <script type="text/javascript">

                $("a.delete").click(function(){
                    var target = $(this).attr("id");
                    var targetURL = $(this).attr("href");
                    var confirmation = confirm("Pagina: "+targetURL +" verwijderen ?")
                    if(confirmation){
                        $.ajax({
                            url: targetURL,
                            type: "POST",
                            data: "content= ",
                            success: function(data) {
                                window.location.reload();
                            },
                            error: function(data) {
                                alert("page could not be deleted");
                            }
                        });
                        return false;
                    } else {
                        return false;
                    }
                });

                function createPage(page){
                    var targetURL = $("#pagePath").attr("value");
                    window.location.href=targetURL+"?edit=true";
                }

            </script>

            <#include "inc_footer.ftl"/>

        </#if>

    <#elseif embedded>

        <#assign locale = locale/>
        ${page.getContent(locale)}

    <#else>

        <#assign locale = locale/>
        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = page.path/>
        <#include "inc_header.ftl"/>
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
        <div class="grid_12">
            <div id="content" class="content-preview">
            ${page.getContent(locale)}
            </div>
            <#if edit??>
                <#if edit>

                    <div id="pageForm">
                        <form action="${page.path}" method="POST" id="form-edit">
                            <table>
                                <tr>
                                    <td>
                                      <a href="javascript:toggleEditor('editor');" class=""><@spring.message '_cms.html.editor.show.hide' /></a>  
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <textarea name="content" id="editor" style="width:100%;height:350px;"${page.getContent(locale)}</textarea>
                                        <input type="submit" name="submit" value="<@spring.message '_cms.save' />"/> <a href="${page.path}?edit=false" class="button"><@spring.message '_cms.cancel' /></a>
                                    </td>
                                </tr>
                            </table>
                        </form>
                    </div>
                <#else>
                    <#if page.id??>
                        <p><a href="${page.path}?edit=true&version=${page.id}" class="button"><@spring.message '_cms.page.edit' /></a></p>
                    <#else>
                        <p><a href="${page.path}?edit=true" class="button"><@spring.message '_cms.page.edit' /></a></p>
                    </#if>
                    <br/><br/>
                    <#if versionList?? && page.id??>
                        <h3><@spring.message '_cms.version.management' /></h3>
                        <p>
                            <@spring.message '_cms.version.approve.explain' />
                        </p>
                        <ul>
                            <#list versionList as version>
                                <#if version.id == page.id>
                                    <li><strong>${version.date?string("yyyy-MM-dd HH:mm:ss")}</strong> - <a href="${version.path}?version=${version.id}&edit=false&approve=true"><@spring.message '_cms.version.approve' /></li>
                                <#else>
                                    <li><a href="${version.path}?version=${version.id}&edit=false">${version.date?string("yyyy-MM-dd HH:mm:ss")}</a></li>
                                </#if>
                            </#list>
                        </ul>
                    </#if>
                </#if>
            </#if>
        </div>
    
        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/tiny_mce/tiny_mce.js"></script>
        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/static-page.js"></script>
        <#include "inc_footer.ftl"/>
     </div>   
    </#if>



</#compress>

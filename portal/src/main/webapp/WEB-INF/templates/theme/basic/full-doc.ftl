<#compress>
<#include "includeMarcos.ftl">
<@addCustomAssigns/>
<@addHeader "${portalDisplayName}", "",["results.js","fancybox/jquery.fancybox-1.3.1.pack.js"],["fancybox/jquery.fancybox-1.3.1.css"]/>
<script type="text/javascript">
    var msgItemSaveSuccess = "<@spring.message '_mine.itemsaved'/>";
    var msgItemSaveFail = "<@spring.message '_mine.itemsavefailed'/>";
</script>
<section id="sidebar" class="grid_3" role="complementary">

    <dl class="menu">
        <dt><@spring.message '_portal.ui.message.relatedcontent' /></dt>
            <#assign max=5/><!-- max shown in list -->
            <#list result.relatedItems as doc>
                <#if doc_index &gt; (max-1)><#break/></#if>
             <dd>
                 <#if useCache="true">
                         <img src="${cacheUrl}uri=${doc.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${doc.type}&amp;view=${view}" onerror="showDefaultSmall(this,'${doc.type}')" alt="Click here to view related item" width="25"/>
                     <#else>
                         <img src="${doc.thumbnail}" alt="Click here to view related item" width="25" onerror="showDefaultSmall(this,'${doc.type}')"/>
                 </#if>
                  <#if queryStringForPaging??>
                    <a href='${doc.fullDocUrl()}?query=europeana_uri:"${doc.id?url('utf-8')}"&amp;start=${doc.index()?c}&amp;startPage=1&amp;pageId=brd'><@stringLimiter "${doc.title}" "40"/></a>
                  <#else>
                       <a href="${doc.fullDocUrl()}"><@stringLimiter "${doc.title}" "40"/></a>
                </#if>
             </dd>

            </#list>
            <#if result.relatedItems?size &gt; max-1>
                <dd>
                     <a href='/${portalName}/brief-doc.html?query=europeana_uri:"${uri}"&amp;view=${view}'><@spring.message '_action.see.all.related.items' /></a>
                </dd>
            </#if>
    </dl>



                                              
        <dl class="menu">
            <dt><@spring.message '_portal.ui.message.actions' /></dt>
            <#if result.fullDoc.europeanaType == "IMAGE">
                <#if result.fullDoc.europeanaIsShownBy[0]?? && imageAnnotationToolBaseUrl?? && imageAnnotationToolBaseUrl!="">
                <dd>
                    <a href="${imageAnnotationToolBaseUrl}?user=${user.email}&objectURL=${result.fullDoc.europeanaIsShownBy[0]}&id=${result.fullDoc.id}" target="_blank"><@spring.message '_action.add.annotation' /></a>
                </dd>
                </#if>
            </#if>
            <#if addThisTrackingCode??>
                <dd>
                    <@addThis "${addThisTrackingCode}"/>
                </dd>
            </#if>
            <#if user??>


                <dd>
                    <a href="#" onclick="saveItem('SavedItem','${postTitle?js_string}','${postAuthor?js_string}','${result.fullDoc.id?js_string}','${result.fullDoc.getFieldValue("europeana_object").getFirst()?js_string}','${result.fullDoc.europeanaType}');"><@spring.message '_action.save.to.mine' /></a>
                </dd>

                <#if result.fullDoc.europeanaType == "IMAGE">
                	<#if result.fullDoc.europeanaIsShownBy[0]?? && imageAnnotationToolBaseUrl?? && imageAnnotationToolBaseUrl!="">
	                    <dd>
		                    <a href="${imageAnnotationToolBaseUrl}?user=${user.email}&objectURL=${result.fullDoc.europeanaIsShownBy[0]}&id=${result.fullDoc.id}" target="_blank"><@spring.message '_action.add.annotation' /></a>
		                </dd>
	                </#if>
                </#if>

            <#else>
                <dd>
                    <a href="/${portalName}/login.html" class="disabled" onclick="highLight('#mustlogin'); writeMessage('div#msg-save-item','<@spring.message '_mine.user.notification.login.required'/>'); return false;"><@spring.message '_action.add.tag' /></a>
                </dd>

                <dd>
                    <a href="/${portalName}/login.html" class="disabled" onclick="highLight('a#login'); writeMessage('div#msg-save-item','<@spring.message '_mine.user.notification.login.required'/>'); return false;"><@spring.message '_action.save.to.mine' /></a>
                </dd>


            </#if>
            <div id="msg-save-item" class="msg-hide"></div>
        </dl>



            <#--<p>-->
            <#--<a href="inc_related_content.ftl#" onclick="saveItem('SavedItem','${postTitle?js_string}','${postAuthor?js_string}','${result.fullDoc.id?js_string}','${result.fullDoc.thumbnails[0]?js_string}','${result.fullDoc.europeanaType}');return false;"><@spring.message '_action.save.to.mine' /></a>-->

            <#--<div id="msg-save-item" class="msg-hide"></div>-->
            <#--</p>-->

            <#--<p>-->
            <#--<h6><@spring.message '_action.add.tag' /></h6>-->


            <#--<form action="inc_related_content.ftl#" method="post" onsubmit="addTag('SocialTag', document.getElementById('tag').value,'${result.fullDoc.id}','${result.fullDoc.thumbnails[0]?js_string}','${postTitle}','${result.fullDoc.europeanaType}'); return false;" id="form-addtag" name="form-addtag" accept-charset="UTF-8">-->
                <#--<input type="text" name="tag" id="tag" maxlength="50" class="text"/>-->
                <#--<input type="submit" class="button" value="Add"/>-->
            <#--</form>-->
            <#--<div id="msg-save-tag" class="hide"></div>-->
        <#--</p>-->
</section>


<section id="item" class="grid_9" role="main">

     <div id="nav_query_breadcrumbs">
            <@resultsFullQueryBreadcrumbs/>
    </div>

    <div class="clear"></div>

    <nav class="pagination" role="navigation">
        <div class="inner">
        <@resultFullPagination/>


        </div>
    </nav>

    <div class="clear"></div>


        <div id="itemImage" class="grid_4 alpha">
            <div class="inner">
            <@resultFullImage/>
            </div>
        </div>

        <div id="itemMetaData" class="grid_5 omega">
            <@resultFullList/>
        </div>



</section>

<@addFooter/>
</#compress>

<#macro addCustomAssigns>
    <#assign result = result/>
    <#assign uri = result.fullDoc.id/>
    <#if format??><#assign format = format/></#if>
    <#if pagination??>
        <#assign pagination = pagination/>
        <#assign queryStringForPaging = pagination.queryStringForPaging />
    </#if>
    <#assign dcTitle = result.fullDoc.getFieldValue("dc_title")/>
    <#if dcTitle.isNotEmpty()>
        <#if dcTitle.getFirst()?length &gt; 110>
            <#assign postTitle = dcTitle.getFirst()?substring(0, 110)?url('utf-8') + "..."/>
        <#else>
            <#assign postTitle = dcTitle.getFirst()?url('utf-8')/>
        </#if>
    </#if>
    <#assign dcCreator = result.fullDoc.getFieldValue("dc_creator")/>
    <#if !dcCreator.isNotEmpty()>
        <#assign postAuthor = "none"/>
    <#else>
        <#assign postAuthor = dcCreator.getFirst()/>
    </#if>
    <#-- Removed ?url('utf-8') from query assignment -->
    <#if RequestParameters.query??><#assign query = "${RequestParameters.query}"/></#if>

</#macro>

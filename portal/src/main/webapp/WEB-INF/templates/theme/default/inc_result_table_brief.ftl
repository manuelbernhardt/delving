<#compress>

<#if seq?size &gt; 0>
    <#if view = "table">
        <@show_result_table seq = seq/>
    <#elseif view = "flow">
        <@show_result_flow seq = seq/>
    <#else>
        <@show_result_list seq = seq/>
    </#if>

<#else>
    <div id="no-result"><@spring.message '_portal.ui.notification.noitemsfound' /></div>
</#if>


<#macro show_result_table seq>
<table id="multi" summary="gallery view all search results" border="0">
    <caption>Results</caption>
    <#list seq?chunk(4) as row>
    <#assign  blockwidth ="25%">
    <#-- if the result is less than 4 change the width of the table cell so that it does not stretch across the page -->
    <#if (row?size < 4)>
       <#assign  blockwidth ="250">
    </#if>
    <tr>
        <#list row as cell>
        <td valign="bottom" width="${blockwidth}" class="${cell.type}">
            <div class="brief-thumb-container">
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true">
                         <img
                                 class="thumb"
                                 id="thumb_${cell.index()?c}"
                                 align="middle"
                                 src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}" alt="<@spring.message '_action.alt.more.info' />"
                                 onload="checkSize(this.id,'brief',this.width);"
                                 onerror="showDefaultSmall(this,'${cell.type}')"
                                 height="110"
                          />
                    <#else>
                        <img
                                class="thumb"
                                id="thumb_${cell.index()?c}"
                                align="middle"
                                src="${cell.thumbnail}"
                                alt="Click for more information"
                                height="110"
                                onload="checkSize(this.id,'brief',this.width);"
                                onerror="showDefaultSmall(this,'${cell.type}')"
                         />
                    </#if>
                </a>
            </div>
            <div class="brief-content-container">
            <h6>
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd">
                    <@stringLimiter "${cell.title}" "40"/>
                </a>
            </h6>
            <ul>
                <#if cell.creator??>
                    <#if !(cell.creator = " " || cell.creator = "," || cell.creator = "Unknown,")>
                        <li><@stringLimiter "${cell.creator}" "120"/></li>
                    </#if>

                </#if>
                <#if cell.year != "">
                    <#if cell.year != "0000">
                        <li>${cell.year}</li>
                    </#if>

                </#if>
                <#if cell.provider != "">
                    <#assign pr = cell.provider />
                    <#if pr?length &gt; 80>
                        <#assign pr = cell.provider?substring(0, 80) + "..."/>
                    </#if>
                    <li title="${cell.provider}"><span class="provider">${pr}</span></li>
                </#if>
            </ul>
            </div>
        </td>
        </#list>
    </tr>
    </#list>
</table>
</#macro>

<#macro show_result_list seq>
<table cellspacing="1" cellpadding="0" width="100%" border="0" summary="search results" id="multi">
    <#list seq as cell>
    <tr>
        <td valign="top" width="50">
            <div class="brief-thumb-container-listview">
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true">
                        <img class="thumb"
                             id="thumb_${cell.index()}"
                             align="middle"
                             src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}"
                             alt="<@spring.message '_action.alt.more.info' />"
                             height="50"
                          />
                    <#else>
                        <img class="thumb"
                             id="thumb_${cell.index()?c}"
                             align="middle"
                             src="${cell.thumbnail}"
                             alt="Click for more information"
                             height="50"
                             onerror="showDefaultSmall(this,'${cell.type}')"
                         />
                    </#if>
                </a>
            </div>
        </td>
        <td class="${cell.type} ">
                <h6>
                    <a class="fg-gray" href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                        <@stringLimiter "${cell.title}" "100"/></a>
                </h6>
                <p>
                <#-- without labels -->
                <#--
                <#if !cell.creator[0]?matches(" ")>${cell.creator}<br/></#if>
                <#if !cell.year?matches(" ")><#if cell.year != "0000">${cell.year}<br/></#if></#if>
                <#if !cell.provider?matches(" ")>${cell.provider}</#if>
                --->
                <#-- with labels -->
                <#if !cell.creator[0]?matches(" ")><span><@spring.message '_search.field.creator' />: </span>${cell.creator}<br/></#if>
                <#if !cell.year?matches(" ")><#if cell.year != "0000"><span><@spring.message '_search.field.date' />: </span>${cell.year}<br/></#if></#if>
                <#if !cell.provider?matches(" ")><@spring.message '_search.field.provider' />: <span class="provider">${cell.provider}</span></#if>
                </p>
        </td>
    </tr>
    </#list>
</table>
</#macro>

<#macro show_result_flow seq>
<div id="imageflow">
    <noscript>
        <div class="attention"><@spring.message '_portal.ui.message.noscript' /></div>
    </noscript>
    <div id="loading">
        <b>Loading images</b><br/>
        <img src="/${portalName}/${portalTheme}/images/loading.gif" width="208" height="13" alt="loading"/>
    </div>
    <div id="images">
        <#list seq as briefDoc>
        <img class="flow" src="${cacheUrl}type=${briefDoc.type}&amp;uri=${briefDoc.thumbnail}" longdesc="${briefDoc.title}" alt="${briefDoc.id}"/>
        </#list>
    </div>
    <div id="captions"></div>
    <div id="scrollbar">
        <div id="slider" style="left: 0px;"></div>
    </div>
    <div id="numinfo">Number of objects found: <strong><span id="numFound">${pagination.numFound}&nbsp;</span></strong>
        <br/>
        results ${pagination.start} - ${pagination.lastViewableRecord} of ${pagination.numFound} <br/>
    </div>
    <div id="objectPageing">
    </div>
</div>
</#macro>

</#compress>

<#--This page will contain all the code for the search Comparator-->
<#assign defaultView = defaultView >
<#assign standardView = standardView >
<#assign custom1View = custom1View >
<#assign custom2View = custom2View >
<#assign thisPage = "brief-doc.html"/>
<#assign next = defaultView.facetQueryLinks>
<#assign breadcrumbs = defaultView.pagination.breadcrumbs/>
<#assign pagination = defaultView.pagination/>
<#assign queryStringForPresentation = defaultView.pagination.presentationQuery.queryForPresentation />
<#assign tab = ""/>

<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>

<section role="main" class="grid_12">

    <div id="breadcrumbs">
        <div class="inner">

        <ul>
            <#if !defaultView.matchDoc??>
                <li class="first"><@spring.message '_portal.ui.navigation.matchesfor' />:</li>
                <#list breadcrumbs as crumb>
                    <#if !crumb.last>
                        <li><a href="${thisPage}?${crumb.href}">${crumb.display?html}</a>&#160;>&#160;</li>
                    <#else>
                        <li><strong>${crumb.display?html}</strong></li>
                    </#if>
                </#list>
            <#else>
                <li class="first">
                <@spring.message '_portal.ui.viewingrelateditems' />
                <#assign match = defaultView.matchDoc/>
                <a href="${match.fullDocUrl}">
                    <#if useCache="true"><img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                    <#else><img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                    </#if>
                </a>
            </li>
            </#if>
        </ul>
        </div>
    </div>

    <div class="clear"></div>

    <div class="inner">

        <div id="objTypes">
            <div>
            <@spring.message '_portal.ui.navigation.results' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message '_portal.ui.navigation.of' /> ${pagination.getNumFound()?c}
            </div>
            <#--<@typeTabs_plain/>-->
            <#--<@viewSelect/>-->
        </div>

        <div class="clearfix"></div>

        <div class="pagination">
            <@resultnav_styled/>
        </div>

        <div class="clearfix"></div>
            <div class="grid_3 alpha">
                <h4>default simple view</h4>
                <p>numFound: ${defaultView.pagination.numFound}</p>
                <p>parsed query: ${defaultView.pagination.presentationQuery.parsedQuery}</p>
                <@show_result_list resultBeanView = defaultView/>
            </div>

            <div class="grid_3">
                <h4>advanced view</h4>
                <p>numFound: ${standardView.pagination.numFound}</p>
                <p>parsed query: ${standardView.pagination.presentationQuery.parsedQuery}</p>
                <@show_result_list resultBeanView = standardView/>
            </div>

            <div class="grid_3">
                <h4>custom view 1</h4>
                <p>numFound: ${custom1View.pagination.numFound}</p>
                <p>parsed query: ${custom1View.pagination.presentationQuery.parsedQuery}</p>
                <@show_result_list resultBeanView = custom1View/>
            </div>

            <div class="grid_3 omega">
                <h4>custom view 2</h4>
                <p>numFound: ${custom2View.pagination.numFound}</p>
                <p>parsed query: ${custom2View.pagination.presentationQuery.parsedQuery}</p>
                <@show_result_list resultBeanView  = custom2View/>
            </div>

            <div class="clearfix"></div>

        <div class="pagination">
            <@resultnav_styled/>
        </div>

    </div>

</section>

<@addFooter/>

<#macro MultiSearchForm className>

    <#assign showAdv="none"/>
    <#assign showSim="block"/>
    <#if pageId??>
        <#if pageId=="adv">
            <#assign showAdv="block"/>
            <#assign showSim="none"/>
        </#if>
    </#if>

    <div id="search_simple" class="${className}" style="display:${showSim};">
        <#if result?? >
            <#if result.badRequest?? >
                <span style="font-style: italic;">Wrong query. ${result.errorMessage}</span>
            </#if>
        </#if>
        <form method="get" action="/${portalName}/comparator.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
            <input type="hidden" name="start" value="1" />
            <input type="hidden" name="view" value="${view}" />
            <input class="txt-input" name="query" id="query" type="text" title="Europeana Search" maxlength="75" />
            <button id="submit_search" type="submit" class="btn-search"><@spring.message '_action.search' /></button>
            <br/>
            <a href="advancedsearch.html" id="href-advanced" title="<@spring.message '_action.advanced.search' />"><@spring.message '_action.advanced.search' /></a>
        </form>
    </div>

    <div id="search_advanced" class="${className}" style="display:${showAdv};" title="<@spring.message '_action.advanced.search' />">
       <form method="get" action="/${portalName}/comparator.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <table>
            <tr>
                <td>&#160;</td>
                <td><select name="facet1" id="facet1"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="query1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator2" id="operator2"><option value="and"><@spring.message '_search.boolean.and'/> &nbsp;</option><option value="or"><@spring.message '_search.boolean.or'/> </option><option value="not"><@spring.message '_search.boolean.not'/> </option></select></td>
                <td><select name="facet2" id="facet2"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="query2" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator3" id="operator3"><option value="and"><@spring.message '_search.boolean.and'/> &nbsp;</option><option value="or"><@spring.message '_search.boolean.or'/> </option><option value="not"><@spring.message '_search.boolean.not'/> </option></select></td>
                <td><select name="facet3" id="facet3"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="query3" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td colspan="3">&#160;</td>
            </tr>
            <tr>
                <td align="left"><input type="reset" value="<@spring.message '_portal.ui.reset.searchbox' />" /></td>
                <td>&#160;</td>
                <td align="right"><input id="searchsubmit2" type="submit" value="<@spring.message '_action.search' />" /></td>
            </tr>
         </table>
        </form>
    </div>
</#macro>


<#-- UI STYLED  -->
<#macro resultnav_styled>
        <div class="fg-buttonset fg-buttonset-multi">

            <#--<@spring.message '_portal.ui.navigation.results' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message '_portal.ui.navigation.of' /> ${pagination.getNumFound()?c}-->

            <#--<@spring.message '_portal.ui.navigation.page' />:-->
            <#list pagination.pageLinks as link>
            <#assign uiClassBorder = ""/>
            <#if link_index == 0>
                <#assign uiClassBorder = "ui-corner-left"/>
            </#if>
            <#if !link_has_next>
                <#assign uiClassBorder = "ui-corner-right"/>
            </#if>
                <#if link.linked>
                    <#assign lstart = link.start/>
                        <a
                                href="../../?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}"
                                class="fg-button ui-state-default ${uiClassBorder}"
                        >
                            ${link.display?c}
                        </a>
                 <#else>
                    <a class="fg-button ui-state-default ui-state-active ${uiClassBorder}">
                        <strong>${link.display?c}</strong>
                    </a>
                </#if>
            </#list>

            <#assign uiClassStatePrev = ""/>
            <#assign uiClassStateNext = ""/>
            <#if !pagination.previous>
                <#assign uiClassStatePrev = "ui-state-disabled">
            </#if>
            <#if !pagination.next>
                <#assign uiClassStateNext = "ui-state-disabled">
            </#if>
            <a
                    href="../../?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"
                    class="fg-button ui-state-default fg-button-icon-left ui-corner-all ${uiClassStatePrev}"
                    alt="<@spring.message '_action.alt.previous.page' />"
                    style="margin: 0 8px;"
                    >
               <span class="ui-icon ui-icon-circle-arrow-w"></span><@spring.message '_action.previous' />
            </a>
            <a
                    href="../../?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"
                    class="fg-button ui-state-default fg-button-icon-right ui-corner-all ${uiClassStateNext}"
                    alt="<@spring.message '_action.alt.next.page' />"
                    >
                    <span class="ui-icon ui-icon-circle-arrow-e"></span><@spring.message '_portal.ui.navigation.next' />
            </a>
        </div>
</#macro>

<#macro show_result_list resultBeanView >
    <#assign seq = resultBeanView.briefDocs>
    <#list seq as cell>
                 <h6>
                    <a class="fg-gray" href="${cell.fullDocUrl}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                        <@stringLimiter "${cell.title}" "100"/></a>
                </h6>
            <div class="brief-thumb-container">
                <a href="${cell.fullDocUrl}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true"><img class="thumb" id="thumb_${cell.index}" align="middle"
                                              src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}"
                                              alt="<@spring.message '_action.alt.more.info' />" height="50"/>
                    <#else><img class="thumb" id="thumb_${cell.index?c}" align="middle" src="${cell.thumbnail}"
                                alt="Click for more information" height="50"
                                onerror="showDefaultSmall(this,'${cell.type}')"/>
                    </#if>
                </a>
            </div>

        <div class="brief-content-container">

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
                    <#-- TODO: MAKE AS OVERLAY -->
                    <#if cell.debugQuery??> ${cell.debugQuery}</#if>
                </p>
        </div>
    </#list>

</#macro>

<#--<#assign queryStringForPresentation = queryStringForPresentation/>-->
<#--<#assign queryToSave = queryToSave />-->
<#assign view = "table"/>
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.start??>
    <#assign start = "${RequestParameters.start}"/>
    <#else>
        <#assign start = "1"/>
</#if>
<#if RequestParameters.query??>
    <#assign justTheQuery = "${RequestParameters.query}"/>
</#if>

<#include "includeMarcos.ftl">

<@addHeader "Norvegiana", "",["results.js"],[]/>
<script type="text/javascript">
    var msgSearchSaveSuccess = "<@spring.message '_portal.ui.message.success.search.saved'/>";
    var msgSearchSaveFail = "<@spring.message '_mine.user.notification.failure.search.saved'/>";
</script>

<section class="grid_3" role="complementary">
    <header id="branding">
        <a href="/${portalName}/" title=""/>
        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana"/>
        </a>
        <h1 class="large">${portalDisplayName}</h1>
    </header>

    <h3><@spring.message '_action.refine.your.search' /></h3>
    <nav id="facetList">
        <@resultBriefFacets "DATAPROVIDER",  "_search.field.provider", 2/>
        <@resultBriefFacets "COUNTY",  "_metadata.abm.county", 2/>
        <@resultBriefFacets "MUNICIPALITY",  "_metadata.abm.municipality", 2/>
        <#-- TODO: Create this facet -------------------------->
        <#-- Norvegiana: Add "by About Person" ---------------->
        <#----------------------------------------------------->
        <@resultBriefFacets "DCTYPE",  "_metadata.dc.type", 2/>
    </nav>

    <nav id="userActions">
        <@resultsBriefUserActions/>
    </nav>
</section>

<section class="grid_9" id="results" role="main">

    <div id="userBar" role="navigation">
        <div class="inner">
        <@userBar/>
        </div>
    </div>

    <div class="clear"></div>

    <div id="search" role="search">
        <div class="inner">
            <@simpleSearch/>
        </div>
    </div>

    <div class="clear"></div>           

    <div id="nav_query_breadcrumbs">
        <div class="inner">
            <h4><@resultBriefQueryBreadcrumbs/></h4>
        </div>
    </div>

    <div class="clear"></div>

    <div id="result_overview">

        <div id="result_count">
            <div class="inner">
            <@spring.message '_portal.ui.navigation.results' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message '_portal.ui.navigation.of' /> ${pagination.getNumFound()?c}
            </div>
        </div>
    
            <div id="result_view_select">
                <div class="inner">
                <@viewSelect/>
                </div>
            </div>

        <div id="result_sort">
            <div class="inner">
            <@sortResults/>
            </div>
        </div>

    </div>


    <div class="clear"></div>

    <nav class="pagination">
        <div class="inner">
            <@resultBriefPaginationStyled/>
        </div>
    </nav>

    <div class="inner">
    <#if briefDocs?size &gt; 0>
        <#if view = "table">
            <@resultBriefGrid/>
        <#else>
            <@resultBriefList/>
        </#if>
    <#else>
        <div id="no-result"><@spring.message '_portal.ui.notification.noitemsfound' /></div>
    </#if>
    </div>

    <nav class="pagination">
        <div class="inner">
            <@resultBriefPaginationStyled/>
        </div>
    </nav>

</section>


<@addFooter/>


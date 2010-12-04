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

<@addHeader "Delving", "",["results.js"],[]/>
<script type="text/javascript">
    var msgSearchSaveSuccess = "<@spring.message 'SearchSaved_t'/>";
    var msgSearchSaveFail = "<@spring.message 'SearchSavedFailed_t'/>";
</script>

<section class="grid_3" role="complementary">
    <h3 class="header"><@spring.message 'RefineYourSearch_t' /></h3>
    <nav id="facetList">
        <@resultBriefFacets "DATAPROVIDER",  "Provider_t", 1/>

        <#-- TODO: Create this facet -------------------------->
        <#-- Norvegiana: Add "by About Person" ---------------->
        <#----------------------------------------------------->
        <@resultBriefFacets "DCTYPE",  "dc_type_t", 2/>
    </nav>

    <nav id="userActions">
        <@resultsBriefUserActions/>
    </nav>
</section>

<section class="grid_9" id="results" role="main">

    <div id="nav_query_breadcrumbs">
            <h4><@resultBriefQueryBreadcrumbs/></h4>
    </div>

    <div class="clear"></div>

    <div id="result_overview">

        <div id="result_count">
            <div class="inner">
            <@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}
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
        <div id="no-result"><@spring.message 'NoItemsFound_t' /></div>
    </#if>
    </div>

    <nav class="pagination">
        <div class="inner">
            <@resultBriefPaginationStyled/>
        </div>
    </nav>

</section>


<@addFooter/>


<#import "spring_form_macros.ftl" as spring />
<#assign thisPage = "exception.html">
<#assign pageId = "exc"/>
<#assign view = "table"/>
<#assign query = ""/>
<#if RequestParameters.view?exists>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query?exists>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "inc_header.ftl">

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

<div id="main" class="grid_10 prefix_2">

    <h3 style="margin: 60px 0 20px 0">Something went wrong! An email has been sent to inform our technical staff.</h3>
    <p style="margin: 0 0 240px 0">Please try another search.</p>

</div>

<#include "inc_footer.ftl"/>



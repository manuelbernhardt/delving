<#import "spring.ftl" as spring />
<#assign query = ""/>
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query??>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "spring_form_macros.ftl"/>
<#include "inc_header.ftl">

<div id="sidebar" class="grid_3">

    <div id="identity">
            <h1>Delving</h1>
            <a href="/${portalName}/index.html" title="ABM"><img src="/${portalName}/${portalTheme}/images/abm-logo.jpg" alt="ABM"/></a>
    </div>

</div>

<div id="main" class="grid_9">

    <div id="top-bar">
        <@userbar/>
        <#include "language_select.ftl">
    </div>

    <div class="clear"></div>

        <h1>Error</h1>
        <br />
        <p>
            The link you used to complete registration is invalid or has expired.
            Please <a href="/${portalName}/login.html">register your email again</a> to finish registration.
        </p>

</div>

<#include "inc_footer.ftl"/>

</body>
</html>

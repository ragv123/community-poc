<%@page session="false"%><%
%><%@ include file="/libs/foundation/global.jsp" %><%
%><body class="scf-guide"><sling:include path="./header"/>
<cq:includeClientLib categories="cq.ckeditor, cq.social.hbs.voting, cq.social.hbs.journal"/>
<div class="cg container-fluid" style="margin-top: 50px">
	<div class="row-fluid">
        <div class="span12">
            <sling:include path="./navigation" resourceType="/apps/community-components/components/navigation"/>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <sling:include path="./content"/>
        </div>
	</div>
</div>
<cq:include path="clientcontext" resourceType="cq/personalization/components/clientcontext"/>
</body>
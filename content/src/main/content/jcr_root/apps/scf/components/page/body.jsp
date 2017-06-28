<%@page session="false"%><%
%><%@ include file="/libs/foundation/global.jsp" %><%
%><body class="scf-guide"><div class="screen"><div class="unscreen">
<div class="cg container-fluid">
	<div class="row-fluid">
        <div class="span12">
            <sling:include path="./header"/>
            <sling:include path="./navigation" resourceType="/apps/scf/components/navigation"/>
            <sling:include path="./content"/>
        </div>
	</div>
</div>
<cq:include path="clientcontext" resourceType="cq/personalization/components/clientcontext"/>
</div></div>
</body>
<%@page session="false"
        import="java.util.Arrays,
				java.util.List"%><%
%><%@ include file="/libs/foundation/global.jsp" %><%
    final String propertyName = "scg:requiredClientLibs";
	final List<String> clientLibs = Arrays.asList(properties.get(propertyName, new String[0]));
	if ( clientLibs.size() > 0) {%>
<%for (String category : clientLibs) {%>

    <cq:includeClientLib categories="<%=category%>" />
    <%}%>

<%} else {
        %>&nbsp;<%
}%>

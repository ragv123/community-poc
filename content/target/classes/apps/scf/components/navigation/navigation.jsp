<%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false"
      		import="java.util.Iterator"%><%
%><%
	final Page rootPage = currentPage.getAbsoluteParent(2);
	final Iterator<Page> iterChildPages = rootPage.listChildren(null, true);
	Page childPage;
	int previousDepth = rootPage.getDepth() + 1;
%><ul class="nav nav-pills">
<li class="<%=(currentPage.getPath().equals(rootPage.getPath()))?"active":""%>"><a href="<%=rootPage.getPath()%>.html">Home</a></li>
<%
	while(iterChildPages.hasNext()) {
		childPage = iterChildPages.next();
		if (childPage.getDepth() > previousDepth) {
			%><ul class="nav nav-list"><%
        } else if (childPage.getDepth() < previousDepth) {
            %></ul><%
        }
        previousDepth = childPage.getDepth();%>
		<li class="<%=(currentPage.getPath().equals(childPage.getPath()))?"active":""%>"><a href="<%=childPage.getPath()%>.html"><%=xssAPI.encodeForHTML(childPage.getTitle())%></a></li><%
    }%>

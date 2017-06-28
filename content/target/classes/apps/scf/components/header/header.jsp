<%@include file="/libs/foundation/global.jsp"%><%
%><%@taglib prefix="personalization" uri="http://www.day.com/taglibs/cq/personalization/1.0" %><%
%><%@page session="false"
      import="javax.jcr.Session,
          org.apache.sling.api.resource.ResourceResolver,
        com.day.cq.wcm.api.WCMMode,
        com.day.cq.personalization.UserPropertiesUtil"%><%
  final Page rootGuidePage = currentPage.getAbsoluteParent(2);
  Session session = resourceResolver.adaptTo(Session.class);
  boolean isImpersonated = (session != null && session.getAttribute(ResourceResolver.USER_IMPERSONATOR) != null);
  boolean isAuthor = (WCMMode.fromRequest(request) != WCMMode.DISABLED);
%>
<div class="header-bar" style="background-color:#fff;">
    <div class="login-ui" style="background-image: none;">
        <span class="head-logo">SCF - Intellimeet</span>
            <button class="btn pull-right logout hidden" type="submit">Logout</button>
      <div class="btn-group pull-right">
                <a class="btn dropdown-toggle login" data-toggle="dropdown" href="#">Login</a>
                <ul class="dropdown-menu">
          <form>
                        <label for="j_username">Username:</label>
                        <input name="j_username"></input>
                        <label for="j_password">Password:</label>
                      <input name="j_password" type="password"></input>
                    <input name="j_validate" value="true" class="hidden"/>
                    <button type="submit" class="btn">Submit</button>
                    </form>
                </ul>
          </div>
  </div>
</div>

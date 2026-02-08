<%@ page contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %><%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    String json = (String) request.getAttribute("jsonResponse");
    if (json != null) {
        out.print(json);
    } else {
        out.print("{\"status\":\"error\",\"code\":500,\"message\":\"No JSON data\",\"data\":null}");
    }
%>
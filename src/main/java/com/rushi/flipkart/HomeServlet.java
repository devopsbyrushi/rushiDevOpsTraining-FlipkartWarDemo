package com.rushi.flipkart;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * ========================================================
 *  Flipkart Clone - WAR Version (Web Application)
 *  Developed by: Rushi
 *  Module: DevOps Multi-Cloud → Maven Day 02
 * ========================================================
 *
 * This servlet handles the "/welcome" endpoint and is
 * deployed inside Apache Tomcat (port 8080).
 *
 * Build  : mvn clean package
 * Deploy : Copy target/flipkart-clone.war into Tomcat's webapps/ folder
 * Open   : http://localhost:8080/flipkart-clone/         → home page (index.html)
 *          http://localhost:8080/flipkart-clone/welcome  → this servlet
 */
@WebServlet("/welcome")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Welcome - Flipkart Clone</title>");
        out.println("<style>");
        out.println("  body { font-family: 'Segoe UI', sans-serif; background: #2874f0;");
        out.println("         color: white; text-align: center; padding: 80px 20px; }");
        out.println("  h1 { font-size: 36px; margin-bottom: 12px; }");
        out.println("  .badge { background: #ff6b35; padding: 8px 20px; border-radius: 30px;");
        out.println("           display: inline-block; margin: 14px; font-weight: bold; }");
        out.println("  a { color: #f9d71c; font-size: 18px; }");
        out.println("</style></head><body>");
        out.println("<h1>👋 Welcome to Flipkart Clone (WAR)</h1>");
        out.println("<div class='badge'>🚀 Application is being developed by Rushi</div>");
        out.println("<p>Deployed on Apache Tomcat — Maven Day 02 Demo</p>");
        out.println("<p style='margin-top:30px;'>");
        out.println("  <a href='index.html'>🏠 Go to Home Page</a>");
        out.println("</p>");
        out.println("</body></html>");
    }
}

package com.example.elk;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")  // Apply to all incoming requests
public class CORSFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization (if needed)
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Set CORS headers for all requests
    httpResponse.setHeader("Access-Control-Allow-Origin", "*");  // Or a specific origin
    httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    httpResponse.setHeader("Access-Control-Allow-Credentials", "true");  // Optional for credentials

    // Handle OPTIONS preflight requests
    if ("OPTIONS".equalsIgnoreCase(((jakarta.servlet.http.HttpServletRequest) request).getMethod())) {
      httpResponse.setStatus(HttpServletResponse.SC_OK);  // 200 OK for preflight
      return;
    }

    // Continue with the request chain
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // Cleanup if needed
  }
}

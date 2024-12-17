package com.example.elk;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import org.json.simple.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "FileUploadServlet", urlPatterns = { "/fileuploadservlet" })
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
        maxFileSize = 1024 * 1024 * 10,      // 10 MB
        maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class FileUploadServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");


//    cres.getHeaders().add("Access-Control-Allow-Origin", "*");
//    cres.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
//    cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
//    cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
//    cres.getHeaders().add("Access-Control-Max-Age", "1209600");
//    CORSFilter filterChain = new CORSFilter();
//    filterChain.doFilter(request, response, filterChain);
    String path = new File("Front-End").getAbsolutePath();
    /* Receive file uploaded to the Servlet from the HTML5 form */
    //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    Part filePart = request.getPart("file");
    String fileName = filePart.getSubmittedFileName();
    for (Part part : request.getParts()) {
      part.write(path + fileName);
    }
    System.out.println(fileName);
    System.out.println("File uploaded");
    response.setStatus(200);
    response.setContentType("application/json");

    PrintWriter out = response.getWriter();

    File file = new File(path+fileName);
    JSONShape[] shapesList;
    try {
      shapesList = new DXFReader().parseFile(file);
      JSONArray array = new JSONArray();
      for (JSONShape s : shapesList) {
        array.add(s.writeJSONShape());
        array.addAll(s.writeJSONSubfeatures());
      }

      out.print(array);
    } catch (IOException e) {
      System.out.println("Error opening file :(");
    }

    out.flush();
  }

}


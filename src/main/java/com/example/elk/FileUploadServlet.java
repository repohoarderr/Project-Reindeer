package com.example.elk;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import org.glassfish.jersey.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
@WebServlet(name = "FileUploadServlet", urlPatterns = { "/fileuploadservlet" })
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
        maxFileSize = 1024 * 1024 * 10,      // 10 MB
        maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class FileUploadServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String path = new File("Front-End").getAbsolutePath();
    /* Receive file uploaded to the Servlet from the HTML5 form */
    Part filePart = request.getPart("file");
    String fileName = filePart.getSubmittedFileName();
    for (Part part : request.getParts()) {
      part.write(path + fileName);
    }
    System.out.println(fileName);
    System.out.println("File uploaded");
    response.setStatus(200);
    response.setContentType("application/json");


    File file = new File(path+fileName);
    Shape[] shapesList;
    try {
      shapesList = new DXFReader().parseFile(file, 14, 3);
      for (Shape s : shapesList) {
        System.out.println(s);
      }
    } catch (IOException e) {
      System.out.println("Error opening file :(");
    }
  }

}
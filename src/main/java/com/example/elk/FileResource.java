package com.example.elk;

import com.fasterxml.jackson.core.format.DataFormatDetector;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import org.glassfish.jersey.*;

import java.awt.*;
import java.io.*;

@Path("/file-upload")
public class FileResource {
  @GET
  @Produces({MediaType.TEXT_HTML})
  public InputStream viewHome() throws FileNotFoundException {

    File f = new File("/Users/andrewlong/Documents/IntelliJ/elk/html/home.html");
    return new FileInputStream(f);
  }
}


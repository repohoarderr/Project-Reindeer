package com.example.elk;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.IOException;

@Path("/file-upload")
public class FileResource {
  @GET
  @Produces("text/plain")
  public String fileText(){ return ""; }
}


const apiService = {
  /**
   * Uploads a file directly to the external server.
   *
   * @param {File} file - The file to be uploaded
   * @returns {Promise<string>} The response data from the server as a string
   */
  uploadFile: async (file) => {
    const formData = new FormData();
    formData.append("file", file); // Append the file to form data

    // Send the file directly to the external server (localhost:8080)
    const response = await fetch(
      "http://localhost:8080/elk_war_exploded/fileuploadservlet",
      {
        method: "POST",
        body: formData,
      },
    );

    if (!response.ok) {
      throw new Error("File upload failed");
    }

    const data = await response.text(); // Get response as plain text (string)
    return data; // Return the response data as a string
  },
};

export default apiService;

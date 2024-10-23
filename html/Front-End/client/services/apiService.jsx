const apiService = {
  /**
   * Uploads a file to the backend server using a POST request.
   *
   * @param {File} file - The file to be uploaded. This is expected to be a .dxf file, as specified in the form.
   * @returns {Promise<string>} - A promise that resolves to the server's response data as a string.
   *                              This response will be passed back to the calling function in the front-end.
   * @throws {Error} - If the server responds with a non-OK status, an error is thrown.
   */
  uploadFile: async (file) => {
    // Create a new FormData object, which allows us to send files and other data as a form submission.
    const formData = new FormData();

    // Append the selected file to the FormData object under the "file" key.
    formData.append("file", file);

    // Send the form data to the server via a POST request.
    // Replace "http://localhost:8080/elk_war_exploded/fileuploadservlet" with the correct server endpoint.
    const response = await fetch(
        "http://localhost:8080/elk_war_exploded/fileuploadservlet",
        {
          method: "POST", // Use the POST method for sending the file.
          body: formData, // Attach the formData object as the body of the request.
        }
    );

    // Check if the server responded with a status other than OK (status 200-299).
    if (!response.ok) {
      // If not OK, throw an error, which will be caught in the calling function (e.g., UploadForm).
      throw new Error("File upload failed");
    }

    // If the upload is successful, extract the response as plain text (since we expect a string response).
    const data = await response.text();

    // Return the server's response, which will be used by the front-end to display the upload results.
    return data;
  },
};

export default apiService;

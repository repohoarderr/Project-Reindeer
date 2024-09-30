/**
 * apiService handles API requests related to file uploads.
 */
const apiService = {
  /**
   * Uploads a file to the server.
   *
   * @param {File} file - The file to be uploaded
   * @returns {Promise<Object>} The response data from the server
   */
  uploadFile: async (file) => {
    const formData = new FormData();
    formData.append("file", file); // Append file to form data

    const response = await fetch("/api/upload", {
      method: "POST",
      body: formData,
    });

    if (!response.ok) {
      throw new Error("File upload failed");
    }

    return await response.json(); // Return the response data
  },

  // Add more API request functions here as needed
};

export default apiService;

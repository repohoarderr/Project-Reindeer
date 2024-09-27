document
  .getElementById("uploadForm")
  .addEventListener("submit", function (event) {
    event.preventDefault();

    const fileInput = document.getElementById("fileInput");
    const file = fileInput.files[0];

    if (file) {
      const formData = new FormData();
      formData.append("file", file);

      fetch("https://your-backend-url.com/upload", {
        method: "POST",
        body: formData,
      })
        .then((response) => response.json())
        .then((data) => {
          console.log("Success:", data);
        })
        .catch((error) => {
          console.error("Error:", error);
        });
    } else {
      alert("Please select a file to upload.");
    }
  });

document.getElementById("removeFile").addEventListener("click", function () {
  const fileInput = document.getElementById("fileInput");
  fileInput.value = ""; // Clears the selected file
  alert("File selection has been cleared.");
});

import Express from "express";
import multer from "multer"; // For handling file uploads

const router = Express.Router();
const upload = multer({ dest: "uploads/" }); // Set destination for uploaded files

// API endpoint to handle file uploads
router.post("/upload", upload.single("file"), (req, res) => {
  const file = req.file;
  if (file) {
    res.json({ results: `File ${file.originalname} uploaded successfully.` });
  } else {
    res.status(400).json({ error: "File upload failed." });
  }
});

export default router;

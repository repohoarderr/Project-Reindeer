import Express from "express";

const dataRouter = new Express.Router();

dataRouter.get("/data", (req, res) => {
  res.json({
    data: "Hello, world!",
  });
});

export default dataRouter;

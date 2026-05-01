import express from "express";
import { authMe } from "../controllers/userController.js";
import { test } from "../controllers/userController.js";

const router = express.Router();

router.get("/me", authMe);

router.get("/test", test);

export default router;

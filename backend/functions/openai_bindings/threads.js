const { onRequest } = require("firebase-functions/v2/https");
const express = require("express");
const { OpenAI } = require("openai");
const { logger } = require("firebase-functions");

const app = express();
// OpenAI API key from .env loaded in index.js
const apiKey = process.env.OPENAI_API_KEY;
const openaiClient = new OpenAI({ apiKey: apiKey });
const messagesRouter = require("./messages");
const runsRouter = require("./runs");
const { sendJSONResponse, validateThreadId, handleError } = require("./utils");

app.use(express.json());

app.post("/", async (req, res) => {
  try {
    const newThread = await openaiClient.beta.threads.create();
    logger.debug("Created thread: ", newThread);
    sendJSONResponse(res, 200, newThread);
  } catch (e) {
    handleError(e, res, "creating thread");
  }
});

app.get("/", async (req, res) => {
  sendJSONResponse(res, 405, { error: "Method not allowed" });
});

app.get("/:id", async (req, res) => {
  if (!validateThreadId(req, res)) return;

  try {
    const thread = await openaiClient.beta.threads.retrieve(req.params.id);
    logger.debug("Retrieved thread: ", thread);
    sendJSONResponse(res, 200, thread);
  } catch (e) {
    handleError(e, res, "retrieving thread");
  }
});

app.use("/", messagesRouter);
app.use("/", runsRouter);
exports.threads = onRequest(app);

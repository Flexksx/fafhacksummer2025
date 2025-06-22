const express = require("express");
const { OpenAI } = require("openai");
const { logger } = require("firebase-functions");
const router = express.Router();
// OpenAI API key from .env loaded in index.js
const apiKey = process.env.OPENAI_API_KEY;
const openaiClient = new OpenAI({ apiKey: apiKey });
const {
  sendJSONResponse,
  validateThreadId,
  formatMessage,
  handleError,
} = require("./utils");

// Get messages from thread
router.get("/:id/messages", async (req, res) => {
  if (!validateThreadId(req, res)) return;

  try {
    const messages = await openaiClient.beta.threads.messages.list(
      req.params.id
    );
    const messagesData = messages.data.map(formatMessage);

    logger.debug("Returned messages: ", messagesData);
    sendJSONResponse(res, 200, messagesData);
  } catch (e) {
    handleError(e, res, "getting messages");
  }
});

// Add message to thread
router.post("/:id/messages", async (req, res) => {
  if (!validateThreadId(req, res)) return;

  try {
    const newMessage = await openaiClient.beta.threads.messages.create(
      req.params.id,
      {
        role: "user",
        content: req.body.content,
      }
    );

    const messageDto = formatMessage(newMessage);
    logger.debug("Created message: ", messageDto);
    sendJSONResponse(res, 200, messageDto);
  } catch (e) {
    handleError(e, res, "creating message");
  }
});

module.exports = router;

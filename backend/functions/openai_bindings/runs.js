const express = require("express");
const { OpenAI } = require("openai");
const { logger } = require("firebase-functions");
const router = express.Router();
// OpenAI API key from .env loaded in index.js
const apiKey = process.env.OPENAI_API_KEY;
const openaiClient = new OpenAI({ apiKey: apiKey });
// Could also be loaded from environment variables
const assistantId =
  process.env.OPENAI_ASSISTANT_ID || "asst_qiqsbYM7YTvl4WlyScnMK4gZ";
const {
  sendJSONResponse,
  validateThreadId,
  validateRunId,
  formatRun,
  handleError,
} = require("./utils");

router.post("/:id/runs", async (req, res) => {
  if (!validateThreadId(req, res)) return;

  try {
    const threadId = req.params.id;
    const run = await openaiClient.beta.threads.runs.create(threadId, {
      assistant_id: assistantId,
    });

    logger.debug("Created run: ", run);
    const runDto = formatRun(run);
    sendJSONResponse(res, 200, runDto);
  } catch (e) {
    handleError(e, res, "creating run");
  }
});

router.get("/:id/runs/:runId", async (req, res) => {
  if (!validateThreadId(req, res)) return;
  if (!validateRunId(req, res)) return;

  try {
    const run = await openaiClient.beta.threads.runs.retrieve(
      req.params.id,
      req.params.runId
    );

    logger.debug("Returned run: ", run);
    const runDto = formatRun(run);
    sendJSONResponse(res, 200, runDto);
  } catch (e) {
    handleError(e, res, "getting run");
  }
});

router.delete("/:id/runs/:runId", async (req, res) => {
  if (!validateThreadId(req, res)) return;
  if (!validateRunId(req, res)) return;

  try {
    await openaiClient.beta.threads.runs.cancel(
      req.params.id,
      req.params.runId
    );

    logger.debug("Canceled run: ", req.params.runId);
    sendJSONResponse(res, 204, null);
  } catch (e) {
    if (e.message.startsWith("404")) {
      sendJSONResponse(res, 404, { error: "Run not found" });
    } else if (e.message.startsWith("400")) {
      sendJSONResponse(res, 400, { error: "Run already canceled" });
    } else {
      handleError(e, res, "canceling run");
    }
  }
});

module.exports = router;

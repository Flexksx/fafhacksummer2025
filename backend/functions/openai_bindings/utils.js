const { logger } = require("firebase-functions");

/**
 * Helper function to send JSON responses with proper formatting
 * @param {Object} res - Express response object
 * @param {number} status - HTTP status code
 * @param {Object} data - Data to send in the response
 * @returns {Object} Express response
 */
const sendJSONResponse = (res, status, data) => {
  return res.status(status).send(JSON.stringify(data));
};

/**
 * Validates thread ID from request parameters
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 * @returns {boolean} True if thread ID is valid, false otherwise
 */
const validateThreadId = (req, res) => {
  if (!req.params.id) {
    sendJSONResponse(res, 400, { error: "Thread ID not provided" });
    return false;
  }
  return true;
};

/**
 * Validates run ID from request parameters
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 * @returns {boolean} True if run ID is valid, false otherwise
 */
const validateRunId = (req, res) => {
  if (!req.params.runId) {
    sendJSONResponse(res, 400, { error: "Run ID not provided" });
    return false;
  }
  return true;
};

/**
 * Standard message formatter
 * @param {Object} message - OpenAI message object
 * @returns {Object} Formatted message object
 */
const formatMessage = (message) => {
  return {
    id: message.id,
    created_at: message.created_at,
    content: message.content,
    role: message.role,
  };
};

/**
 * Standard run formatter
 * @param {Object} run - OpenAI run object
 * @returns {Object} Formatted run object
 */
const formatRun = (run) => {
  return {
    id: run.id,
    created_at: run.created_at,
    thread_id: run.thread_id,
    status: run.status,
  };
};

/**
 * Standard error handler for API requests
 * @param {Error} error - Error object
 * @param {Object} res - Express response object
 * @param {string} operation - Description of the operation that failed
 */
const handleError = (error, res, operation) => {
  logger.error(`Error in ${operation}: `, error);
  sendJSONResponse(res, 500, { error: error.message });
};

module.exports = {
  sendJSONResponse,
  validateThreadId,
  validateRunId,
  formatMessage,
  formatRun,
  handleError,
};

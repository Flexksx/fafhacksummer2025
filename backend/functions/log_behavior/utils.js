const { logger } = require("firebase-functions");

/**
 * Sends a consistent JSON response.
 * @param {object} res - The Express response object.
 * @param {number} status - The HTTP status code.
 * @param {object} data - The JSON data to send.
 * @returns {object} The Express response.
 */
const sendJSONResponse = (res, status, data) => {
  return res.status(status).json(data);
};

/**
 * Handles errors in a centralized way.
 * @param {Error} error - The error object.
 * @param {object} res - The Express response object.
 * @param {string} operation - A string describing the failed operation (e.g., "logging behavior").
 */
const handleError = (error, res, operation) => {
  logger.error(`Error during ${operation}:`, error);
  
  sendJSONResponse(res, 500, { error: "An internal server error occurred." });
};

module.exports = {
  sendJSONResponse,
  handleError,
};
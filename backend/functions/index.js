const path = require("path");
const dotenv = require("dotenv");
dotenv.config({ path: path.resolve(__dirname, "../.env") });

const openai_bindings = require("./openai_bindings/threads");
exports.openai_bindings = openai_bindings;

const log_behavior = require("./log_behavior/main");
exports.log_behavior = log_behavior;

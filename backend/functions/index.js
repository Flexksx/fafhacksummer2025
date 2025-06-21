const path = require("path");
const dotenv = require("dotenv");
dotenv.config({ path: path.resolve(__dirname, "../.env") });

const openai_bindings = require("./openai_bindings/threads");

exports.openai_bindings = openai_bindings;

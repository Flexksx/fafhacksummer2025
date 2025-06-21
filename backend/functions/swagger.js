const swaggerJsdoc = require("swagger-jsdoc");
const swaggerUi = require("swagger-ui-express");

// Swagger definition
const swaggerDefinition = {
  openapi: "3.0.0",
  info: {
    title: "OpenAI API Bindings",
    version: "1.0.0",
    description: "RESTful API for interacting with OpenAI API",
    contact: {
      name: "API Support",
    },
    license: {
      name: "MIT",
    },
  },
  servers: [
    {
      url: "/api",
      description: "Development server",
    },
  ],
  components: {
    schemas: {
      Thread: {
        type: "object",
        properties: {
          id: {
            type: "string",
            description: "Thread ID",
          },
          created_at: {
            type: "integer",
            description: "Creation timestamp",
          },
        },
      },
      Message: {
        type: "object",
        properties: {
          id: {
            type: "string",
            description: "Message ID",
          },
          created_at: {
            type: "integer",
            description: "Creation timestamp",
          },
          content: {
            type: "array",
            description: "Message content",
            items: {
              type: "object",
              properties: {
                type: {
                  type: "string",
                  description: "Content type (text)",
                },
                text: {
                  type: "object",
                  description: "Text content",
                  properties: {
                    value: {
                      type: "string",
                      description: "Content value",
                    },
                  },
                },
              },
            },
          },
          role: {
            type: "string",
            description: "Role (user, assistant)",
            enum: ["user", "assistant"],
          },
        },
      },
      Run: {
        type: "object",
        properties: {
          id: {
            type: "string",
            description: "Run ID",
          },
          created_at: {
            type: "integer",
            description: "Creation timestamp",
          },
          thread_id: {
            type: "string",
            description: "Thread ID",
          },
          status: {
            type: "string",
            description: "Run status",
            enum: ["queued", "in_progress", "completed", "failed", "cancelled"],
          },
        },
      },
      Error: {
        type: "object",
        properties: {
          error: {
            type: "string",
            description: "Error message",
          },
        },
      },
    },
  },
};

// Options for the swagger docs
const options = {
  swaggerDefinition,
  // Paths to files containing OpenAPI definitions
  apis: ["./openai_bindings/*.js"],
};

// Initialize swagger-jsdoc
const swaggerSpec = swaggerJsdoc(options);

module.exports = {
  swaggerServe: swaggerUi.serve,
  swaggerSetup: swaggerUi.setup(swaggerSpec, {
    explorer: true,
  }),
  swaggerSpec,
};

const express = require("express");
const admin = require("firebase-admin");
const { FieldValue } = require("firebase-admin/firestore");
const { OpenAI } = require("openai");
const { onRequest } = require("firebase-functions/v2/https");
const { logger } = require("firebase-functions");
const swaggerUi = require("swagger-ui-express");
const swaggerJsdoc = require("swagger-jsdoc");

const { handleError, sendJSONResponse } = require("./utils");

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();
const apiKey = process.env.OPENAI_API_KEY;
const openaiClient = new OpenAI({ apiKey: apiKey });

const app = express();
app.use(express.json());

const swaggerOptions = {
  definition: {
    openapi: "3.0.0",
    info: {
      title: "Log Behavior API",
      version: "1.0.0",
      description: "API for logging child behavior events and getting AI analysis",
    },
    servers: [
      {
        url: "/", 
        description: "Cloud Function Endpoint",
      },
    ],
    components: {
      securitySchemes: {
        BearerAuth: {
          type: "http",
          scheme: "bearer",
          bearerFormat: "JWT",
        },
      },
    },
  },
  apis: [__filename],
};

const swaggerSpec = swaggerJsdoc(swaggerOptions);

app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));

// Add a root GET handler for debugging and redirecting to docs
app.get("/", (req, res) => {
  res.json({
    message: "Log Behavior API is running",
    documentation: `${req.protocol}://${req.get('host')}/api-docs`,
    endpoints: {
      "POST /": "Log a behavior event (requires authentication)",
      "GET /api-docs": "API documentation"
    }
  });
});

const buildAIPrompt = (childProfile, logData) => {
  const promptData = {
    childProfile: {
      name: childProfile.name || "the child",
      supportProfile: childProfile.supportProfile,
      sensoryProfile: childProfile.sensoryProfile,
    },
    behaviorLog: {
      parentEmotion: logData.parentEmotion,
      description: logData.eventDescription,
      context: logData.contextTags,
    },
    outputSchema: {
      analysis: {
        probableCause: "A short, descriptive snake_case string (e.g., SENSORY_OVERLOAD)",
        reassuranceText: "A personalized, empathetic message for the parent.",
        explanationText: "A clear explanation of the meltdown's cause.",
      },
      suggestedActivities: [
        {
          activityId: "a_unique_snake_case_id",
          title: "A short, clear title for the suggestion.",
          type: "One of: PREVENTATIVE, TOOL, IMMEDIATE_CALM_DOWN",
        },
      ],
    },
  };
  return JSON.stringify(promptData, null, 2);
};

const authenticate = async (req, res, next) => {
  if (!req.headers.authorization || !req.headers.authorization.startsWith('Bearer ')) {
    return sendJSONResponse(res, 403, { error: 'Unauthorized: No authorization token.' });
  }
  const idToken = req.headers.authorization.split('Bearer ')[1];
  try {
    req.uid = (await admin.auth().verifyIdToken(idToken)).uid;
    next();
  } catch (e) {
    logger.error("Error verifying auth token:", e);
    return sendJSONResponse(res, 403, { error: 'Unauthorized: Invalid token.' });
  }
};

const logBehaviorHandler = async (req, res) => {
    const parentId = req.uid || "test_parent_id"; // Use test ID when no auth
    const { childId, parentEmotion, eventDescription, contextTags } = req.body;
    
    if (!childId || !parentEmotion || !eventDescription || !contextTags) {
        return sendJSONResponse(res, 400, { error: "Missing required fields." });
    }

    try {
        const childDoc = await db.collection("children").doc(childId).get();
        if (!childDoc.exists) {
            return sendJSONResponse(res, 404, { error: "Child profile not found." });
        }

        const prompt = buildAIPrompt(childDoc.data(), req.body);
        const aiResponse = await openaiClient.chat.completions.create({
            model: "gpt-4-turbo",
            messages: [
                { role: "system", content: "You are a compassionate AI for parents of neurodivergent children. Analyze events, provide empathy, causes, and strategies in the specified JSON format." },
                { role: "user", content: prompt },
            ],
            response_format: { type: "json_object" },
        });

        const aiResultJson = JSON.parse(aiResponse.choices[0].message.content);        const logEntry = {
            parentId, childId, timestamp: FieldValue.serverTimestamp(),
            parentEmotion, eventDescription, contextTags,
            aiAnalysis: aiResultJson.analysis,
            suggestedActivities: aiResultJson.suggestedActivities,
        };
        await db.collection("behavior_logs").add(logEntry);

        return sendJSONResponse(res, 200, aiResultJson);
    } catch (e) {
        return handleError(e, res, "logging behavior");
    }
};

/**
 * @swagger
 * /:
 *   post:
 *     summary: Log a child behavior event
 *     description: Logs a behavior event for a child and returns AI analysis and suggested activities
 *     security:
 *       - BearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - childId
 *               - parentEmotion
 *               - eventDescription
 *               - contextTags
 *             properties:
 *               childId:
 *                 type: string
 *                 description: The ID of the child
 *                 example: "child123"
 *               parentEmotion:
 *                 type: string
 *                 description: The parent's emotional state during the event
 *                 example: "stressed"
 *               eventDescription:
 *                 type: string
 *                 description: Description of the behavior event
 *                 example: "Child had a meltdown at the grocery store"
 *               contextTags:
 *                 type: array
 *                 items:
 *                   type: string
 *                 description: Context tags for the event
 *                 example: ["loud_environment", "transition", "hunger"]
 *     responses:
 *       200:
 *         description: Successfully logged behavior and returned AI analysis
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 analysis:
 *                   type: object
 *                   properties:
 *                     probableCause:
 *                       type: string
 *                       description: Snake case string describing the probable cause
 *                       example: "SENSORY_OVERLOAD"
 *                     reassuranceText:
 *                       type: string
 *                       description: Empathetic message for the parent
 *                       example: "It's completely normal for children to feel overwhelmed in busy environments."
 *                     explanationText:
 *                       type: string
 *                       description: Clear explanation of the meltdown's cause
 *                       example: "The loud noises and crowded space likely triggered sensory overload."
 *                 suggestedActivities:
 *                   type: array
 *                   items:
 *                     type: object
 *                     properties:
 *                       activityId:
 *                         type: string
 *                         description: Unique identifier for the activity
 *                         example: "noise_cancelling_headphones"
 *                       title:
 *                         type: string
 *                         description: Short title for the suggestion
 *                         example: "Use noise-cancelling headphones"
 *                       type:
 *                         type: string
 *                         enum: ["PREVENTATIVE", "TOOL", "IMMEDIATE_CALM_DOWN"]
 *                         description: Type of suggested activity
 *                         example: "PREVENTATIVE"
 *       400:
 *         description: Missing required fields
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Missing required fields."
 *       403:
 *         description: Unauthorized - Invalid or missing token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Unauthorized: Invalid token."
 *       404:
 *         description: Child profile not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Child profile not found."
 *       500:
 *         description: Internal server error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "An internal server error occurred."
 */
app.post("/", logBehaviorHandler);

module.exports = onRequest(app);
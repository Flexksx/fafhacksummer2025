const express = require("express");
const admin = require("firebase-admin");
const { OpenAI } = require("openai");
const { onRequest } = require("firebase-functions/v2/https");
const { logger } = require("firebase-functions");

const { handleError, sendJSONResponse } = require("./utils"); 

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();
const apiKey = process.env.OPENAI_API_KEY;
const openaiClient = new OpenAI({ apiKey: apiKey });

const app = express();
app.use(express.json());

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
    const parentId = req.uid;
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

        const aiResultJson = JSON.parse(aiResponse.choices[0].message.content);

        const logEntry = {
            parentId, childId, timestamp: admin.firestore.FieldValue.serverTimestamp(),
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

app.post("/", authenticate, logBehaviorHandler);

module.exports = onRequest(app);
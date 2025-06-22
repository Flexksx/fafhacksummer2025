const { onRequest } = require("firebase-functions/v2/https");
const express = require("express");
const { OpenAI } = require("openai");
const { logger } = require("firebase-functions");
const {
  sendJSONResponse,
  validateActivityPlanRequest,
  createActivityPlanPrompt,
  createWeeklyRoutinePrompt,
  handleError,
  validateRoutineRequest,
  validateRoutineId,
  generateRoutineId,
  formatRoutine,
} = require("./utils");

const app = express();
app.use(express.json());

// OpenAI API key from .env loaded in index.js
const apiKey = process.env.OPENAI_API_KEY;
const openaiClient = new OpenAI({ apiKey: apiKey });

// In-memory storage for routines (in production, use a database)
const routines = new Map();

/**
 * POST /activity_plan
 * Creates a personalized activity plan based on developmental goals
 */
app.post("/", async (req, res) => {
  if (!validateActivityPlanRequest(req, res)) return;

  try {
    const { goal, childName, age, preferences, additionalInfo } = req.body;
    
    logger.info("Creating activity plan", { goal, childName, age, preferences, additionalInfo });

    const systemPrompt = createActivityPlanPrompt(goal, childName, age, preferences, additionalInfo);
    
    const completion = await openaiClient.chat.completions.create({
      model: "gpt-4",
      messages: [
        {
          role: "system",
          content: systemPrompt
        },
        {
          role: "user",
          content: `Please create a personalized activity plan for ${childName || 'the child'} to work on: "${goal}"`
        }
      ],
      max_tokens: 2000,
      temperature: 0.7,
    });

    const activityPlan = completion.choices[0].message.content;
    
    const response = {
      childName,
      age,
      goal,
      preferences,
      additionalInfo,
      activityPlan,
      createdAt: new Date().toISOString(),
    };

    logger.debug("Generated activity plan: ", response);
    sendJSONResponse(res, 200, response);
  } catch (e) {
    handleError(e, res, "creating activity plan");
  }
});

/**
 * POST /activity_plan/routine
 * Creates an AI-generated weekly routine based on goals and preferences
 */
app.post("/routine", async (req, res) => {
  try {
    const { goal, childName, age, preferences, additionalInfo } = req.body;
    
    // Validate required fields
    if (!goal || typeof goal !== 'string' || goal.trim() === '') {
      sendJSONResponse(res, 400, { error: "Goal is required and must be a non-empty string" });
      return;
    }
    
    logger.info("Creating AI-generated weekly routine", { goal, childName, age, preferences, additionalInfo });

    const systemPrompt = createWeeklyRoutinePrompt(goal, childName, age, preferences, additionalInfo);
    
    const completion = await openaiClient.chat.completions.create({
      model: "gpt-4",
      messages: [
        {
          role: "system",
          content: systemPrompt
        },
        {
          role: "user",
          content: `Create a weekly routine for ${childName || 'the child'} to work on: "${goal}"`
        }
      ],
      max_tokens: 1500,
      temperature: 0.7,
    });

    let aiGeneratedActivities;
    try {
      aiGeneratedActivities = JSON.parse(completion.choices[0].message.content);
    } catch (parseError) {
      logger.error("Failed to parse AI response", parseError);
      sendJSONResponse(res, 500, { error: "Failed to generate routine structure" });
      return;
    }

    const routineId = generateRoutineId();
    const routine = {
      id: routineId,
      childName: childName || 'Child',
      goal: goal,
      age: age,
      preferences: preferences,
      activities: aiGeneratedActivities.map(activity => ({
        name: activity.name,
        dayOfWeek: activity.dayOfWeek.toLowerCase(),
        time: activity.time,
        duration: activity.duration,
        materials: activity.materials || [],
        description: activity.description,
        completed: false
      })),
      weekStartDate: new Date().toISOString().split('T')[0],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      generatedByAI: true
    };

    routines.set(routineId, routine);
    
    logger.info("Created AI-generated routine", { routineId, childName, goal, activitiesCount: routine.activities.length });
    sendJSONResponse(res, 201, formatRoutine(routine));
  } catch (e) {
    handleError(e, res, "creating AI-generated routine");
  }
});

/**
 * POST /activity_plan/routine/manual
 * Creates a manual weekly routine from provided activities (original functionality)
 */
app.post("/routine/manual", async (req, res) => {
  if (!validateRoutineRequest(req, res)) return;

  try {
    const { childName, goal, activities, weekStartDate } = req.body;
    
    const routineId = generateRoutineId();
    const routine = {
      id: routineId,
      childName: childName || 'Child',
      goal: goal || 'General Development',
      activities: activities.map(activity => ({
        name: activity.name,
        dayOfWeek: activity.dayOfWeek.toLowerCase(),
        time: activity.time || '09:00',
        duration: activity.duration || '15-20 minutes',
        materials: activity.materials || [],
        description: activity.description || '',
        completed: false
      })),
      weekStartDate: weekStartDate || new Date().toISOString().split('T')[0],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      generatedByAI: false
    };

    routines.set(routineId, routine);
    
    logger.info("Created manual routine", { routineId, childName, goal });
    sendJSONResponse(res, 201, formatRoutine(routine));
  } catch (e) {
    handleError(e, res, "creating manual routine");
  }
});

/**
 * GET /activity_plan/routine/:routineId
 * Retrieves a specific routine
 */
app.get("/routine/:routineId", async (req, res) => {
  if (!validateRoutineId(req, res)) return;

  try {
    const routine = routines.get(req.params.routineId);
    
    if (!routine) {
      sendJSONResponse(res, 404, { error: "Routine not found" });
      return;
    }

    logger.debug("Retrieved routine", { routineId: req.params.routineId });
    sendJSONResponse(res, 200, formatRoutine(routine));
  } catch (e) {
    handleError(e, res, "retrieving routine");
  }
});

/**
 * PUT /activity_plan/routine/:routineId
 * Updates a routine (e.g., marking activities as completed)
 */
app.put("/routine/:routineId", async (req, res) => {
  if (!validateRoutineId(req, res)) return;

  try {
    const routine = routines.get(req.params.routineId);
    
    if (!routine) {
      sendJSONResponse(res, 404, { error: "Routine not found" });
      return;
    }

    const { activities, weekStartDate } = req.body;
    
    if (activities) {
      routine.activities = activities.map(activity => ({
        ...activity,
        dayOfWeek: activity.dayOfWeek.toLowerCase()
      }));
    }
    
    if (weekStartDate) {
      routine.weekStartDate = weekStartDate;
    }
    
    routine.updatedAt = new Date().toISOString();
    routines.set(req.params.routineId, routine);

    logger.info("Updated routine", { routineId: req.params.routineId });
    sendJSONResponse(res, 200, formatRoutine(routine));
  } catch (e) {
    handleError(e, res, "updating routine");
  }
});

/**
 * POST /activity_plan/routine/:routineId/complete
 * Marks a specific activity as completed
 */
app.post("/routine/:routineId/complete", async (req, res) => {
  if (!validateRoutineId(req, res)) return;

  try {
    const { activityName, dayOfWeek } = req.body;
    
    if (!activityName || !dayOfWeek) {
      sendJSONResponse(res, 400, { error: "Activity name and day of week are required" });
      return;
    }

    const routine = routines.get(req.params.routineId);
    
    if (!routine) {
      sendJSONResponse(res, 404, { error: "Routine not found" });
      return;
    }

    const activity = routine.activities.find(a => 
      a.name === activityName && a.dayOfWeek === dayOfWeek.toLowerCase()
    );
    
    if (!activity) {
      sendJSONResponse(res, 404, { error: "Activity not found in routine" });
      return;
    }

    activity.completed = true;
    activity.completedAt = new Date().toISOString();
    routine.updatedAt = new Date().toISOString();
    
    routines.set(req.params.routineId, routine);

    logger.info("Marked activity as completed", { 
      routineId: req.params.routineId, 
      activityName, 
      dayOfWeek 
    });
    
    sendJSONResponse(res, 200, { 
      message: "Activity marked as completed",
      activity: activity
    });
  } catch (e) {
    handleError(e, res, "completing activity");
  }
});

/**
 * GET /activity_plan/routine/:routineId/reminders
 * Gets today's activities for reminders
 */
app.get("/routine/:routineId/reminders", async (req, res) => {
  if (!validateRoutineId(req, res)) return;

  try {
    const routine = routines.get(req.params.routineId);
    
    if (!routine) {
      sendJSONResponse(res, 404, { error: "Routine not found" });
      return;
    }

    const today = new Date().toLocaleDateString('en-US', { weekday: 'long' }).toLowerCase();
    const todaysActivities = routine.activities.filter(activity => 
      activity.dayOfWeek === today
    );

    const reminders = todaysActivities.map(activity => ({
      routineId: req.params.routineId,
      childName: routine.childName,
      goal: routine.goal,
      activity: activity.name,
      time: activity.time,
      duration: activity.duration,
      materials: activity.materials,
      completed: activity.completed
    }));

    logger.debug("Retrieved daily reminders", { 
      routineId: req.params.routineId, 
      today, 
      count: reminders.length 
    });
    
    sendJSONResponse(res, 200, {
      day: today,
      reminders: reminders
    });
  } catch (e) {
    handleError(e, res, "getting reminders");
  }
});

exports.activity_plan = onRequest(app);

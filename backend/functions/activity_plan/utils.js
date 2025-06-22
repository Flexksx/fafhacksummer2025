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
 * Validates activity plan request body
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 * @returns {boolean} True if request is valid, false otherwise
 */
const validateActivityPlanRequest = (req, res) => {
  const { goal, childName, age, preferences } = req.body;
  
  if (!goal || typeof goal !== 'string' || goal.trim() === '') {
    sendJSONResponse(res, 400, { error: "Goal is required and must be a non-empty string" });
    return false;
  }
  
  if (age && (typeof age !== 'number' || age < 0 || age > 18)) {
    sendJSONResponse(res, 400, { error: "Age must be a number between 0 and 18" });
    return false;
  }
  
  if (childName && typeof childName !== 'string') {
    sendJSONResponse(res, 400, { error: "Child name must be a string" });
    return false;
  }
  
  if (preferences && typeof preferences !== 'object') {
    sendJSONResponse(res, 400, { error: "Preferences must be an object" });
    return false;
  }
  
  return true;
};

/**
 * Creates a system prompt for activity plan generation
 * @param {string} goal - The developmental goal
 * @param {string} childName - Child's name
 * @param {number} age - Child's age in years
 * @param {Object} preferences - Child's preferences and interests
 * @param {string} additionalInfo - Additional context
 * @returns {string} System prompt for OpenAI
 */
const createActivityPlanPrompt = (goal, childName, age, preferences, additionalInfo) => {
  const childInfo = childName ? `Child Name: ${childName}` : 'Child Name: Not specified';
  const ageInfo = age ? `Age: ${age} years` : 'Age: Not specified';
  
  let preferencesInfo = 'Preferences: None specified';
  if (preferences) {
    const { favoriteActivities, interests, environment } = preferences;
    preferencesInfo = `Preferences:
- Favorite Activities: ${favoriteActivities ? favoriteActivities.join(', ') : 'Not specified'}
- Interests: ${interests ? interests.join(', ') : 'Not specified'}
- Preferred Environment: ${environment || 'Not specified'}`;
  }

  return `You are an expert developmental therapist and coach for children with special needs. Create a personalized activity plan for a child.

${childInfo}
${ageInfo}
Goal: ${goal}
${preferencesInfo}
Additional Information: ${additionalInfo || 'None provided'}

Please provide a structured activity plan with the following format:
1. A brief explanation of why this goal is important (2-3 sentences)
2. 5-7 specific, play-based activities that target this goal
3. For each activity, include:
   - Activity name
   - Simple instructions (2-3 sentences)
   - Materials needed
   - Developmental domains addressed (e.g., +Fine Motor, +Sensory, +Social Skills)
   - Estimated duration
   - Location (indoor/outdoor/both)

IMPORTANT: Personalize the activities based on the child's preferences and interests. If they like animals, incorporate animal themes. If they prefer indoor activities, focus on those. Make the activities feel tailored specifically for this child.

Focus on activities that are:
- Simple and achievable for parents
- Fun and engaging for the child
- Use common household items when possible
- Appropriate for the child's age
- Evidence-based for developmental progress
- Aligned with the child's interests and preferences

Format your response as a clear, structured plan that empowers parents to be their child's coach.`;
};

/**
 * Creates a system prompt for AI-generated weekly routine
 * @param {string} goal - The developmental goal
 * @param {string} childName - Child's name
 * @param {number} age - Child's age in years
 * @param {Object} preferences - Child's preferences and interests
 * @param {string} additionalInfo - Additional context
 * @returns {string} System prompt for OpenAI
 */
const createWeeklyRoutinePrompt = (goal, childName, age, preferences, additionalInfo) => {
  const childInfo = childName ? `Child Name: ${childName}` : 'Child Name: Not specified';
  const ageInfo = age ? `Age: ${age} years` : 'Age: Not specified';
  
  let preferencesInfo = 'Preferences: None specified';
  if (preferences) {
    const { favoriteActivities, interests, environment } = preferences;
    preferencesInfo = `Preferences:
- Favorite Activities: ${favoriteActivities ? favoriteActivities.join(', ') : 'Not specified'}
- Interests: ${interests ? interests.join(', ') : 'Not specified'}
- Preferred Environment: ${environment || 'Not specified'}`;
  }

  return `You are an expert developmental therapist creating a weekly routine for a child with special needs. Create a structured, realistic weekly schedule that parents can follow.

${childInfo}
${ageInfo}
Goal: ${goal}
${preferencesInfo}
Additional Information: ${additionalInfo || 'None provided'}

Create a weekly routine with 5-7 activities spread across the week. For each activity, provide:
- Activity name (clear and engaging)
- Day of week (monday, tuesday, etc.)
- Recommended time (in HH:MM format, consider realistic family schedules)
- Duration (realistic timeframes like "15 minutes" or "20-25 minutes")
- Materials needed (common household items when possible)
- Brief description (1-2 sentences on what to do)

IMPORTANT Guidelines:
- Distribute activities across different days (don't overload any single day)
- Consider realistic family schedules (avoid very early mornings or late evenings)
- Include variety: some short (10-15 min), some longer (20-30 min) activities
- Personalize based on the child's interests and preferences
- Make activities progressive throughout the week
- Include both active and quiet activities
- Consider the child's energy levels at different times

Format your response as a JSON array of activities like this:
[
  {
    "name": "Activity Name",
    "dayOfWeek": "monday",
    "time": "09:30",
    "duration": "15 minutes",
    "materials": ["item1", "item2"],
    "description": "Brief description of what to do"
  }
]

Respond ONLY with the JSON array, no additional text.`;
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

/**
 * Validates routine/calendar request body
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 * @returns {boolean} True if request is valid, false otherwise
 */
const validateRoutineRequest = (req, res) => {
  const { activities, weekStartDate } = req.body;
  
  if (!activities || !Array.isArray(activities) || activities.length === 0) {
    sendJSONResponse(res, 400, { error: "Activities array is required and must not be empty" });
    return false;
  }
  
  if (weekStartDate && !isValidDate(weekStartDate)) {
    sendJSONResponse(res, 400, { error: "Week start date must be a valid ISO date string" });
    return false;
  }
  
  // Validate each activity in the array
  for (let i = 0; i < activities.length; i++) {
    const activity = activities[i];
    if (!activity.name || typeof activity.name !== 'string') {
      sendJSONResponse(res, 400, { error: `Activity ${i + 1}: name is required and must be a string` });
      return false;
    }
    if (!activity.dayOfWeek || !['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'].includes(activity.dayOfWeek.toLowerCase())) {
      sendJSONResponse(res, 400, { error: `Activity ${i + 1}: dayOfWeek must be a valid day name` });
      return false;
    }
    if (activity.time && !isValidTime(activity.time)) {
      sendJSONResponse(res, 400, { error: `Activity ${i + 1}: time must be in HH:MM format` });
      return false;
    }
  }
  
  return true;
};

/**
 * Validates routine ID from request parameters
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 * @returns {boolean} True if routine ID is valid, false otherwise
 */
const validateRoutineId = (req, res) => {
  if (!req.params.routineId || req.params.routineId.trim() === '') {
    sendJSONResponse(res, 400, { error: "Routine ID is required" });
    return false;
  }
  return true;
};

/**
 * Helper function to validate date string
 * @param {string} dateString - Date string to validate
 * @returns {boolean} True if valid date
 */
const isValidDate = (dateString) => {
  const date = new Date(dateString);
  return date instanceof Date && !isNaN(date);
};

/**
 * Helper function to validate time string (HH:MM format)
 * @param {string} timeString - Time string to validate
 * @returns {boolean} True if valid time
 */
const isValidTime = (timeString) => {
  const timeRegex = /^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/;
  return timeRegex.test(timeString);
};

/**
 * Generates a unique routine ID
 * @returns {string} Unique routine ID
 */
const generateRoutineId = () => {
  return 'routine_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

/**
 * Formats a routine object for response
 * @param {Object} routine - Routine object
 * @returns {Object} Formatted routine
 */
const formatRoutine = (routine) => {
  return {
    id: routine.id,
    childName: routine.childName,
    goal: routine.goal,
    activities: routine.activities,
    weekStartDate: routine.weekStartDate,
    createdAt: routine.createdAt,
    updatedAt: routine.updatedAt
  };
};

module.exports = {
  sendJSONResponse,
  validateActivityPlanRequest,
  createActivityPlanPrompt,
  createWeeklyRoutinePrompt,
  handleError,
  validateRoutineRequest,
  validateRoutineId,
  generateRoutineId,
  formatRoutine,
};

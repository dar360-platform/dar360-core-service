import twilio from 'twilio';

const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const twilioPhoneNumber = process.env.TWILIO_PHONE_NUMBER;

const isMockMode = process.env.NODE_ENV === 'development' && !accountSid;
const client = isMockMode ? null : twilio(accountSid, authToken);

export async function sendSms(to: string, message: string) {
  // MOCK MODE for development without Twilio credentials
  if (isMockMode) {
    console.log('📱 [MOCK SMS] Would send to:', to);
    console.log('📱 [MOCK SMS] Message:', message);
    return {
      sid: `MOCK_${Date.now()}`,
      status: 'sent',
      to,
      from: 'MOCK_NUMBER',
    };
  }

  // REAL MODE with Twilio
  if (!twilioPhoneNumber) {
    throw new Error('TWILIO_PHONE_NUMBER is not set in environment variables');
  }
  if (!accountSid || !authToken) {
    throw new Error('TWILIO_ACCOUNT_SID or TWILIO_AUTH_TOKEN is not set in environment variables');
  }

  try {
    const response = await client!.messages.create({
      body: message,
      to: to,
      from: twilioPhoneNumber,
    });
    console.log(`✅ SMS sent to ${to}: ${response.sid}`);
    return response;
  } catch (error) {
    console.error(`❌ Error sending SMS to ${to}:`, error);
    throw error;
  }
}

import twilio from 'twilio';

const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const twilioPhoneNumber = process.env.TWILIO_PHONE_NUMBER;

const client = twilio(accountSid, authToken);

export async function sendSms(to: string, message: string) {
  if (!twilioPhoneNumber) {
    throw new Error('TWILIO_PHONE_NUMBER is not set in environment variables');
  }
  if (!accountSid || !authToken) {
    throw new Error('TWILIO_ACCOUNT_SID or TWILIO_AUTH_TOKEN is not set in environment variables');
  }

  try {
    const response = await client.messages.create({
      body: message,
      to: to,
      from: twilioPhoneNumber,
    });
    console.log(`SMS sent to ${to}: ${response.sid}`);
    return response;
  } catch (error) {
    console.error(`Error sending SMS to ${to}:`, error);
    throw error;
  }
}
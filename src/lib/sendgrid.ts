import { Resend } from 'resend';

const isMockMode = process.env.NODE_ENV === 'development' && !process.env.RESEND_API_KEY;

// Lazy instantiation - only create client when RESEND_API_KEY is present
const resend = (process.env.RESEND_API_KEY && !isMockMode)
  ? new Resend(process.env.RESEND_API_KEY)
  : null;

const FROM_EMAIL = process.env.RESEND_FROM_EMAIL || 'onboarding@resend.dev';

export async function sendEmail(to: string | string[], subject: string, html: string, text?: string) {
  // MOCK MODE for development without Resend credentials
  if (isMockMode || !resend) {
    console.log('📧 [MOCK EMAIL] Would send to:', to);
    console.log('📧 [MOCK EMAIL] Subject:', subject);
    console.log('📧 [MOCK EMAIL] Body:', text || html.substring(0, 100) + '...');
    return;
  }

  // REAL MODE with Resend
  if (!process.env.RESEND_API_KEY) {
    throw new Error('RESEND_API_KEY is not set in environment variables');
  }

  const recipients = Array.isArray(to) ? to : [to];

  try {
    const { data, error } = await resend!.emails.send({
      from: FROM_EMAIL,
      to: recipients,
      subject,
      html,
      text: text || html,
    });

    if (error) {
      console.error(`❌ Error sending email to ${to}:`, error);
      throw error;
    }

    console.log(`✅ Email sent to ${to} with subject: ${subject}`);
  } catch (error: any) {
    console.error(`❌ Error sending email to ${to}:`, error);
    throw error;
  }
}

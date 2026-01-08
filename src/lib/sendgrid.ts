import sgMail from '@sendgrid/mail';

sgMail.setApiKey(process.env.SENDGRID_API_KEY as string);
const FROM_EMAIL = process.env.SENDGRID_FROM_EMAIL || 'noreply@dar360.ae';

export async function sendEmail(to: string | string[], subject: string, html: string, text?: string) {
  if (!process.env.SENDGRID_API_KEY) {
    throw new Error('SENDGRID_API_KEY is not set in environment variables');
  }

  const msg = {
    to,
    from: FROM_EMAIL,
    subject,
    html,
    text: text || html,
  };

  try {
    await sgMail.send(msg);
    console.log(`Email sent to ${to} with subject: ${subject}`);
  } catch (error: any) {
    console.error(`Error sending email to ${to}:`, error.response?.body || error);
    throw error;
  }
}
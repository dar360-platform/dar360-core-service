import prisma from '@/lib/db';
import { sendSms as twilioSendSms } from '@/lib/twilio';
import { sendEmail as sendgridSendEmail } from '@/lib/sendgrid';
import { NotificationLog } from '@prisma/client';

export class NotificationService {
  private async logNotification(
    type: string,
    recipient: string,
    template: string,
    status: string,
    externalId?: string,
    error?: string
  ): Promise<NotificationLog> {
    return prisma.notificationLog.create({
      data: {
        type,
        recipient,
        template,
        status,
        externalId,
        error,
      },
    });
  }

  async sendSms(to: string, message: string, templateCode: string) {
    let status = 'SENT';
    let externalId: string | undefined;
    let error: string | undefined;

    try {
      const response = await twilioSendSms(to, message);
      externalId = response.sid;
    } catch (err: any) {
      status = 'FAILED';
      error = err.message;
      console.error('Failed to send SMS:', err);
      throw err;
    } finally {
      await this.logNotification('SMS', to, templateCode, status, externalId, error);
    }
  }

  async sendEmail(to: string | string[], subject: string, html: string, templateCode: string, text?: string) {
    let status = 'SENT';
    let error: string | undefined;

    try {
      await sendgridSendEmail(to, subject, html, text);
    } catch (err: any) {
      status = 'FAILED';
      error = err.message;
      console.error('Failed to send email:', err);
      throw err;
    } finally {
      // SendGrid does not easily provide a single external ID for each email sent through sgMail.send()
      // For simplicity, externalId is omitted here.
      const recipientString = Array.isArray(to) ? to.join(', ') : to;
      await this.logNotification('EMAIL', recipientString, templateCode, status, undefined, error);
    }
  }
}

export const notificationService = new NotificationService();
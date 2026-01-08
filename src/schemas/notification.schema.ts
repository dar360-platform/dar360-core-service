import { z } from 'zod';

export const sendSmsSchema = z.object({
  to: z.string().min(1, 'Recipient phone number is required'),
  message: z.string().min(1, 'Message content is required'),
  templateCode: z.string().optional(),
});

export const sendEmailSchema = z.object({
  to: z.union([z.string().email(), z.array(z.string().email()).min(1, 'At least one recipient is required')]),
  subject: z.string().min(1, 'Email subject is required'),
  html: z.string().min(1, 'Email HTML content is required'),
  text: z.string().optional(),
  templateCode: z.string().optional(),
});

export type SendSmsSchema = z.infer<typeof sendSmsSchema>;
export type SendEmailSchema = z.infer<typeof sendEmailSchema>;
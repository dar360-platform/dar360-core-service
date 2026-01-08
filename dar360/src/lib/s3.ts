import { S3Client, PutObjectCommand, DeleteObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';

const s3Client = new S3Client({
  region: process.env.AWS_REGION,
  credentials: {
    accessKeyId: process.env.AWS_ACCESS_KEY_ID as string,
    secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY as string,
  },
});

const S3_BUCKET = process.env.AWS_S3_BUCKET;

export async function uploadToS3(fileBuffer: Buffer, fileName: string, contentType: string) {
  if (!S3_BUCKET) {
    throw new Error('AWS_S3_BUCKET is not defined in environment variables');
  }
  const command = new PutObjectCommand({
    Bucket: S3_BUCKET,
    Key: fileName,
    Body: fileBuffer,
    ContentType: contentType,
  });

  await s3Client.send(command);

  return `https://${S3_BUCKET}.s3.${process.env.AWS_REGION}.amazonaws.com/${fileName}`;
}

export async function deleteFromS3(fileName: string) {
  if (!S3_BUCKET) {
    throw new Error('AWS_S3_BUCKET is not defined in environment variables');
  }
  const command = new DeleteObjectCommand({
    Bucket: S3_BUCKET,
    Key: fileName,
  });

  await s3Client.send(command);
}

export async function getSignedUploadUrl(fileName: string, contentType: string) {
  if (!S3_BUCKET) {
    throw new Error('AWS_S3_BUCKET is not defined in environment variables');
  }
  const command = new PutObjectCommand({
    Bucket: S3_BUCKET,
    Key: fileName,
    ContentType: contentType,
  });
  const signedUrl = await getSignedUrl(s3Client, command, { expiresIn: 3600 }); // URL expires in 1 hour
  return signedUrl;
}

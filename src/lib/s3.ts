import { S3Client, PutObjectCommand, DeleteObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { createClient } from '@supabase/supabase-js';

// Determine storage mode: Supabase > AWS S3 > Mock
const hasSupabase = !!(process.env.NEXT_PUBLIC_SUPABASE_URL && process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY);
const hasS3 = !!process.env.AWS_S3_BUCKET;
const isMockMode = !hasSupabase && !hasS3;

// Initialize Supabase client if credentials are available
const supabase = hasSupabase ? createClient(
  process.env.NEXT_PUBLIC_SUPABASE_URL as string,
  process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY as string
) : null;

// Initialize S3 client only if Supabase is not available
const s3Client = hasS3 && !hasSupabase ? new S3Client({
  region: process.env.AWS_REGION,
  credentials: {
    accessKeyId: process.env.AWS_ACCESS_KEY_ID as string,
    secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY as string,
  },
}) : null;

const S3_BUCKET = process.env.AWS_S3_BUCKET;
const SUPABASE_BUCKET = 'contracts';

export async function uploadToS3(fileBuffer: Buffer, fileName: string, contentType: string) {
  // MOCK MODE for development without storage credentials
  if (isMockMode) {
    const mockUrl = `https://mock-storage-bucket.example.com/${fileName}`;
    console.log('📦 [MOCK STORAGE] Would upload file:', fileName);
    console.log('📦 [MOCK STORAGE] Content-Type:', contentType);
    console.log('📦 [MOCK STORAGE] Size:', fileBuffer.length, 'bytes');
    console.log('📦 [MOCK STORAGE] Mock URL:', mockUrl);
    return mockUrl;
  }

  // SUPABASE STORAGE MODE (preferred)
  if (hasSupabase && supabase) {
    console.log('📦 [SUPABASE] Uploading file:', fileName);

    const { data, error } = await supabase.storage
      .from(SUPABASE_BUCKET)
      .upload(fileName, fileBuffer, {
        contentType,
        upsert: true,
      });

    if (error) {
      console.error('❌ Supabase upload error:', error);
      throw new Error(`Failed to upload to Supabase: ${error.message}`);
    }

    const { data: publicData } = supabase.storage
      .from(SUPABASE_BUCKET)
      .getPublicUrl(fileName);

    console.log('✅ File uploaded to Supabase:', publicData.publicUrl);
    return publicData.publicUrl;
  }

  // AWS S3 MODE (fallback)
  if (hasS3 && s3Client) {
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
    console.log('✅ File uploaded to AWS S3');
    return `https://${S3_BUCKET}.s3.${process.env.AWS_REGION}.amazonaws.com/${fileName}`;
  }

  throw new Error('No storage provider configured');
}

export async function deleteFromS3(fileName: string) {
  if (isMockMode) {
    console.log('📦 [MOCK STORAGE] Would delete file:', fileName);
    return;
  }

  if (hasSupabase && supabase) {
    console.log('📦 [SUPABASE] Deleting file:', fileName);
    const { error } = await supabase.storage
      .from(SUPABASE_BUCKET)
      .remove([fileName]);

    if (error) {
      console.error('❌ Supabase delete error:', error);
      throw new Error(`Failed to delete from Supabase: ${error.message}`);
    }
    console.log('✅ File deleted from Supabase');
    return;
  }

  if (hasS3 && s3Client) {
    if (!S3_BUCKET) {
      throw new Error('AWS_S3_BUCKET is not defined in environment variables');
    }
    const command = new DeleteObjectCommand({
      Bucket: S3_BUCKET,
      Key: fileName,
    });
    await s3Client.send(command);
    console.log('✅ File deleted from AWS S3');
    return;
  }

  throw new Error('No storage provider configured');
}

export async function getSignedUploadUrl(fileName: string, contentType: string) {
  if (isMockMode) {
    const mockSignedUrl = `https://mock-storage-bucket.example.com/upload/${fileName}?signature=mock-signature-123`;
    console.log('📦 [MOCK STORAGE] Would generate signed URL for:', fileName);
    return mockSignedUrl;
  }

  if (hasSupabase && supabase) {
    console.log('📦 [SUPABASE] Generating signed upload URL for:', fileName);
    const { data, error } = await supabase.storage
      .from(SUPABASE_BUCKET)
      .createSignedUploadUrl(fileName);

    if (error) {
      console.error('❌ Supabase signed URL error:', error);
      throw new Error(`Failed to create signed URL: ${error.message}`);
    }
    console.log('✅ Signed URL generated');
    return data.signedUrl;
  }

  if (hasS3 && s3Client) {
    if (!S3_BUCKET) {
      throw new Error('AWS_S3_BUCKET is not defined in environment variables');
    }
    const command = new PutObjectCommand({
      Bucket: S3_BUCKET,
      Key: fileName,
      ContentType: contentType,
    });
    const signedUrl = await getSignedUrl(s3Client, command, { expiresIn: 3600 });
    console.log('✅ Signed URL generated (AWS S3)');
    return signedUrl;
  }

  throw new Error('No storage provider configured');
}

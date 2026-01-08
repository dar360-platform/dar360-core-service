import prisma from '@/lib/db';
import { ContractStatus, Prisma } from '@prisma/client';
import { notificationService } from './notification.service';
import { generatePdf } from '@/lib/pdf';
import { uploadToS3 } from '@/lib/s3';
import { propertyService } from './property.service'; // Assuming propertyService is needed to get owner details

export class ContractService {

  // Generate contract number
  private async generateContractNumber(): Promise<string> {
    const year = new Date().getFullYear();
    const count = await prisma.contract.count({
      where: {
        contractNumber: { startsWith: `DAR-${year}` },
      },
    });
    return `DAR-${year}-${String(count + 1).padStart(5, '0')}`;
  }

  // Create contract
  async create(data: {
    propertyId: string;
    agentId: string;
    tenantName: string;
    tenantPhone: string;
    tenantEmail: string;
    tenantEmiratesId: string;
    startDate: Date;
    endDate: Date;
    rentAmount: number;
    depositAmount: number;
  }) {
    const property = await propertyService.getPropertyById(data.propertyId); // Using propertyService
    // const property = await prisma.property.findUnique({ // Original from document
    //   where: { id: data.propertyId },
    //   include: { owner: true },
    // });

    if (!property) throw new Error('Property not found');

    const contractNumber = await this.generateContractNumber();

    return prisma.contract.create({
      data: {
        contractNumber,
        propertyId: data.propertyId,
        agentId: data.agentId,
        ownerId: property.ownerId,
        tenantName: data.tenantName,
        tenantPhone: data.tenantPhone,
        tenantEmail: data.tenantEmail,
        tenantEmiratesId: data.tenantEmiratesId,
        startDate: data.startDate,
        endDate: data.endDate,
        rentAmount: data.rentAmount,
        depositAmount: data.depositAmount,
        status: ContractStatus.DRAFT,
      },
    });
  }

  // Generate PDF
  async generatePdf(contractId: string) {
    const contract = await prisma.contract.findUnique({
      where: { id: contractId },
      include: {
        property: true,
        agent: true,
        owner: true,
      },
    });

    if (!contract) throw new Error('Contract not found');

    // Generate PDF from HTML template
    const pdfBuffer = await generatePdf('contract-template', contract);
    
    // Upload to S3
    const s3Key = `contracts/${contractId}/contract-unsigned.pdf`;
    const pdfUrl = await uploadToS3(pdfBuffer, s3Key, 'application/pdf');

    await prisma.contract.update({
      where: { id: contractId },
      data: { pdfUrl },
    });

    return { pdfUrl };
  }

  // Send OTP
  async sendOtp(contractId: string) {
    const contract = await prisma.contract.findUnique({
      where: { id: contractId },
    });

    if (!contract) throw new Error('Contract not found');
    if (!contract.pdfUrl) throw new Error('PDF must be generated first');
    if (contract.status === ContractStatus.SIGNED) throw new Error('Contract already signed');

    // Generate 6-digit OTP
    const otpCode = Math.floor(100000 + Math.random() * 900000).toString();
    const otpExpiresAt = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes

    await prisma.contract.update({
      where: { id: contractId },
      data: {
        otpCode,
        otpExpiresAt,
        otpAttempts: 0,
        status: ContractStatus.PENDING_SIGNATURE,
      },
    });

    // Send SMS
    const signingUrl = `${process.env.NEXT_PUBLIC_APP_URL}/sign/${contractId}`;
    await notificationService.sendSms(
      contract.tenantPhone,
      `Your Dar360 contract signing code is: ${otpCode}. Valid for 10 minutes. Sign here: ${signingUrl}`,
      'CONTRACT_SIGNING_OTP'
    );

    return { message: 'OTP sent successfully' };
  }

  // Verify OTP and sign
  async verifyOtpAndSign(contractId: string, otp: string, ipAddress: string) {
    const contract = await prisma.contract.findUnique({
      where: { id: contractId },
    });

    if (!contract) throw new Error('Contract not found');
    if (contract.status !== ContractStatus.PENDING_SIGNATURE) {
      throw new Error('Contract is not pending signature');
    }

    // Check if locked
    if (contract.otpAttempts >= 5) {
      throw new Error('Too many attempts. Please request a new OTP.');
    }

    // Check expiry
    if (!contract.otpExpiresAt || new Date() > contract.otpExpiresAt) {
      throw new Error('OTP has expired. Please request a new one.');
    }

    // Verify OTP
    if (contract.otpCode !== otp) {
      await prisma.contract.update({
        where: { id: contractId },
        data: { otpAttempts: { increment: 1 } },
      });
      throw new Error('Invalid OTP');
    }

    // OTP is valid - sign the contract
    // TODO: Generate signed PDF with signature stamp

    const updated = await prisma.contract.update({
      where: { id: contractId },
      data: {
        status: ContractStatus.SIGNED,
        signedAt: new Date(),
        signedIp: ipAddress,
        otpCode: null,
        otpExpiresAt: null,
      },
      include: {
        property: true,
        agent: true,
        owner: true,
      },
    });

    // Notify agent and owner
    try {
      // Notify Tenant
      await notificationService.sendSms(
        updated.tenantPhone,
        `Contract ${updated.contractNumber} has been successfully signed.`,
        'CONTRACT_SIGNED_TENANT'
      );

      // Notify Agent
      if (updated.agent?.phone) {
        await notificationService.sendSms(
          updated.agent.phone,
          `Contract ${updated.contractNumber} for ${updated.property.title} has been signed by the tenant.`,
          'CONTRACT_SIGNED_AGENT'
        );
      }

      // Notify Owner
      if (updated.owner?.phone) {
        await notificationService.sendSms(
          updated.owner.phone,
          `Contract ${updated.contractNumber} for your property ${updated.property.title} has been signed.`,
          'CONTRACT_SIGNED_OWNER'
        );
      }
    } catch (error) {
      console.error('Failed to send contract signed notifications:', error);
      // Don't throw here, as the contract is already signed
    }

    return { message: 'Contract signed successfully', contract: updated };
  }

  // Additional methods for contract management (not in example but needed for API)
  async getContractById(id: string) {
    return prisma.contract.findUnique({
      where: { id },
      include: {
        property: true,
        agent: { select: { id: true, fullName: true, email: true } },
        owner: { select: { id: true, fullName: true, email: true } },
      },
    });
  }

  async updateContract(id: string, data: Partial<any>) { // Using 'any' for now, should use a specific type based on schema
    return prisma.contract.update({
      where: { id },
      data: data,
      include: {
        property: true,
        agent: { select: { id: true, fullName: true, email: true } },
        owner: { select: { id: true, fullName: true, email: true } },
      },
    });
  }

  async searchContracts(params: {
    status?: ContractStatus;
    propertyId?: string;
    agentId?: string;
    ownerId?: string;
    page?: number;
    limit?: number;
  }) {
    const { page = 1, limit = 20, ...filters } = params;
    
    const where: Prisma.ContractWhereInput = {
      ...filters,
    };

    const [contracts, total] = await Promise.all([
      prisma.contract.findMany({
        where,
        skip: (page - 1) * limit,
        take: limit,
        include: {
          property: { select: { id: true, title: true, addressLine: true } },
          agent: { select: { id: true, fullName: true, agencyName: true } },
          owner: { select: { id: true, fullName: true } },
        },
        orderBy: { createdAt: 'desc' },
      }),
      prisma.contract.count({ where }),
    ]);

    return {
      data: contracts,
      pagination: { page, limit, total, totalPages: Math.ceil(total / limit) },
    };
  }
}

export const contractService = new ContractService();
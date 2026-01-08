import prisma from '@/lib/db';
import { PropertyStatus, PropertyType, Prisma } from '@prisma/client';
import { nanoid } from 'nanoid';
import { deleteFromS3 } from '@/lib/s3';

export class PropertyService {
  
  // Create property
  async create(data: {
    agentId: string;
    ownerId?: string;
    title: string;
    description?: string;
    type: PropertyType;
    bedrooms: number;
    bathrooms: number;
    rentAmount: number;
    addressLine: string;
    areaName?: string;
    amenities?: string[];
  }) {
    return prisma.property.create({
      data: {
        ...data,
        rentAmount: new Prisma.Decimal(data.rentAmount),
        status: PropertyStatus.DRAFT,
      },
      include: {
        agent: { select: { id: true, fullName: true, email: true } },
        owner: { select: { id: true, fullName: true, email: true } },
        images: true,
      },
    });
  }

  // Search properties
  async search(params: {
    type?: PropertyType;
    minBedrooms?: number;
    maxBedrooms?: number;
    minRent?: number;
    maxRent?: number;
    areaName?: string;
    agentId?: string;
    ownerId?: string;
    page?: number;
    limit?: number;
  }) {
    const { page = 1, limit = 20, ...filters } = params;
    
    const where: Prisma.PropertyWhereInput = {};

    // Default to AVAILABLE unless specific agent/owner is filtering (then they might want to see all)
    // Or strictly enforce AVAILABLE for public search.
    // For now: If agentId or ownerId is provided, show all statuses, otherwise show AVAILABLE.
    if (!filters.agentId && !filters.ownerId) {
        where.status = PropertyStatus.AVAILABLE;
    }

    if (filters.type) where.type = filters.type;
    if (filters.minBedrooms) where.bedrooms = { gte: filters.minBedrooms };
    if (filters.maxBedrooms) where.bedrooms = { ...where.bedrooms as any, lte: filters.maxBedrooms };
    if (filters.minRent) where.rentAmount = { gte: filters.minRent };
    if (filters.maxRent) where.rentAmount = { ...where.rentAmount as any, lte: filters.maxRent };
    if (filters.areaName) where.areaName = { contains: filters.areaName, mode: 'insensitive' };
    if (filters.agentId) where.agentId = filters.agentId;
    if (filters.ownerId) where.ownerId = filters.ownerId;

    const [properties, total] = await Promise.all([
      prisma.property.findMany({
        where,
        skip: (page - 1) * limit,
        take: limit,
        include: {
          images: { where: { isPrimary: true }, take: 1 },
          agent: { select: { id: true, fullName: true, agencyName: true } },
        },
        orderBy: { createdAt: 'desc' },
      }),
      prisma.property.count({ where }),
    ]);

    return {
      data: properties,
      pagination: { page, limit, total, totalPages: Math.ceil(total / limit) },
    };
  }

  // Generate share link
  async createShareLink(propertyId: string, agentId: string) {
    const property = await prisma.property.findUnique({
      where: { id: propertyId },
    });

    if (!property) throw new Error('Property not found');
    if (property.agentId !== agentId) throw new Error('Not authorized');

    const shareToken = nanoid(12);
    
    const share = await prisma.propertyShare.create({
      data: {
        propertyId,
        agentId,
        shareToken,
        channel: 'WHATSAPP',
      },
    });

    const shareUrl = `${process.env.NEXT_PUBLIC_APP_URL}/p/${shareToken}`;
    
    return {
      shareToken,
      shareUrl,
      whatsappUrl: `https://wa.me/?text=${encodeURIComponent(`Check out this property: ${shareUrl}`)}`,
    };
  }

  // Track share click
  async trackShareClick(shareToken: string) {
    await prisma.propertyShare.update({
      where: { shareToken },
      data: {
        clicks: { increment: 1 },
        lastClickedAt: new Date(),
      },
    });

    return prisma.propertyShare.findUnique({
      where: { shareToken },
      include: {
        property: {
          include: {
            images: { orderBy: { displayOrder: 'asc' } },
            agent: { select: { id: true, fullName: true, phone: true, agencyName: true } },
          },
        },
      },
    });
  }

  async getPropertyById(id: string) {
    return prisma.property.findUnique({
      where: { id },
      include: {
        agent: { select: { id: true, fullName: true, email: true } },
        owner: { select: { id: true, fullName: true, email: true } },
        images: true,
      },
    });
  }

  async updateProperty(id: string, data: Partial<{
    ownerId?: string;
    title?: string;
    description?: string;
    type?: PropertyType;
    bedrooms?: number;
    bathrooms?: number;
    rentAmount?: number;
    addressLine?: string;
    areaName?: string;
    amenities?: string[];
  }>) {
    return prisma.property.update({
      where: { id },
      data: {
        ...data,
        rentAmount: data.rentAmount ? new Prisma.Decimal(data.rentAmount) : undefined,
      },
      include: {
        agent: { select: { id: true, fullName: true, email: true } },
        owner: { select: { id: true, fullName: true, email: true } },
        images: true,
      },
    });
  }

  async deleteProperty(id: string) {
    return prisma.property.delete({
      where: { id },
    });
  }

  // New methods for image management
  async addPropertyImage(
    propertyId: string,
    url: string,
    s3Key: string,
    isPrimary: boolean,
    displayOrder: number,
  ) {
    return prisma.propertyImage.create({
      data: {
        propertyId,
        url,
        s3Key,
        isPrimary,
        displayOrder,
      },
    });
  }

  async deletePropertyImage(imageId: string) {
    const image = await prisma.propertyImage.findUnique({
      where: { id: imageId },
    });

    if (!image) throw new Error('Property image not found');

    await deleteFromS3(image.s3Key); // Delete from S3
    return prisma.propertyImage.delete({ // Delete from DB
      where: { id: imageId },
    });
  }
}

export const propertyService = new PropertyService();

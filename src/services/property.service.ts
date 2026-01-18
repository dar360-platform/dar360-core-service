import { Prisma, Property, PropertyImage, PropertyShare, PropertyStatus } from '@prisma/client';
import db from '@/lib/db';
import { searchPropertySchema } from '@/schemas/property.schema';
import { z } from 'zod';
import { nanoid } from 'nanoid';

async function create(data: Prisma.PropertyUncheckedCreateInput): Promise<Property> {
  return db.property.create({ data });
}

type SearchParams = z.infer<typeof searchPropertySchema> & { agentId?: string; ownerId?: string; };

async function search(params: SearchParams): Promise<{ data: Property[], pagination: any }> {
  const { page = 1, limit = 10, type, status, bedrooms_min, bedrooms_max, rent_min, rent_max, area_min, area_max, areaName, agentId, ownerId } = params;
  
  const where: Prisma.PropertyWhereInput = {};
  if (type) where.type = type;
  if (status) where.status = status;
  if (agentId) where.agentId = agentId;
  if (ownerId) where.ownerId = ownerId;
  if (areaName) where.areaName = { contains: areaName, mode: 'insensitive' };

  if (bedrooms_min !== undefined || bedrooms_max !== undefined) {
    where.bedrooms = {};
    if (bedrooms_min !== undefined) where.bedrooms.gte = bedrooms_min;
    if (bedrooms_max !== undefined) where.bedrooms.lte = bedrooms_max;
  }

  if (rent_min !== undefined || rent_max !== undefined) {
    where.rentAmount = {};
    if (rent_min !== undefined) where.rentAmount.gte = rent_min;
    if (rent_max !== undefined) where.rentAmount.lte = rent_max;
  }
  
  if (area_min !== undefined || area_max !== undefined) {
    where.areaSqft = {};
    if (area_min !== undefined) where.areaSqft.gte = area_min;
    if (area_max !== undefined) where.areaSqft.lte = area_max;
  }

  const [data, total] = await db.$transaction([
    db.property.findMany({
      where,
      skip: (page - 1) * limit,
      take: limit,
      orderBy: { createdAt: 'desc' },
      include: {
        images: {
          where: { isPrimary: true },
          take: 1
        }
      }
    }),
    db.property.count({ where }),
  ]);

  return {
    data,
    pagination: {
      total,
      page,
      limit,
      totalPages: Math.ceil(total / limit),
    },
  };
}

async function getPropertyById(id: string): Promise<Property | null> {
  return db.property.findUnique({ 
    where: { id },
    include: {
      images: true,
      agent: {
        select: {
          id: true,
          fullName: true,
          email: true,
          phone: true,
          agencyName: true
        }
      },
      owner: {
        select: {
          id: true,
          fullName: true,
          email: true,
          phone: true,
        }
      }
    }
  });
}

async function updateProperty(id: string, data: Prisma.PropertyUpdateInput): Promise<Property> {
  return db.property.update({ where: { id }, data });
}

async function deleteProperty(id: string): Promise<Property> {
  return db.property.delete({ where: { id } });
}

async function updatePropertyStatus(id: string, status: PropertyStatus): Promise<Property> {
  return db.property.update({ where: { id }, data: { status } });
}

async function addPropertyImage(propertyId: string, url: string, s3Key: string, isPrimary: boolean, displayOrder: number): Promise<PropertyImage> {
  if (isPrimary) {
    // Ensure no other image is set as primary
    await db.propertyImage.updateMany({
      where: { propertyId },
      data: { isPrimary: false },
    });
  }

  return db.propertyImage.create({
    data: {
      propertyId,
      url,
      s3Key,
      isPrimary,
      displayOrder,
    },
  });
}

async function getPropertyImageById(id: string): Promise<PropertyImage | null> {
  return db.propertyImage.findUnique({ where: { id } });
}

async function deletePropertyImage(id: string): Promise<PropertyImage> {
  return db.propertyImage.delete({ where: { id } });
}

async function createShareLink(propertyId: string, agentId: string): Promise<PropertyShare> {
  const shareToken = nanoid(10); // Generate a 10-character unique token
  return db.propertyShare.create({
    data: {
      propertyId,
      agentId,
      shareToken,
    },
  });
}

async function trackShareClick(token: string) {
  const share = await db.propertyShare.findUnique({
    where: { shareToken: token },
    include: {
      property: {
        include: {
          images: true,
          agent: {
            select: {
              fullName: true,
              agencyName: true,
            }
          }
        }
      }
    }
  });

  if (share) {
    await db.propertyShare.update({
      where: { id: share.id },
      data: {
        clicks: {
          increment: 1,
        },
        lastClickedAt: new Date(),
      },
    });
  }

  return share;
}


export const propertyService = {
  create,
  search,
  getPropertyById,
  updateProperty,
  deleteProperty,
  updatePropertyStatus,
  addPropertyImage,
  getPropertyImageById,
  deletePropertyImage,
  createShareLink,
  trackShareClick,
};

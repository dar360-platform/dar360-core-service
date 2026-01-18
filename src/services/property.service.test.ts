import { describe, it, expect, vi, beforeEach } from 'vitest';
import { propertyService } from './property.service';
import db from '@/lib/db';
import { PropertyType, RentFrequency, PropertyStatus, Prisma } from '@prisma/client';

// Mock the db module
vi.mock('@/lib/db', () => ({
  default: {
    property: {
      create: vi.fn(),
      findMany: vi.fn(),
      count: vi.fn(),
      findUnique: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
    },
    propertyImage: {
      create: vi.fn(),
      updateMany: vi.fn(),
      findUnique: vi.fn(),
      delete: vi.fn(),
    },
    propertyShare: {
      create: vi.fn(),
      findUnique: vi.fn(),
      update: vi.fn(),
    },
    $transaction: vi.fn(),
  },
}));

// Mock nanoid
vi.mock('nanoid', () => ({
    nanoid: () => 'test-token',
}));

describe('PropertyService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockProperty = {
    id: 'prop1',
    title: 'Amazing Villa',
    description: 'A beautiful villa in Dubai.',
    agentId: 'agent1',
    ownerId: 'owner1',
    type: PropertyType.VILLA,
    status: PropertyStatus.AVAILABLE,
    bedrooms: 5,
    bathrooms: 6,
    areaSqft: new Prisma.Decimal(5000.00),
    rentAmount: new Prisma.Decimal(250000.00),
    rentFrequency: RentFrequency.YEARLY,
    depositAmount: new Prisma.Decimal(25000.00),
    addressLine: '123, Palm Jumeirah',
    buildingName: 'Villa 123',
    areaName: 'Palm Jumeirah',
    city: 'Dubai',
    latitude: new Prisma.Decimal(25.1111),
    longitude: new Prisma.Decimal(55.1387),
    amenities: ['POOL', 'GYM'],
    permitNumber: '12345',
    createdAt: new Date(),
    updatedAt: new Date(),
  };

  it('should create a property', async () => {
    const propertyData = { ...mockProperty };
    (db.property.create as any).mockResolvedValue(propertyData);

    const result = await propertyService.create(propertyData as any);

    expect(db.property.create).toHaveBeenCalledWith({ data: propertyData });
    expect(result).toEqual(propertyData);
  });

  it('should get property by ID', async () => {
    (db.property.findUnique as any).mockResolvedValue(mockProperty);
    const property = await propertyService.getPropertyById('prop1');
    expect(db.property.findUnique).toHaveBeenCalledWith({
        where: { id: 'prop1' },
        include: expect.any(Object),
    });
    expect(property).toEqual(mockProperty);
  });

  it('should update a property', async () => {
    const updateData = { title: 'Updated Title' };
    (db.property.update as any).mockResolvedValue({ ...mockProperty, ...updateData });
    const updatedProperty = await propertyService.updateProperty('prop1', updateData);
    expect(db.property.update).toHaveBeenCalledWith({ where: { id: 'prop1' }, data: updateData });
    expect(updatedProperty.title).toBe('Updated Title');
  });

  it('should delete a property', async () => {
    (db.property.delete as any).mockResolvedValue(mockProperty);
    await propertyService.deleteProperty('prop1');
    expect(db.property.delete).toHaveBeenCalledWith({ where: { id: 'prop1' } });
  });

  it('should create a share link', async () => {
    const shareData = {
        id: 'share1',
        propertyId: 'prop1',
        agentId: 'agent1',
        shareToken: 'test-token',
        clicks: 0,
        lastClickedAt: null,
        createdAt: new Date()
    };
    (db.propertyShare.create as any).mockResolvedValue(shareData);

    const share = await propertyService.createShareLink('prop1', 'agent1');

    expect(db.propertyShare.create).toHaveBeenCalledWith({
      data: {
        propertyId: 'prop1',
        agentId: 'agent1',
        shareToken: 'test-token',
      },
    });
    expect(share).toEqual(shareData);
  });

  it('should track a share click', async () => {
    const share = { id: 'share1', propertyId: 'prop1', shareToken: 'test-token', property: mockProperty };
    (db.propertyShare.findUnique as any).mockResolvedValue(share);
    (db.propertyShare.update as any).mockResolvedValue({ ...share, clicks: 1 });

    const result = await propertyService.trackShareClick('test-token');

    expect(db.propertyShare.findUnique).toHaveBeenCalledWith({
        where: { shareToken: 'test-token' },
        include: expect.any(Object),
    });
    expect(db.propertyShare.update).toHaveBeenCalledWith({
        where: { id: share.id },
        data: {
            clicks: { increment: 1 },
            lastClickedAt: expect.any(Date),
        }
    });
    expect(result).toEqual(share);
  });

});

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { viewingService } from './viewing.service';
import db from '@/lib/db';
import { ViewingStatus, ViewingOutcome, Prisma } from '@prisma/client';

// Mock the db module
vi.mock('@/lib/db', () => ({
  default: {
    viewing: {
      create: vi.fn(),
      findFirst: vi.fn(),
      findUnique: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
      findMany: vi.fn(),
      count: vi.fn(),
    },
    $transaction: vi.fn(),
  },
}));

describe('ViewingService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const mockViewing = {
        id: 'view1',
        propertyId: 'prop1',
        agentId: 'agent1',
        tenantName: 'John Doe',
        tenantPhone: '123456789',
        tenantEmail: 'john.doe@example.com',
        scheduledAt: new Date(),
        status: ViewingStatus.SCHEDULED,
        outcome: null,
        notes: null,
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    it('should create a viewing if there are no conflicts', async () => {
        (db.viewing.findFirst as any).mockResolvedValue(null);
        (db.viewing.create as any).mockResolvedValue(mockViewing);

        const result = await viewingService.createViewing(mockViewing as any);

        expect(db.viewing.findFirst).toHaveBeenCalled();
        expect(db.viewing.create).toHaveBeenCalledWith({ data: mockViewing });
        expect(result).toEqual(mockViewing);
    });

    it('should throw an error if a conflicting viewing exists', async () => {
        (db.viewing.findFirst as any).mockResolvedValue(mockViewing);
        await expect(viewingService.createViewing(mockViewing as any)).rejects.toThrow(
          'A viewing for this property with this agent is already scheduled around this time.'
        );
    });

    it('should get a viewing by ID', async () => {
        (db.viewing.findUnique as any).mockResolvedValue(mockViewing);
        const viewing = await viewingService.getViewingById('view1');
        expect(db.viewing.findUnique).toHaveBeenCalledWith({
            where: { id: 'view1' },
            include: expect.any(Object),
        });
        expect(viewing).toEqual(mockViewing);
    });

    it('should update a viewing', async () => {
        const updateData = { notes: 'Updated notes' };
        (db.viewing.update as any).mockResolvedValue({ ...mockViewing, ...updateData });
        const updatedViewing = await viewingService.updateViewing('view1', updateData);
        expect(db.viewing.update).toHaveBeenCalledWith({ where: { id: 'view1' }, data: updateData });
        expect(updatedViewing.notes).toBe('Updated notes');
    });

    it('should delete a viewing', async () => {
        (db.viewing.delete as any).mockResolvedValue(mockViewing);
        await viewingService.deleteViewing('view1');
        expect(db.viewing.delete).toHaveBeenCalledWith({ where: { id: 'view1' } });
    });

    it('should update a viewing outcome', async () => {
        const outcomeData = { outcome: ViewingOutcome.INTERESTED, notes: 'Very interested' };
        (db.viewing.update as any).mockResolvedValue({ ...mockViewing, ...outcomeData, status: ViewingStatus.COMPLETED });

        const updatedViewing = await viewingService.updateViewingOutcome('view1', outcomeData.outcome, outcomeData.notes);

        expect(db.viewing.update).toHaveBeenCalledWith({
            where: { id: 'view1' },
            data: { ...outcomeData, status: ViewingStatus.COMPLETED },
        });
        expect(updatedViewing.outcome).toBe(ViewingOutcome.INTERESTED);
        expect(updatedViewing.status).toBe(ViewingStatus.COMPLETED);
    });

    it('should get calendar viewings', async () => {
        const start = new Date('2024-01-01');
        const end = new Date('2024-01-31');
        (db.viewing.findMany as any).mockResolvedValue([mockViewing]);

        const viewings = await viewingService.getCalendarViewings(start, end, 'agent1');

        expect(db.viewing.findMany).toHaveBeenCalledWith({
            where: {
                scheduledAt: { gte: start, lte: end },
                status: { notIn: ['CANCELLED', 'NO_SHOW'] },
                agentId: 'agent1',
            },
            orderBy: { scheduledAt: 'asc' },
            include: expect.any(Object),
        });
        expect(viewings).toEqual([mockViewing]);
    });
});

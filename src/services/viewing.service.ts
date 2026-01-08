import prisma from '@/lib/db';
import { Prisma, ViewingOutcome, ViewingStatus } from '@prisma/client';

export class ViewingService {
  async createViewing(data: {
    propertyId: string;
    agentId: string;
    tenantName: string;
    tenantPhone: string;
    tenantEmail?: string;
    scheduledAt: Date;
    notes?: string;
  }) {
    return prisma.viewing.create({
      data: {
        ...data,
        status: ViewingStatus.SCHEDULED,
      },
      include: {
        property: true,
        agent: { select: { id: true, fullName: true, email: true } },
      },
    });
  }

  async getViewingById(id: string) {
    return prisma.viewing.findUnique({
      where: { id },
      include: {
        property: true,
        agent: { select: { id: true, fullName: true, email: true } },
      },
    });
  }

  async updateViewing(id: string, data: Partial<{
    propertyId: string;
    agentId: string;
    tenantName: string;
    tenantPhone: string;
    tenantEmail: string;
    scheduledAt: Date;
    status: ViewingStatus;
    outcome: ViewingOutcome;
    notes: string;
  }>) {
    return prisma.viewing.update({
      where: { id },
      data: data,
      include: {
        property: true,
        agent: { select: { id: true, fullName: true, email: true } },
      },
    });
  }

  async updateViewingOutcome(id: string, outcome: ViewingOutcome, notes?: string) {
    return prisma.viewing.update({
      where: { id },
      data: {
        outcome,
        notes,
      },
      include: {
        property: true,
        agent: { select: { id: true, fullName: true, email: true } },
      },
    });
  }

  async deleteViewing(id: string) {
    return prisma.viewing.delete({
      where: { id },
    });
  }

  async searchViewings(params: {
    status?: ViewingStatus;
    propertyId?: string;
    agentId?: string;
    startDate?: Date;
    endDate?: Date;
    page?: number;
    limit?: number;
  }) {
    const { page = 1, limit = 20, startDate, endDate, ...filters } = params;

    const where: Prisma.ViewingWhereInput = {
      ...filters,
    };

    if (startDate && endDate) {
      where.scheduledAt = {
        gte: startDate,
        lte: endDate,
      };
    } else if (startDate) {
      where.scheduledAt = {
        gte: startDate,
      };
    } else if (endDate) {
      where.scheduledAt = {
        lte: endDate,
      };
    }

    const [viewings, total] = await Promise.all([
      prisma.viewing.findMany({
        where,
        skip: (page - 1) * limit,
        take: limit,
        include: {
          property: { select: { id: true, title: true, addressLine: true } },
          agent: { select: { id: true, fullName: true, agencyName: true } },
        },
        orderBy: { scheduledAt: 'desc' },
      }),
      prisma.viewing.count({ where }),
    ]);

    return {
      data: viewings,
      pagination: { page, limit, total, totalPages: Math.ceil(total / limit) },
    };
  }
}

export const viewingService = new ViewingService();

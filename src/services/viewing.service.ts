import { Prisma, Viewing, ViewingOutcome, ViewingStatus } from '@prisma/client';
import db from '@/lib/db';
import { add, sub } from 'date-fns';
import { searchViewingSchema } from '@/schemas/viewing.schema';
import { z } from 'zod';

async function createViewing(data: Prisma.ViewingUncheckedCreateInput): Promise<Viewing> {
  const { propertyId, agentId, scheduledAt } = data;

  // Conflict detection: Check if there's another viewing for the same property and agent within a 30-minute window
  const thirtyMinutesBefore = sub(new Date(scheduledAt), { minutes: 30 });
  const thirtyMinutesAfter = add(new Date(scheduledAt), { minutes: 30 });

  const conflictingViewing = await db.viewing.findFirst({
    where: {
      propertyId,
      agentId,
      scheduledAt: {
        gte: thirtyMinutesBefore,
        lte: thirtyMinutesAfter,
      },
      status: {
        notIn: ['CANCELLED', 'NO_SHOW'],
      }
    },
  });

  if (conflictingViewing) {
    throw new Error('A viewing for this property with this agent is already scheduled around this time.');
  }

  return db.viewing.create({ data });
}

type SearchParams = z.infer<typeof searchViewingSchema> & { agentId?: string; };

async function searchViewings(params: SearchParams): Promise<{ data: Viewing[], pagination: any }> {
  const { page = 1, limit = 10, status, date_from, date_to, agentId } = params;
  
  const where: Prisma.ViewingWhereInput = {};
  if (status) where.status = status;
  if (agentId) where.agentId = agentId;

  if (date_from || date_to) {
    where.scheduledAt = {};
    if (date_from) where.scheduledAt.gte = date_from;
    if (date_to) where.scheduledAt.lte = date_to;
  }

  const [data, total] = await db.$transaction([
    db.viewing.findMany({
      where,
      skip: (page - 1) * limit,
      take: limit,
      orderBy: { scheduledAt: 'desc' },
      include: {
        property: {
          select: {
            id: true,
            title: true,
            addressLine: true,
            buildingName: true,
            unit: true,
            areaName: true,
          }
        }
      }
    }),
    db.viewing.count({ where }),
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

async function getViewingById(id: string): Promise<Viewing | null> {
  return db.viewing.findUnique({
    where: { id },
    include: {
      property: {
        select: {
          id: true,
          title: true,
          addressLine: true,
          buildingName: true,
          unit: true,
          areaName: true,
        }
      }
    }
  });
}

async function updateViewing(id: string, data: Prisma.ViewingUpdateInput): Promise<Viewing> {
  return db.viewing.update({ where: { id }, data });
}

async function deleteViewing(id: string): Promise<Viewing> {
  return db.viewing.delete({ where: { id } });
}

async function updateViewingOutcome(
  id: string,
  outcome: ViewingOutcome | null,
  notes?: string,
  status: ViewingStatus = ViewingStatus.COMPLETED
): Promise<Viewing> {
  return db.viewing.update({
    where: { id },
    data: {
      outcome,
      notes,
      status,
    },
  });
}

async function getCalendarViewings(start: Date, end: Date, agentId?: string): Promise<Viewing[]> {
  const where: Prisma.ViewingWhereInput = {
    scheduledAt: {
      gte: start,
      lte: end,
    },
    status: {
      notIn: ['CANCELLED', 'NO_SHOW'],
    }
  };

  if (agentId) {
    where.agentId = agentId;
  }

  return db.viewing.findMany({
    where,
    orderBy: { scheduledAt: 'asc' },
    include: {
      property: {
        select: {
          id: true,
          title: true,
          buildingName: true,
          unit: true,
          areaName: true,
        }
      }
    }
  });
}


export const viewingService = {
  createViewing,
  searchViewings,
  getViewingById,
  updateViewing,
  deleteViewing,
  updateViewingOutcome,
  getCalendarViewings,
};

import {
  Contract,
  ContractStatus,
  Property,
  PropertyImage,
  PropertyStatus,
  PropertyType,
  RentFrequency,
  User,
  Viewing,
  ViewingOutcome,
  ViewingStatus,
} from '@prisma/client';
import { parse } from 'date-fns';

type PropertyOwner = Pick<User, 'fullName' | 'email' | 'phone'>;
type PropertyAgent = Pick<User, 'fullName' | 'email' | 'phone' | 'agencyName'>;

type PropertyWithRelations = Property & {
  images?: PropertyImage[];
  owner?: PropertyOwner | null;
  agent?: PropertyAgent | null;
  _count?: { viewings: number };
};

type ViewingWithProperty = Viewing & {
  property?: Pick<Property, 'title' | 'buildingName' | 'unit' | 'areaName' | 'addressLine'> | null;
};

type ContractWithProperty = Contract & {
  property?: Pick<Property, 'title' | 'buildingName' | 'unit' | 'areaName'> | null;
};

const propertyTypeToFrontend: Record<PropertyType, string> = {
  APARTMENT: 'apartment',
  VILLA: 'villa',
  TOWNHOUSE: 'townhouse',
  PENTHOUSE: 'penthouse',
  STUDIO: 'studio',
  DUPLEX: 'apartment',
  OFFICE: 'apartment',
  RETAIL: 'apartment',
  WAREHOUSE: 'apartment',
};

const propertyStatusToFrontend: Record<PropertyStatus, 'available' | 'reserved' | 'rented'> = {
  AVAILABLE: 'available',
  RESERVED: 'reserved',
  RENTED: 'rented',
  DRAFT: 'available',
  MAINTENANCE: 'reserved',
  UNLISTED: 'reserved',
};

const contractStatusToFrontend: Record<ContractStatus, 'draft' | 'pending_signature' | 'signed' | 'expired' | 'cancelled'> = {
  DRAFT: 'draft',
  PENDING_SIGNATURE: 'pending_signature',
  SIGNED: 'signed',
  ACTIVE: 'signed',
  EXPIRED: 'expired',
  TERMINATED: 'cancelled',
};

const viewingOutcomeToFrontend: Record<ViewingOutcome, 'interested' | 'not_interested' | 'offer_made' | 'pending'> = {
  INTERESTED: 'interested',
  NOT_INTERESTED: 'not_interested',
  APPLICATION_SUBMITTED: 'offer_made',
  FOLLOW_UP: 'pending',
  PENDING: 'pending',
};

const propertyTypeFromFrontend: Record<string, PropertyType> = {
  apartment: PropertyType.APARTMENT,
  villa: PropertyType.VILLA,
  townhouse: PropertyType.TOWNHOUSE,
  penthouse: PropertyType.PENTHOUSE,
  studio: PropertyType.STUDIO,
};

const propertyStatusFromFrontend: Record<string, PropertyStatus> = {
  available: PropertyStatus.AVAILABLE,
  reserved: PropertyStatus.RESERVED,
  rented: PropertyStatus.RENTED,
};

const viewingOutcomeFromFrontend: Record<string, ViewingOutcome> = {
  interested: ViewingOutcome.INTERESTED,
  not_interested: ViewingOutcome.NOT_INTERESTED,
  offer_made: ViewingOutcome.APPLICATION_SUBMITTED,
  pending: ViewingOutcome.PENDING,
};

function coerceNumber(value: unknown): number | undefined {
  if (value === undefined || value === null || value === '') return undefined;
  const num = Number(value);
  return Number.isNaN(num) ? undefined : num;
}

function coerceString(value: unknown): string | undefined {
  if (value === undefined || value === null) return undefined;
  const str = String(value).trim();
  return str.length ? str : undefined;
}

function normalizeEnumValue<T>(enumValues: Record<string, string>, value: unknown): T | undefined {
  if (!value) return undefined;
  if (typeof value === 'string') {
    const upper = value.toUpperCase();
    if (upper in enumValues) return upper as unknown as T;
    const lower = value.toLowerCase();
    if (lower in propertyTypeFromFrontend) return propertyTypeFromFrontend[lower] as unknown as T;
    if (lower in propertyStatusFromFrontend) return propertyStatusFromFrontend[lower] as unknown as T;
    if (lower === 'yearly') return RentFrequency.YEARLY as unknown as T;
    if (lower === 'monthly') return RentFrequency.MONTHLY as unknown as T;
    if (lower === 'quarterly') return RentFrequency.QUARTERLY as unknown as T;
  }
  return value as T;
}

function buildTitle(buildingName?: string, unit?: string): string | undefined {
  if (buildingName && unit) return `${buildingName} - Unit ${unit}`;
  return buildingName || undefined;
}

function buildAddressLine(buildingName?: string, unit?: string, areaName?: string): string | undefined {
  const parts = [buildingName, unit ? `Unit ${unit}` : undefined, areaName].filter(Boolean);
  return parts.length ? parts.join(', ') : undefined;
}

function formatDateOnly(date: Date | string | null | undefined): string {
  if (!date) return '';
  const dt = date instanceof Date ? date : new Date(date);
  const year = dt.getFullYear();
  const month = String(dt.getMonth() + 1).padStart(2, '0');
  const day = String(dt.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatTime(date: Date | string | null | undefined): string {
  if (!date) return '';
  const dt = date instanceof Date ? date : new Date(date);
  return new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  }).format(dt);
}

export function combineDateAndTime(date: string, time: string): Date {
  return parse(`${date} ${time}`, 'yyyy-MM-dd h:mm a', new Date());
}

export function normalizeUserInput(input: Record<string, any>) {
  const { firstName, lastName, reraBrokerId, ...rest } = input;
  const computedFullName = coerceString(rest.fullName) || [firstName, lastName].filter(Boolean).join(' ').trim();
  const fullName = coerceString(computedFullName);
  const reraLicenseNumber = rest.reraLicenseNumber || reraBrokerId;

  return {
    ...rest,
    ...(fullName ? { fullName } : {}),
    ...(reraLicenseNumber ? { reraLicenseNumber } : {}),
  };
}

export function normalizePropertyInput(input: Record<string, any>) {
  const buildingName = coerceString(input.buildingName) || coerceString(input.building);
  const unit = coerceString(input.unit);
  const areaName = coerceString(input.areaName) || coerceString(input.area);
  const title = coerceString(input.title) || buildTitle(buildingName, unit);
  const addressLine = coerceString(input.addressLine) || buildAddressLine(buildingName, unit, areaName);

  return {
    title,
    description: input.description,
    type: normalizeEnumValue<PropertyType>(PropertyType, input.type),
    status: normalizeEnumValue<PropertyStatus>(PropertyStatus, input.status),
    bedrooms: coerceNumber(input.bedrooms ?? input.beds),
    bathrooms: coerceNumber(input.bathrooms ?? input.baths),
    areaSqft: coerceNumber(input.areaSqft ?? input.sqft),
    rentAmount: coerceNumber(input.rentAmount ?? input.rent),
    rentFrequency: normalizeEnumValue<RentFrequency>(RentFrequency, input.rentFrequency),
    depositAmount: coerceNumber(input.depositAmount ?? input.deposit),
    numberOfCheques: coerceNumber(input.numberOfCheques ?? input.cheques),
    addressLine,
    buildingName,
    unit,
    areaName,
    city: input.city,
    latitude: coerceNumber(input.latitude),
    longitude: coerceNumber(input.longitude),
    amenities: input.amenities,
    ownerId: input.ownerId,
  };
}

export function normalizeContractInput(input: Record<string, any>) {
  const rentAmount = coerceNumber(input.rentAmount ?? input.rent);
  const depositAmount = coerceNumber(input.depositAmount ?? input.deposit);
  const numberOfCheques = coerceNumber(input.numberOfCheques ?? input.cheques);
  const status =
    typeof input.status === 'string'
      ? (ContractStatus[input.status.toUpperCase() as keyof typeof ContractStatus] ?? input.status)
      : input.status;

  return {
    ...input,
    ...(rentAmount !== undefined ? { rentAmount } : {}),
    ...(depositAmount !== undefined ? { depositAmount } : {}),
    ...(numberOfCheques !== undefined ? { numberOfCheques } : {}),
    ...(status ? { status } : {}),
  };
}

export function normalizeViewingOutcomeInput(outcome: string | ViewingOutcome) {
  if (typeof outcome !== 'string') {
    return { outcome, status: ViewingStatus.COMPLETED };
  }

  const lower = outcome.toLowerCase();
  if (lower === 'no_show') {
    return { outcome: null, status: ViewingStatus.NO_SHOW };
  }

  if (lower in viewingOutcomeFromFrontend) {
    return { outcome: viewingOutcomeFromFrontend[lower], status: ViewingStatus.COMPLETED };
  }

  return { outcome: outcome as ViewingOutcome, status: ViewingStatus.COMPLETED };
}

export function toFrontendProperty(property: PropertyWithRelations) {
  const buildingName = property.buildingName ?? '';
  const unit = property.unit ?? '';
  const areaName = property.areaName ?? '';
  const images = (property.images || [])
    .slice()
    .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
    .map((image) => image.url);

  return {
    id: property.id,
    title: property.title,
    building: buildingName,
    unit,
    area: areaName,
    type: propertyTypeToFrontend[property.type] || 'apartment',
    beds: property.bedrooms ?? 0,
    baths: property.bathrooms ?? 0,
    sqft: coerceNumber(property.areaSqft) ?? 0,
    rent: coerceNumber(property.rentAmount) ?? 0,
    cheques: property.numberOfCheques ?? 1,
    deposit: coerceNumber(property.depositAmount) ?? 0,
    status: propertyStatusToFrontend[property.status] || 'available',
    images,
    owner: {
      name: property.owner?.fullName || '',
      email: property.owner?.email || '',
      phone: property.owner?.phone || '',
    },
    agent: property.agent
      ? {
          name: property.agent.fullName,
          email: property.agent.email,
          phone: property.agent.phone,
          company: property.agent.agencyName || '',
          rating: 0,
        }
      : undefined,
    viewingsCount: property._count?.viewings ?? 0,
    createdAt: formatDateOnly(property.createdAt),
  };
}

export function toFrontendViewing(viewing: ViewingWithProperty) {
  const propertyTitle =
    viewing.property?.title ||
    buildTitle(viewing.property?.buildingName, viewing.property?.unit) ||
    '';

  return {
    id: viewing.id,
    propertyId: viewing.propertyId,
    propertyTitle,
    propertyArea: viewing.property?.areaName || viewing.property?.addressLine || '',
    tenantName: viewing.tenantName,
    tenantPhone: viewing.tenantPhone,
    date: formatDateOnly(viewing.scheduledAt),
    time: formatTime(viewing.scheduledAt),
    outcome:
      viewing.status === ViewingStatus.NO_SHOW
        ? 'no_show'
        : viewing.outcome
          ? viewingOutcomeToFrontend[viewing.outcome]
          : undefined,
    notes: viewing.notes || undefined,
  };
}

export function toFrontendContract(contract: ContractWithProperty) {
  const propertyTitle =
    contract.property?.title ||
    buildTitle(contract.property?.buildingName, contract.property?.unit) ||
    '';

  return {
    id: contract.id,
    propertyId: contract.propertyId,
    propertyTitle,
    tenantName: contract.tenantName,
    tenantEmail: contract.tenantEmail,
    tenantPhone: contract.tenantPhone,
    startDate: contract.startDate?.toISOString?.() ?? String(contract.startDate),
    endDate: contract.endDate?.toISOString?.() ?? String(contract.endDate),
    rent: coerceNumber(contract.rentAmount) ?? 0,
    cheques: contract.numberOfCheques ?? 1,
    deposit: coerceNumber(contract.depositAmount) ?? 0,
    status: contractStatusToFrontend[contract.status] || 'draft',
    signedAt: contract.signedAt?.toISOString?.() ?? undefined,
    pdfUrl: contract.signedPdfUrl || contract.pdfUrl || undefined,
    createdAt: contract.createdAt?.toISOString?.() ?? String(contract.createdAt),
  };
}

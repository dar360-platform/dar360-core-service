import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { contractService } from '@/services/contract.service';

// POST /api/contracts/[id]/generate-pdf - Generate PDF
export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;

    const existingContract = await contractService.getContractById(id);
    if (!existingContract) {
      return NextResponse.json({ error: 'Contract not found' }, { status: 404 });
    }

    // Only the agent who created the contract or an admin can generate PDF
    if (existingContract.agentId !== session.user.id && session.user.role !== 'ADMIN') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const { pdfUrl } = await contractService.generatePdf(id);

    return NextResponse.json({ data: { pdfUrl } });
  } catch (error) {
    console.error('POST /api/contracts/[id]/generate-pdf error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
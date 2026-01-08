import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { contractService } from '@/services/contract.service';

// GET /api/contracts/[id]/download - Download PDF
export async function GET(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const contract = await contractService.getContractById(id);

    if (!contract) {
      return NextResponse.json({ error: 'Contract not found' }, { status: 404 });
    }

    if (!contract.pdfUrl) {
      return NextResponse.json({ error: 'Contract PDF not generated yet' }, { status: 404 });
    }

    // Agent, Owner, or Tenant (if authenticated to the contract via other means) can download
    // For now, restrict to agent/owner. Public download will be separate.
    if (contract.agentId !== session.user.id && contract.ownerId !== session.user.id && session.user.role !== 'ADMIN') {
        return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    // Redirect to the PDF URL (e.g., S3 pre-signed URL)
    return NextResponse.redirect(contract.pdfUrl);
  } catch (error) {
    console.error('GET /api/contracts/[id]/download error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
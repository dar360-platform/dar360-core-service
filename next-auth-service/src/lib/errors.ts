export class HttpError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "HttpError";
  }
}

export function toErrorResponse(err: unknown) {
  if (err instanceof HttpError) {
    return { status: err.status, body: { error: err.message } };
  }
  if (err instanceof Error) {
    return { status: 500, body: { error: err.message } };
  }
  return { status: 500, body: { error: "Internal Server Error" } };
}

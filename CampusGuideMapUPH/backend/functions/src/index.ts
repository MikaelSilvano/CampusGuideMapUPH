import { onRequest } from "firebase-functions/v2/https";
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getDatabase } from "firebase-admin/database";
import type { Request, Response } from "express";
import { z } from "zod";

initializeApp();
const db = getDatabase();

function toDateKeyUTC(ms: number): string {
  const d = new Date(ms);
  const y = d.getUTCFullYear();
  const m = String(d.getUTCMonth() + 1).padStart(2, "0");
  const day = String(d.getUTCDate()).padStart(2, "0");
  return `${y}${m}${day}`;
}

const EventSchema = z.object({
  name: z.string().min(1),
  building: z.string().default(""),
  floor: z.number().int().optional(),
  room: z.string().default(""),
  heldBy: z.string().default(""),
  date: z.number().int().nonnegative(),                // epoch millis (UTC)
  startTimeMinutes: z.number().int().nonnegative().default(0),
  endTimeMinutes: z.number().int().nonnegative().default(0),
  posterUrl: z.string().default(""),
  published: z.boolean().default(false),
});

type EventRow = {
  id: string;
  name?: string;
  building?: string;
  floor?: number;
  room?: string;
  heldBy?: string;
  date: number;              
  startTimeMinutes?: number;
  endTimeMinutes?: number;
  posterUrl?: string;
  published?: boolean;
  createdAt?: number;
  updatedAt?: number;
  createdBy?: string;
};

function getIdFromReq(req: Request): string | null {
  const path = (req.path || "") as string;
  const segs = path.split("/").filter(Boolean);
  const tail = segs.length >= 1 ? segs[segs.length - 1] : null;
  const fromQuery = (req.query?.id as string) || null;
  return tail || fromQuery;
}

async function verify(req: Request, res: Response, role?: "admin") {
  const authz = (req.headers.authorization || "") as string;
  const token = authz.startsWith("Bearer ") ? authz.slice(7) : null;
  if (!token) {
    res.status(401).json({ error: "missing bearer token" });
    return null;
  }
  const decoded = await getAuth().verifyIdToken(token).catch(() => null);
  if (!decoded) {
    res.status(401).json({ error: "invalid token" });
    return null;
  }
  if (role === "admin" && (decoded as any).role !== "admin") {
    res.status(403).json({ error: "forbidden" });
    return null;
  }
  return decoded;
}

// GET Event
export const listEvents = onRequest(
  { region: "asia-southeast1", cors: true },
  async (_req: Request, res: Response): Promise<void> => {
    try {
      const snap = await db.ref("events").get();
      const raw = (snap.val() ?? {}) as Record<string, unknown>;
      const now = Date.now();

      const items: EventRow[] = Object.entries(raw)
        .filter(([, v]) => (v as any)?.published === true)
        .map(([id, v]) => ({ id, ...(v as Record<string, any>) } as EventRow))
        .filter((e) => {
          const endMs = Number(e.date ?? 0) + Number(e.endTimeMinutes ?? 0) * 60_000;
          return endMs >= now;
        })
        .sort((a, b) =>
          a.date === b.date
            ? (a.startTimeMinutes ?? 0) - (b.startTimeMinutes ?? 0)
            : a.date - b.date
        );

      res.json(items);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : String(e);
      res.status(500).json({ error: msg });
    }
  }
);

export const listAllEvents = onRequest(
  { region: "asia-southeast1", cors: true },
  async (req: Request, res: Response): Promise<void> => {
    const decoded = await verify(req, res, "admin");
    if (!decoded) return;

    try {
      const snap = await db.ref("events").get();
      const raw = (snap.val() ?? {}) as Record<string, unknown>;

      const items: EventRow[] = Object.entries(raw)
        .map(([id, v]) => ({ id, ...(v as Record<string, any>) } as EventRow))
        .sort((a, b) =>
          a.date === b.date
            ? (a.startTimeMinutes ?? 0) - (b.startTimeMinutes ?? 0)
            : a.date - b.date
        );

      res.json(items);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : String(e);
      res.status(500).json({ error: msg });
    }
  }
);

// POST Event
export const createEvent = onRequest(
  { region: "asia-southeast1", cors: true },
  async (req: Request, res: Response): Promise<void> => {
    if (req.method !== "POST") { res.status(405).end(); return; }
    const decoded = await verify(req, res, "admin"); if (!decoded) return;

    const body: any = { ...(req.body || {}) };
    if (body.title && !body.name) body.name = body.title;

    const parsed = EventSchema.safeParse(body);
    if (!parsed.success) {
      res.status(400).json({ error: parsed.error.flatten() });
      return;
    }

    const now = Date.now();
    const evt = { ...parsed.data, createdAt: now, updatedAt: now, createdBy: decoded.uid };
    const ref = await db.ref("events").push(evt);

    const dateKey = toDateKeyUTC(evt.date);
    await db.ref(`eventsByDate/${dateKey}/${ref.key}`).set(true);

    res.json({ id: ref.key!, ...evt });
    return;
  }
);

// PATCH Event
export const updateEvent = onRequest(
  { region: "asia-southeast1", cors: true },
  async (req: Request, res: Response): Promise<void> => {
    if (req.method !== "PATCH") { res.status(405).end(); return; }
    const decoded = await verify(req, res, "admin"); if (!decoded) return;

    const id = getIdFromReq(req);
    if (!id) { res.status(400).json({ error: "missing id" }); return; }

    const beforeSnap = await db.ref(`events/${id}`).get();
    const before = beforeSnap.val();
    if (!before) { res.status(404).json({ error: "not found" }); return; }

    const body: any = { ...(req.body || {}) };
    if (body.title && !body.name) body.name = body.title;

    const toValidate = { ...before, ...body };
    const parsed = EventSchema.safeParse(toValidate);
    if (!parsed.success) { res.status(400).json({ error: parsed.error.flatten() }); return; }

    const after = { ...parsed.data, createdAt: before.createdAt, createdBy: before.createdBy, updatedAt: Date.now() };
    await db.ref(`events/${id}`).set(after);

    if (after.date !== before.date) {
      const oldKey = toDateKeyUTC(before.date);
      const newKey = toDateKeyUTC(after.date);
      await db.ref(`eventsByDate/${oldKey}/${id}`).remove();
      await db.ref(`eventsByDate/${newKey}/${id}`).set(true);
    }

    res.json({ id, ...after });
    return;
  }
);

// DELETE Event
export const deleteEvent = onRequest(
  { region: "asia-southeast1", cors: true },
  async (req: Request, res: Response): Promise<void> => {
    if (req.method !== "DELETE") { res.status(405).end(); return; }
    const decoded = await verify(req, res, "admin"); if (!decoded) return;

    const id = getIdFromReq(req);
    if (!id) { res.status(400).json({ error: "missing id" }); return; }

    const snap = await db.ref(`events/${id}`).get();
    const evt = snap.val();
    if (!evt) { res.status(404).json({ error: "not found" }); return; }

    const dateKey = toDateKeyUTC(evt.date);
    await db.ref(`events/${id}`).remove();
    await db.ref(`eventsByDate/${dateKey}/${id}`).remove();

    res.json({ ok: true, id });
    return;
  }
);

export const grantAdmin = onRequest(
  { region: "asia-southeast1", cors: true },
  async (req, res) => {
    if (req.method !== "POST") { res.status(405).end(); return; }

    const decoded = await verify(req, res, "admin");
    if (!decoded) return;

    const email = (req.body?.email ?? "").toString().trim();
    if (!email) { res.status(400).json({ error: "missing email" }); return; }

    const user = await getAuth().getUserByEmail(email);
    const existing = user.customClaims ?? {};
    await getAuth().setCustomUserClaims(user.uid, { ...existing, role: "admin" });

    res.json({ ok: true, uid: user.uid, role: "admin" });
  }
);

export const revokeAdmin = onRequest(
  { region: "asia-southeast1", cors: true },
  async (req, res) => {
    if (req.method !== "POST") { res.status(405).end(); return; }

    const decoded = await verify(req, res, "admin");
    if (!decoded) return;

    const email = (req.body?.email ?? "").toString().trim();
    if (!email) { res.status(400).json({ error: "missing email" }); return; }

    const user = await getAuth().getUserByEmail(email);
    const existing = user.customClaims ?? {};
    const { role, ...rest } = existing;
    await getAuth().setCustomUserClaims(user.uid, { ...rest });

    res.json({ ok: true, uid: user.uid, role: null });
  }
);

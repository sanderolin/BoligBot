import type { Route } from "./+types/index";
import { Link } from "react-router";
import { useTranslation } from "react-i18next";
import { HousingCard } from "~/housings/HousingItemCard";
import type { HousingDTO, PagedResponse } from "~/types/api";
import { HousingFiltersSidebar } from "~/housings/HousingFiltersSidebar";

const PAGE_SIZE = 20;
const PAGE_WINDOW = 7;

const ALLOWED_QUERY_KEYS = [
    "rentalObjectId",
    "address",
    "name",
    "housingType",
    "city",
    "district",
    "minPricePerMonth",
    "maxPricePerMonth",
    "minAreaSqm",
    "maxAreaSqm",
    "sortBy",
    "sortDirection",
] as const;

function toInt(value: string | null, fallback: number) {
    if (value == null) return fallback;
    const n = Number(value);
    return Number.isFinite(n) ? Math.trunc(n) : fallback;
}

function getPageWindow(current: number, pageCount: number, windowSize: number) {
    const safeCount = Math.max(1, pageCount);
    const safeCurrent = Math.min(Math.max(0, current), safeCount - 1);

    const size = Math.max(1, windowSize);
    const half = Math.floor(size / 2);

    const start = Math.max(0, Math.min(safeCurrent - half, safeCount - size));
    const end = Math.min(safeCount - 1, start + size - 1);

    return { current: safeCurrent, start, end, pageCount: safeCount };
}

export async function loader({ request }: Route.LoaderArgs) {
    const url = new URL(request.url);

    const page = Math.max(0, toInt(url.searchParams.get("page"), 0));

    const backendUrl = new URL("http://localhost:8080/api/v1/housings");
    backendUrl.searchParams.set("page", String(page));
    backendUrl.searchParams.set("size", String(PAGE_SIZE));

    for (const key of ALLOWED_QUERY_KEYS) {
        const value = url.searchParams.get(key);
        if (value != null && value.trim() !== "") {
            backendUrl.searchParams.set(key, value);
        }
    }

    const res = await fetch(backendUrl.toString(), {
        headers: { Accept: "application/json" },
    });

    if (!res.ok) {
        const contentType = res.headers.get("content-type") ?? "";
        const body = contentType.includes("application/problem+json")
            ? await res.json()
            : await res.text();

        throw new Response(typeof body === "string" ? body : JSON.stringify(body), {
            status: res.status,
            headers: { "content-type": "application/problem+json" },
        });
    }

    const data = (await res.json()) as PagedResponse<HousingDTO>;
    return { data };
}

export function meta(_: Route.MetaArgs) {
    return [{ title: "BoligBot — Housings" }];
}

export default function Housings({ loaderData }: Route.ComponentProps) {
    const { t } = useTranslation();
    const { data } = loaderData;

    const pageCount = Math.max(1, Math.ceil(data.total / data.size));
    const { current, start, end } = getPageWindow(data.page, pageCount, PAGE_WINDOW);

    return (
        <section className="w-full">
            <div className="mx-auto w-full max-w-6xl px-4 py-6">
                <header className="mb-6">
                    <h2 className="text-xl font-semibold text-slate-900 dark:text-slate-50">
                        {t("housings.title", { defaultValue: "Housings" })}
                    </h2>
                    <p className="mt-1 text-sm text-slate-600 dark:text-slate-300">
                        {t("housings.results", { page: current + 1, pages: pageCount, total: data.total })}
                    </p>
                </header>

                {/* Sidebar + content */}
                <div className="flex flex-col gap-6 lg:flex-row">
                    <HousingFiltersSidebar />

                    <div className="min-w-0 flex-1">
                        {data.items.length === 0 ? (
                            <div className="rounded-2xl border border-slate-200/70 bg-white/70 p-6 text-slate-700 shadow-sm dark:border-slate-800/70 dark:bg-slate-950/40 dark:text-slate-200">
                                No results.
                            </div>
                        ) : (
                            <div className="grid grid-cols-1 gap-4">
                                {data.items.map((h) => (
                                    <HousingCard key={h.rentalObjectId} h={h} />
                                ))}
                            </div>
                        )}

                        <nav aria-label="Pagination" className="mt-10 flex items-center justify-center gap-2">
                            {/* Prev */}
                            {current > 0 ? (
                                <Link
                                    to={`?page=${current - 1}`}
                                    rel="prev"
                                    className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-950 dark:hover:bg-slate-900"
                                >
                                    Prev
                                </Link>
                            ) : (
                                <span className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm opacity-50 dark:border-slate-800 dark:bg-slate-950">
                  Prev
                </span>
                            )}

                            {/* Left truncation */}
                            {start > 0 && (
                                <>
                                    <Link
                                        to="?page=0"
                                        className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-950 dark:hover:bg-slate-900"
                                    >
                                        1
                                    </Link>
                                    <span className="px-1 text-slate-500">…</span>
                                </>
                            )}

                            {/* Page numbers */}
                            {Array.from({ length: end - start + 1 }, (_, i) => start + i).map((p) =>
                                    p === current ? (
                                        <span
                                            key={p}
                                            className="rounded-xl border border-slate-200 bg-slate-900 px-3 py-1.5 text-sm text-white dark:border-slate-800 dark:bg-slate-50 dark:text-slate-950"
                                        >
                    {p + 1}
                  </span>
                                    ) : (
                                        <Link
                                            key={p}
                                            to={`?page=${p}`}
                                            className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-950 dark:hover:bg-slate-900"
                                        >
                                            {p + 1}
                                        </Link>
                                    )
                            )}

                            {/* Right truncation */}
                            {end < pageCount - 1 && (
                                <>
                                    <span className="px-1 text-slate-500">…</span>
                                    <Link
                                        to={`?page=${pageCount - 1}`}
                                        className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-950 dark:hover:bg-slate-900"
                                    >
                                        {pageCount}
                                    </Link>
                                </>
                            )}

                            {/* Next */}
                            {data.hasNext ? (
                                <Link
                                    to={`?page=${current + 1}`}
                                    rel="next"
                                    className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-950 dark:hover:bg-slate-900"
                                >
                                    Next
                                </Link>
                            ) : (
                                <span className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-sm opacity-50 dark:border-slate-800 dark:bg-slate-950">
                  Next
                </span>
                            )}
                        </nav>
                    </div>
                </div>
            </div>
        </section>
    );
}

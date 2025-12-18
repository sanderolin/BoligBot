import { useMemo } from "react";
import { useSearchParams } from "react-router";
import { useTranslation } from "react-i18next";

type Option = { value: string; label: string };

function toStr(v: string | null) {
    return v ?? "";
}

function setOrDelete(params: URLSearchParams, key: string, value: string) {
    const trimmed = value.trim();
    if (!trimmed) params.delete(key);
    else params.set(key, trimmed);
}

function setOrDeleteNumber(params: URLSearchParams, key: string, value: string) {
    const trimmed = value.trim();
    if (!trimmed) {
        params.delete(key);
        return;
    }
    const n = Number(trimmed);
    if (!Number.isFinite(n)) return;
    params.set(key, String(Math.trunc(n)));
}

export function HousingFiltersSidebar() {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();

    const values = useMemo(() => {
        return {
            city: toStr(searchParams.get("city")),
            district: toStr(searchParams.get("district")),
            housingType: toStr(searchParams.get("housingType")),
            minPricePerMonth: toStr(searchParams.get("minPricePerMonth")),
            maxPricePerMonth: toStr(searchParams.get("maxPricePerMonth")),
            minAreaSqm: toStr(searchParams.get("minAreaSqm")),
            maxAreaSqm: toStr(searchParams.get("maxAreaSqm")),
            sortBy: toStr(searchParams.get("sortBy")) || "availableFromDate",
            sortDirection: toStr(searchParams.get("sortDirection")) || "asc",
        };
    }, [searchParams]);

    const sortByOptions: Option[] = [
        { value: "availableFromDate", label: "Available from" },
        { value: "pricePerMonth", label: "Price" },
        { value: "areaSqm", label: "Area" },
        { value: "city", label: "City" },
        { value: "district", label: "District" },
    ];

    function update(updater: (p: URLSearchParams) => void) {
        const next = new URLSearchParams(searchParams);
        updater(next);
        next.delete("page");
        setSearchParams(next);
    }

    function resetAll() {
        setSearchParams(new URLSearchParams());
    }

    return (
        <aside className="w-full lg:w-80 shrink-0">
            <div className="sticky top-16 rounded-2xl border border-slate-200/70
            bg-white/70 p-4 shadow-sm backdrop-blur dark:border-slate-800/70 dark:bg-slate-950/40">
                <div className="flex items-center justify-between">
                    <h3 className="text-base font-semibold text-slate-900 dark:text-slate-50">
                        {t("housings.filters.title", { defaultValue: "Filters" })}
                    </h3>
                    <button
                        type="button"
                        onClick={resetAll}
                        className="text-sm text-slate-600 hover:text-slate-900 dark:text-slate-300
                        dark:hover:text-slate-50"
                    >
                        {t("housings.filters.reset", { defaultValue: "Reset" })}
                    </button>
                </div>

                <div className="mt-4 space-y-4">
                    {/* City */}
                    <div>
                        <label className="block text-xs text-slate-500 dark:text-slate-400">
                            {t("housings.filters.city", { defaultValue: "City" })}
                        </label>
                        <input
                            value={values.city}
                            onChange={(e) =>
                                update((p) => setOrDelete(p, "city", e.target.value))}
                            placeholder="e.g. Trondheim"
                            className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                            dark:border-slate-800 dark:bg-slate-950"
                        />
                    </div>

                    {/* District */}
                    <div>
                        <label className="block text-xs text-slate-500 dark:text-slate-400">
                            {t("housings.filters.district", { defaultValue: "District" })}
                        </label>
                        <input
                            value={values.district}
                            onChange={(e) =>
                                update((p) => setOrDelete(p, "district", e.target.value))}
                            placeholder="e.g. Moholt"
                            className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                            dark:border-slate-800 dark:bg-slate-950"
                        />
                    </div>

                    {/* Housing type */}
                    <div>
                        <label className="block text-xs text-slate-500 dark:text-slate-400">
                            {t("housings.filters.housingType", { defaultValue: "Housing type" })}
                        </label>
                        <input
                            value={values.housingType}
                            onChange={(e) =>
                                update((p) => setOrDelete(p, "housingType", e.target.value))}
                            placeholder="e.g. Dorm"
                            className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                            dark:border-slate-800 dark:bg-slate-950"
                        />
                    </div>

                    {/* Price range */}
                    <div>
                        <div className="flex items-baseline justify-between">
                            <label className="block text-xs text-slate-500 dark:text-slate-400">
                                {t("housings.filters.price", { defaultValue: "Price / month" })}
                            </label>
                        </div>
                        <div className="mt-1 grid grid-cols-2 gap-2">
                            <input
                                inputMode="numeric"
                                value={values.minPricePerMonth}
                                onChange={(e) =>
                                    update((p) => setOrDeleteNumber(p, "minPricePerMonth", e.target.value))}
                                placeholder={t("housings.filters.min", { defaultValue: "Min" })}
                                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                                dark:border-slate-800 dark:bg-slate-950"
                            />
                            <input
                                inputMode="numeric"
                                value={values.maxPricePerMonth}
                                onChange={(e) =>
                                    update((p) => setOrDeleteNumber(p, "maxPricePerMonth", e.target.value))}
                                placeholder={t("housings.filters.max", { defaultValue: "Max" })}
                                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                                dark:border-slate-800 dark:bg-slate-950"
                            />
                        </div>
                    </div>

                    {/* Area range */}
                    <div>
                        <label className="block text-xs text-slate-500 dark:text-slate-400">
                            {t("housings.filters.area", { defaultValue: "Area (m²)" })}
                        </label>
                        <div className="mt-1 grid grid-cols-2 gap-2">
                            <input
                                inputMode="decimal"
                                value={values.minAreaSqm}
                                onChange={(e) =>
                                    update((p) => setOrDelete(p, "minAreaSqm", e.target.value))}
                                placeholder={t("housings.filters.min", { defaultValue: "Min" })}
                                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                                dark:border-slate-800 dark:bg-slate-950"
                            />
                            <input
                                inputMode="decimal"
                                value={values.maxAreaSqm}
                                onChange={(e) =>
                                    update((p) => setOrDelete(p, "maxAreaSqm", e.target.value))}
                                placeholder={t("housings.filters.max", { defaultValue: "Max" })}
                                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                                dark:border-slate-800 dark:bg-slate-950"
                            />
                        </div>
                    </div>

                    {/* Sorting */}
                    <div>
                        <label className="block text-xs text-slate-500 dark:text-slate-400">
                            {t("housings.filters.sorting", { defaultValue: "Sorting" })}
                        </label>

                        <div className="mt-1 grid grid-cols-2 gap-2">
                            <select
                                value={values.sortBy}
                                onChange={(e) =>
                                    update((p) => setOrDelete(p, "sortBy", e.target.value))}
                                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                                dark:border-slate-800 dark:bg-slate-950"
                            >
                                {sortByOptions.map((o) => (
                                    <option key={o.value} value={o.value}>
                                        {o.label}
                                    </option>
                                ))}
                            </select>

                            <select
                                value={values.sortDirection}
                                onChange={(e) =>
                                    update((p) => setOrDelete(p, "sortDirection", e.target.value))}
                                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm
                                dark:border-slate-800 dark:bg-slate-950"
                            >
                                <option value="asc">{t("common.asc", { defaultValue: "Asc" })}</option>
                                <option value="desc">{t("common.desc", { defaultValue: "Desc" })}</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
        </aside>
    );
}

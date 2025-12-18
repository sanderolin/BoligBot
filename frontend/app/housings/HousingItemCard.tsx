import { Link } from "react-router";
import {useTranslation} from "react-i18next";
import type { HousingDTO } from "~/types/api";

function formatPrice(value: number) {
    return new Intl.NumberFormat("nb-NO").format(value);
}

function formatDate(date: string) {
    return date;
}

export function HousingCard({ h }: { h: HousingDTO }) {
    const id = encodeURIComponent(h.rentalObjectId);
    const showAvailableBanner = h.isAvailable && Boolean(h.availableFromDate);

    const { t } = useTranslation();

    const imageUrl = "https://picsum.photos/seed/boligbot/600/400";

    return (
        <Link
            to={`https://bolig.sit.no/en/unit/${id}`}
            target="_blank"
            className="group block rounded-2xl focus:outline-none focus:ring-2 focus:ring-slate-400/50"
        >
            <article
                className="
                    relative mx-auto max-w-4xl overflow-hidden rounded-2xl
                    border border-slate-200/70 bg-white/70
                    shadow-sm backdrop-blur
                    transition-all duration-200
                    group-hover:shadow-md
                    group-hover:bg-white
                    group-hover:border-slate-300
                    group-hover:scale-[1.01]
                    dark:border-slate-800/70 dark:bg-slate-950/40
                    dark:group-hover:bg-slate-950/70
                    dark:group-hover:border-slate-700
                "
            >
                <div className="flex">
                    <div className="relative w-[28%] min-w-[120px] overflow-hidden">
                        <img
                            src={imageUrl}
                            alt=""
                            className="
                                h-full w-full object-cover
                                transition-transform duration-300
                                group-hover:scale-105
                            "
                            loading="lazy"
                        />
                        <div className="pointer-events-none absolute inset-0 bg-gradient-to-t
                        from-black/20 via-transparent to-transparent" />
                    </div>

                    <div className="flex min-w-0 flex-1 flex-col p-5">
                        <div className="flex items-start justify-between gap-3">
                            <div className="min-w-0">
                                <h3 className="text-base font-semibold leading-6 text-slate-900 dark:text-slate-50 truncate">
                                    {h.address}, {h.name}
                                </h3>

                                <p className="mt-1 text-sm text-slate-600 dark:text-slate-300 truncate">
                                    {h.housingType}, {h.district}, {h.city}
                                </p>
                            </div>

                            {showAvailableBanner && (
                                <span className="shrink-0 inline-flex items-center rounded-xl
                                bg-emerald-50 px-3 py-1.5 text-xs font-medium text-emerald-800 ring-1
                                ring-emerald-200 dark:bg-emerald-950/50 dark:text-emerald-200 dark:ring-emerald-900">
                                    {t("housings.itemCard.availableFrom", { defaultValue: "Available from"})} {formatDate(h.availableFromDate!)}
                                </span>
                            )}
                        </div>

                        {/* Metrics — SAME layout, just tighter spacing */}
                        <div className="mt-3 grid grid-cols-2 gap-3">
                            <div className="rounded-xl border border-slate-200/70 bg-white/60 px-3 py-2
                            dark:border-slate-800/70 dark:bg-slate-950/30">
                                <div className="text-xs text-slate-500 dark:text-slate-400">
                                    {t("housings.itemCard.pricePerMonth", { defaultValue: "Price per month" })}
                                </div>
                                <div className="mt-0.5 text-sm font-semibold text-slate-900 dark:text-slate-50">
                                    {formatPrice(h.pricePerMonth)} NOK
                                </div>
                            </div>

                            <div className="rounded-xl border border-slate-200/70 bg-white/60 px-3 py-2
                            dark:border-slate-800/70 dark:bg-slate-950/30">
                                <div className="text-xs text-slate-500 dark:text-slate-400">
                                    {t("housings.itemCard.area", { defaultValue: "Area" })}
                                </div>
                                <div className="mt-0.5 text-sm font-semibold text-slate-900 dark:text-slate-50">
                                    {h.areaSqm} m²
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </article>
        </Link>
    );
}

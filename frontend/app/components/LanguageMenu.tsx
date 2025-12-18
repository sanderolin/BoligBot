import { useEffect, useRef, useState } from "react";
import { useFetcher, useLocation } from "react-router";
import { useTranslation } from "react-i18next";

type Locale = "nb" | "en";

const localeLabel: Record<Locale, string> = {
    nb: "Norsk",
    en: "English",
};

const localeFlag: Record<Locale, string> = {
    nb: "🇳🇴",
    en: "🇬🇧",
};

export function LanguageMenu() {
    const { i18n } = useTranslation();
    const fetcher = useFetcher();
    const location = useLocation();

    const current: Locale = i18n.language?.startsWith("nb") ? "nb" : "en";
    const [open, setOpen] = useState(false);

    const dropdownRef = useRef<HTMLDivElement>(null);

    // Close after locale change completes
    useEffect(() => {
        if (fetcher.state === "idle") setOpen(false);
    }, [fetcher.state]);

    // Close on outside click
    useEffect(() => {
        if (!open) return;

        function handleClickOutside(event: MouseEvent) {
            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(event.target as Node)
            ) {
                setOpen(false);
            }
        }

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [open]);

    return (
        <div ref={dropdownRef} className="relative">
            <button
                type="button"
                onClick={() => setOpen((v) => !v)}
                className="inline-flex items-center gap-2 rounded-xl border
          border-slate-200 bg-white/70 px-3 py-2 text-lg font-semibold
          text-slate-900 shadow-sm backdrop-blur hover:bg-white
          dark:border-slate-800 dark:bg-slate-950/50
          dark:text-white dark:hover:bg-slate-900"
                aria-haspopup="menu"
                aria-expanded={open}
            >
                <span aria-hidden="true">{localeFlag[current]}</span>
                <span className="text-slate-400 dark:text-slate-500">▾</span>
            </button>

            {open && (
                <div
                    role="menu"
                    className="absolute right-0 z-30 mt-2 w-44 overflow-hidden
            rounded-2xl border border-slate-200 bg-white shadow-lg
            dark:border-slate-800 dark:bg-slate-950"
                >
                    <fetcher.Form method="post" action="/actions/set-locale">
                        <input
                            type="hidden"
                            name="redirectTo"
                            value={location.pathname + location.search}
                        />

                        {(["nb", "en"] as const).map((lng) => (
                            <button
                                key={lng}
                                type="submit"
                                name="locale"
                                value={lng}
                                role="menuitem"
                                className="flex w-full items-center gap-2 px-4 py-3
                  text-left text-sm hover:bg-slate-50
                  dark:hover:bg-slate-900"
                            >
                                <span aria-hidden="true">{localeFlag[lng]}</span>
                                <span className="font-medium">{localeLabel[lng]}</span>
                            </button>
                        ))}
                    </fetcher.Form>
                </div>
            )}
        </div>
    );
}

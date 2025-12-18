import type { Route } from "./+types/home";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "BoligBot"}
  ];
}

export default function Home() {
  const { t } = useTranslation();

  return (
      <section className="relative mx-auto flex w-full max-w-6xl flex-col items-center justify-center px-6 text-center">
        <h1 className="text-balance text-4xl font-semibold tracking-tight sm:text-6xl">
          {t("landing.title")}
        </h1>

        <p className="mt-5 max-w-2xl text-pretty text-base leading-7 text-slate-600 sm:text-lg dark:text-slate-300">
          {t("landing.description")}
        </p>

        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-center">
          <Link
              to="/housings"
              className="inline-flex items-center justify-center rounded-2xl
                bg-slate-900 px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-slate-800
                dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {t("landing.viewCatalog")}
          </Link>
          <Link
              to="/login"
              className="inline-flex items-center justify-center rounded-2xl border
                border-slate-200 bg-white/70 px-5 py-3 text-sm font-semibold text-slate-900 shadow-sm backdrop-blur
                hover:bg-white dark:border-slate-800 dark:bg-slate-950/50 dark:text-white dark:hover:bg-slate-900"
          >
            {t("landing.login")}
          </Link>
        </div>
      </section>
  );
}

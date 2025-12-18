import { Link } from "react-router";
import { useTranslation } from "react-i18next";
import { LanguageMenu } from "~/components/LanguageMenu";

export function AppHeader() {
    const { t } = useTranslation();

    return (
        <header className="relative z-20 mx-auto w-full max-w-6xl px-6 pt-6">
            <div className="flex items-center justify-between">
                <Link
                    to="/"
                    className="flex items-center gap-3 hover:opacity-90 focus:outline-none"
                >
                    <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-indigo-500 to-cyan-400 shadow-sm" />
                    <span className="text-xl font-semibold tracking-tight">
                        {t("title")}
                    </span>
                </Link>

                <LanguageMenu />
            </div>
        </header>
    );
}

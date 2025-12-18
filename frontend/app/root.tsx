import {
  data,
  isRouteErrorResponse,
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
} from "react-router";
import { useEffect } from "react";
import type { Route } from "./+types/root";
import {
  getLocale,
  i18nextMiddleware,
  localeCookie,
} from "./middleware/i18next";
import { useTranslation } from "react-i18next";
import "./app.css";
import {AppHeader} from "~/components/AppHeader";

export const middleware = [i18nextMiddleware];

export async function loader({ context }: Route.LoaderArgs) {
  let locale = getLocale(context);
  return data(
      { locale }, // Return the locale to the UI
      { headers: { "Set-Cookie": await localeCookie.serialize(locale) } },
  );
}

export const links: Route.LinksFunction = () => [
  { rel: "preconnect", href: "https://fonts.googleapis.com" },
  {
    rel: "preconnect",
    href: "https://fonts.gstatic.com",
    crossOrigin: "anonymous",
  },
  {
    rel: "stylesheet",
    href: "https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap",
  },
];

export function Layout({ children }: { children: React.ReactNode }) {
  let { i18n } = useTranslation();
  return (
    <html lang={i18n.language} dir={i18n.dir(i18n.language)}>
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <Meta />
        <Links />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  );
}

export default function App({ loaderData: { locale } }: Route.ComponentProps) {
  let { i18n } = useTranslation();
  useEffect(() => {
    if (i18n.language !== locale) i18n.changeLanguage(locale);
  }, [locale, i18n]);
  return (
      <main className="relative min-h-screen bg-white text-slate-900 dark:bg-slate-950 dark:text-slate-50 flex flex-col">
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div className="absolute -top-48 left-1/2 h-[520px] w-[520px] -translate-x-1/2 rounded-full bg-gradient-to-tr from-indigo-500/25 via-cyan-400/15 to-emerald-400/15 blur-3xl" />
          <div className="absolute -bottom-56 left-1/3 h-[520px] w-[520px] rounded-full bg-gradient-to-tr from-fuchsia-500/10 via-indigo-500/10 to-cyan-400/10 blur-3xl" />
        </div>

        <AppHeader />

        <div className="relative flex-1 flex">
          <Outlet />
        </div>
      </main>
  );
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
  let message = "Oops!";
  let details = "An unexpected error occurred.";
  let stack: string | undefined;

  if (isRouteErrorResponse(error)) {
    message = error.status === 404 ? "404" : "Error";
    details =
      error.status === 404
        ? "The requested page could not be found."
        : error.statusText || details;
  } else if (import.meta.env.DEV && error && error instanceof Error) {
    details = error.message;
    stack = error.stack;
  }

  return (
    <main className="pt-16 p-4 container mx-auto">
      <h1>{message}</h1>
      <p>{details}</p>
      {stack && (
        <pre className="w-full p-4 overflow-x-auto">
          <code>{stack}</code>
        </pre>
      )}
    </main>
  );
}

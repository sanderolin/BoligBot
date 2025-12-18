import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
    index("routes/home.tsx"),
    route("api/locales/:lng/:ns", "routes/locales.ts"),
    route("actions/set-locale", "routes/actions.set-locale.ts"),

    route("housings", "./housings/index.tsx"),
    route("subscriptions", "./subscriptions/index.tsx"),
] satisfies RouteConfig;

import { redirect } from "react-router";
import type { ActionFunctionArgs } from "react-router";
import { localeCookie } from "~/middleware/i18next";

export async function action({ request }: ActionFunctionArgs) {
    const form = await request.formData();
    const locale = form.get("locale");
    const redirectTo = form.get("redirectTo");

    const next = locale === "nb" || locale === "en" ? locale : "nb";
    const to = typeof redirectTo === "string" ? redirectTo : "/";

    const headers = new Headers();
    headers.append("Set-Cookie", await localeCookie.serialize(next));

    return redirect(to, { headers });
}

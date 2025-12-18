export default {
    title: "BoligBot",
    description: "",
    landing: {
        title: "Tired of manually scouting SiT for available housings?",
        description: "BoligBot gives you a full overview of all SiT housings and notifies you when a place you want becomes available.",
        viewCatalog: "View housings catalog",
        subscriptions: "My subscriptions",
        login: "Log in",
    },
    housings: {
        title: "Housings",
        itemCard: {
            availableFrom: "Available from",
            pricePerMonth: "Price per month",
            area: "Area",
        },
        results: "Page {{page}} of {{pages}} ({{total}} total)",
        filters: {
            title: "Filters",
            reset: "Reset",
            city: "City",
            district: "District",
            housingType: "Housing type",
            price: "Price",
            area: "Area",
            min: "Min",
            max: "Max",
            sorting: "Sorting",
        }
    },
} satisfies typeof import("~/locales/nb/translation").default;
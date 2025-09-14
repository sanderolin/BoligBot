package no.sanderolin.boligbot.service.housings;

public enum HousingSortBy {
    AVAILABLE_FROM_DATE("availableFromDate"),
    PRICE_PER_MONTH("pricePerMonth"),
    AREA_SQM("areaSqm"),
    CITY("city"),
    DISTRICT("district");

    private final String property;
    HousingSortBy(String property) { this.property = property; }
    public String property() { return property; }
}
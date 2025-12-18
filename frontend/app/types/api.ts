export type PagedResponse<T> = {
    items: T[];
    page: number;
    size: number;
    total: number;
    hasNext: boolean;
};

export type HousingDTO = {
    rentalObjectId: string;
    address: string;
    name: string;
    housingType: string;
    city: string;
    district: string;
    areaSqm: string;
    pricePerMonth: number;
    isAvailable: boolean;
    availableFromDate: string | null;
};
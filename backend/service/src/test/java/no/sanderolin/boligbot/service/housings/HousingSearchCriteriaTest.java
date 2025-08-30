package no.sanderolin.boligbot.service.housings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class HousingSearchCriteriaTest {

    @Test
    void pageOrDefault_WhenPageIsNull_ShouldReturnDefaultPage() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setPage(null)
                .build();

        int result = criteria.pageOrDefault();

        assertEquals(0, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void pageOrDefault_WhenPageIsZeroOrNegative_ShouldReturnZero(int page) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setPage(page)
                .build();

        int result = criteria.pageOrDefault();

        assertEquals(0, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100})
    void pageOrDefault_WhenPageIsPositive_ShouldReturnPage(int page) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setPage(page)
                .build();

        int result = criteria.pageOrDefault();

        assertEquals(page, result);
    }

    @Test
    void sizeOrDefault_WhenSizeIsNull_ShouldReturnDefaultSize() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSize(null)
                .build();

        int result = criteria.sizeOrDefault();

        assertEquals(20, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void sizeOrDefault_WhenSizeIsZeroOrNegative_ShouldReturnOne(int size) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSize(size)
                .build();

        int result = criteria.sizeOrDefault();

        assertEquals(1, result);
    }

    @Test
    void sizeOrDefault_WhenSizeExceedsMax_ShouldReturnMaxSize() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSize(150)
                .build();

        int result = criteria.sizeOrDefault();

        assertEquals(100, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 20, 50, 100})
    void sizeOrDefault_WhenSizeIsValid_ShouldReturnSize(int size) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSize(size)
                .build();

        int result = criteria.sizeOrDefault();

        assertEquals(size, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    void sortByOrDefault_WhenSortByIsNullOrBlank_ShouldReturnDefaultSort(String sortBy) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy(sortBy)
                .build();

        String result = criteria.sortByOrDefault();

        assertEquals("availableFromDate", result);
    }

    @Test
    void sortByOrDefault_WhenSortByIsNull_ShouldReturnDefaultSort() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy(null)
                .build();

        String result = criteria.sortByOrDefault();

        assertEquals("availableFromDate", result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidField", "notWhitelisted", "randomField"})
    void sortByOrDefault_WhenSortByIsNotInWhitelist_ShouldReturnDefaultSort(String sortBy) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy(sortBy)
                .build();

        String result = criteria.sortByOrDefault();

        assertEquals("availableFromDate", result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"availableFromDate", "pricePerMonth", "name", "areaSqm"})
    void sortByOrDefault_WhenSortByIsInWhitelist_ShouldReturnTrimmedValue(String sortBy) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy("  " + sortBy + "  ")
                .build();

        String result = criteria.sortByOrDefault();

        assertEquals(sortBy, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    void sortDirectionOrDefault_WhenSortDirectionIsBlank_ShouldReturnDefaultDirection(String direction) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortDirection(direction)
                .build();

        Sort.Direction result = criteria.sortDirectionOrDefault();

        assertEquals(Sort.Direction.ASC, result);
    }

    @Test
    void sortDirectionOrDefault_WhenSortDirectionIsNull_ShouldReturnDefaultDirection() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortDirection(null)
                .build();

        Sort.Direction result = criteria.sortDirectionOrDefault();

        assertEquals(Sort.Direction.ASC, result);
    }

    @ParameterizedTest
    @CsvSource({
            "desc, DESC",
            "DESC, DESC",
            "Desc, DESC",
            "DESC, DESC"
    })
    void sortDirectionOrDefault_WhenSortDirectionIsDesc_ShouldReturnDesc(String input, Sort.Direction expected) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortDirection(input)
                .build();

        Sort.Direction result = criteria.sortDirectionOrDefault();

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"asc", "ASC", "Asc", "invalid", "random", "up", "down"})
    void sortDirectionOrDefault_WhenSortDirectionIsNotDesc_ShouldReturnAsc(String direction) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortDirection(direction)
                .build();

        Sort.Direction result = criteria.sortDirectionOrDefault();

        assertEquals(Sort.Direction.ASC, result);
    }

    @Test
    void toSpringSort_ShouldCreateSortWithPrimaryAndTieBreaker() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy("pricePerMonth")
                .setSortDirection("desc")
                .build();

        Sort result = criteria.toSpringSort();

        assertNotNull(result);
        assertEquals(2, result.toList().size());

        Sort.Order primaryOrder = result.toList().getFirst();
        assertEquals("pricePerMonth", primaryOrder.getProperty());
        assertEquals(Sort.Direction.DESC, primaryOrder.getDirection());
        assertEquals(Sort.NullHandling.NATIVE, primaryOrder.getNullHandling());

        Sort.Order tieBreakerOrder = result.toList().get(1);
        assertEquals("rentalObjectId", tieBreakerOrder.getProperty());
        assertEquals(Sort.Direction.ASC, tieBreakerOrder.getDirection());
    }

    @Test
    void toSpringSort_WithDefaultValues_ShouldCreateCorrectSort() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder().build();

        Sort result = criteria.toSpringSort();

        assertNotNull(result);
        assertEquals(2, result.toList().size());

        Sort.Order primaryOrder = result.toList().getFirst();
        assertEquals("availableFromDate", primaryOrder.getProperty());
        assertEquals(Sort.Direction.ASC, primaryOrder.getDirection());
        assertEquals(Sort.NullHandling.NATIVE, primaryOrder.getNullHandling());

        Sort.Order tieBreakerOrder = result.toList().get(1);
        assertEquals("rentalObjectId", tieBreakerOrder.getProperty());
        assertEquals(Sort.Direction.ASC, tieBreakerOrder.getDirection());
    }

    @Test
    void toSpringSort_WithInvalidSortBy_ShouldUseDefault() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy("invalidField")
                .setSortDirection("desc")
                .build();

        Sort result = criteria.toSpringSort();

        Sort.Order primaryOrder = result.toList().getFirst();
        assertEquals("availableFromDate", primaryOrder.getProperty());
        assertEquals(Sort.Direction.DESC, primaryOrder.getDirection());
    }

    @Test
    void builder_ShouldCreateInstanceWithAllFields() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setCity("Oslo")
                .setDistrict("Sentrum")
                .setHousingType("Apartment")
                .setPage(1)
                .setSize(10)
                .setSortBy("pricePerMonth")
                .setSortDirection("desc")
                .build();

        assertEquals("Oslo", criteria.city());
        assertEquals("Sentrum", criteria.district());
        assertEquals("Apartment", criteria.housingType());
        assertEquals(1, criteria.page());
        assertEquals(10, criteria.size());
        assertEquals("pricePerMonth", criteria.sortBy());
        assertEquals("desc", criteria.sortDirection());
    }

    @Test
    void builder_WithNullValues_ShouldAllowNullFields() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setCity(null)
                .setDistrict(null)
                .setHousingType(null)
                .setPage(null)
                .setSize(null)
                .setSortBy(null)
                .setSortDirection(null)
                .build();

        assertNull(criteria.city());
        assertNull(criteria.district());
        assertNull(criteria.housingType());
        assertNull(criteria.page());
        assertNull(criteria.size());
        assertNull(criteria.sortBy());
        assertNull(criteria.sortDirection());
    }

    @Test
    void criteriaIntegration_ShouldWorkEndToEnd() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setPage(2)
                .setSize(15)
                .setSortBy("pricePerMonth")
                .setSortDirection("desc")
                .build();

        assertEquals(2, criteria.pageOrDefault());
        assertEquals(15, criteria.sizeOrDefault());
        assertEquals("pricePerMonth", criteria.sortByOrDefault());
        assertEquals(Sort.Direction.DESC, criteria.sortDirectionOrDefault());

        Sort sort = criteria.toSpringSort();
        assertEquals(2, sort.toList().size());
        assertEquals("pricePerMonth", sort.toList().get(0).getProperty());
        assertEquals("rentalObjectId", sort.toList().get(1).getProperty());
    }
}
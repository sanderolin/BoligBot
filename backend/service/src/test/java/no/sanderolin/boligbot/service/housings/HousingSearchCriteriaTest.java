package no.sanderolin.boligbot.service.housings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

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
    @EnumSource(HousingSortBy.class)
    void sortByOrDefault_WhenSortByIsValid_ShouldReturnCorrectSortBy(HousingSortBy sortBy) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy(sortBy)
                .build();

        HousingSortBy result = criteria.sortByOrDefault();

        assertEquals(sortBy, result);
    }

    @ParameterizedTest
    @EnumSource(SortDirection.class)
    void sortDirectionOrDefault_WhenSortDirectionIsValid_ShouldReturnCorrectSortDirection(SortDirection direction) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortDirection(direction)
                .build();

        SortDirection result = criteria.sortDirectionOrDefault();

        assertEquals(direction, result);
    }

    @Test
    void toSpringSort_ShouldCreateSortWithPrimaryAndTieBreaker() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy(HousingSortBy.PRICE_PER_MONTH)
                .setSortDirection(SortDirection.DESC)
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
                .setSortBy(null)
                .setSortDirection(SortDirection.DESC)
                .build();

        Sort result = criteria.toSpringSort();

        Sort.Order primaryOrder = result.toList().getFirst();
        assertEquals("availableFromDate", primaryOrder.getProperty());
        assertEquals(Sort.Direction.DESC, primaryOrder.getDirection());
    }

    @Test
    void minPriceOrNull_WhenMinPriceIsNull_ShouldReturnNull() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMinPricePerMonth(null)
                .build();

        assertNull(criteria.minPricePerMonth());
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void minPriceOrNull_WhenMinPriceIsZeroOrNegative_ShouldReturnNull(int minPrice) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMinPricePerMonth(minPrice)
                .build();

        assertNull(criteria.minPricePerMonthOrNull());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 15000})
    void minPriceOrNull_WhenMinPriceIsPositive_ShouldReturnSame(int minPrice) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMinPricePerMonth(minPrice)
                .build();

        assertEquals(minPrice, criteria.minPricePerMonthOrNull());
    }

    @Test
    void maxPriceOrNull_WhenMaxPriceIsNull_ShouldReturnNull() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMaxPricePerMonth(null)
                .build();

        assertNull(criteria.maxPricePerMonthOrNull());
    }

    @ParameterizedTest
    @ValueSource(ints = {-5, -1, 0})
    void maxPriceOrNull_WhenMaxPriceIsZeroOrNegative_ShouldReturnNull(int maxPrice) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMaxPricePerMonth(maxPrice)
                .build();

        assertNull(criteria.maxPricePerMonthOrNull());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 9999})
    void maxPriceOrNull_WhenMaxPriceIsPositive_ShouldReturnSame(int maxPrice) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMaxPricePerMonth(maxPrice)
                .build();

        assertEquals(maxPrice, criteria.maxPricePerMonthOrNull());
    }

    @Test
    void minAreaOrNull_WhenMinAreaIsNull_ShouldReturnNull() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMinAreaSqm(null)
                .build();

        assertNull(criteria.minAreaOrNull());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-10", "-1", "0", "0.0"})
    void minAreaOrNull_WhenMinAreaIsZeroOrNegative_ShouldReturnNull(String value) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMinAreaSqm(new BigDecimal(value))
                .build();

        assertNull(criteria.minAreaOrNull());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.1", "1", "25", "45.5"})
    void minAreaOrNull_WhenMinAreaIsPositive_ShouldReturnSame(String value) {
        BigDecimal expected = new BigDecimal(value);
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMinAreaSqm(expected)
                .build();

        BigDecimal result = criteria.minAreaOrNull();
        assertNotNull(result);
        assertEquals(0, result.compareTo(expected));
    }

    @Test
    void maxAreaOrNull_WhenMaxAreaIsNull_ShouldReturnNull() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMaxAreaSqm(null)
                .build();

        assertNull(criteria.maxAreaOrNull());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-3", "-0.1", "0", "0.00"})
    void maxAreaOrNull_WhenMaxAreaIsZeroOrNegative_ShouldReturnNull(String value) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMaxAreaSqm(new BigDecimal(value))
                .build();

        assertNull(criteria.maxAreaOrNull());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.5", "10", "30.75"})
    void maxAreaOrNull_WhenMaxAreaIsPositive_ShouldReturnSame(String value) {
        BigDecimal expected = new BigDecimal(value);
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setMaxAreaSqm(expected)
                .build();

        BigDecimal result = criteria.maxAreaOrNull();
        assertNotNull(result);
        assertEquals(0, result.compareTo(expected));
    }
}
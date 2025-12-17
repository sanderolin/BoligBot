package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.dao.repository.HousingRepository;
import no.sanderolin.boligbot.housingimport.dto.HousingAvailabilityDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousingAvailabilityImportServiceTest {

    @Mock private HousingAvailabilityFetcher availabilityFetcher;
    @Mock private HousingRepository housingRepository;
    @InjectMocks private HousingAvailabilityImportService importTask;

    private List<HousingAvailabilityDTO> availableHousings;
    private List<String> availableHousingIds;

    @BeforeEach
    void setUp() {
        availableHousings = List.of(new HousingAvailabilityDTO("1", LocalDate.now()));
        availableHousingIds = List.of("1");
        lenient().when(housingRepository.count()).thenReturn(6000L);
    }

    @Test
    void runImport_ShouldUpdateAvailability() {
        when(availabilityFetcher.fetchAvailabilityFromGraphQL()).thenReturn(availableHousings);

        when(housingRepository.markAvailableIfInIds(eq(availableHousingIds))).thenReturn(3);
        when(housingRepository.markUnavailableIfNotInIds(eq(availableHousingIds))).thenReturn(2);
        when(housingRepository.updateAvailableFromDate(eq("1"), any(LocalDate.class))).thenReturn(1);

        importTask.runImport();

        verify(housingRepository).count();
        verify(availabilityFetcher).fetchAvailabilityFromGraphQL();
        verify(housingRepository).markAvailableIfInIds(eq(availableHousingIds));
        verify(housingRepository).markUnavailableIfNotInIds(eq(availableHousingIds));
        verify(housingRepository).updateAvailableFromDate(eq("1"), any(LocalDate.class));
    }

    @Test
    void runImport_WithEmptyDatabase_ShouldExitEarly() {
        when(housingRepository.count()).thenReturn(0L);

        importTask.runImport();

        verify(housingRepository).count();
        verifyNoMoreInteractions(availabilityFetcher, housingRepository);
    }

    @Test
    void runImport_WithEmptyResponse_ShouldExitEarly() {
        when(availabilityFetcher.fetchAvailabilityFromGraphQL()).thenReturn(Collections.emptyList());
        importTask.runImport();

        verify(housingRepository).count();
        verify(availabilityFetcher).fetchAvailabilityFromGraphQL();
        verifyNoMoreInteractions(housingRepository);
    }

    @Test
    void runImport_WhenFetcherThrowsHousingImportException_ShouldPropagate() {
        when(availabilityFetcher.fetchAvailabilityFromGraphQL())
                .thenThrow(new HousingImportException("API unavailable"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("API unavailable");

        verify(housingRepository).count();
        verify(availabilityFetcher).fetchAvailabilityFromGraphQL();
        verifyNoMoreInteractions(housingRepository);
    }

    @Test
    void runImport_WhenBulkUpdateAvailabilityTrueThrows_ShouldWrapAsUnexpectedException() {
        when(availabilityFetcher.fetchAvailabilityFromGraphQL()).thenReturn(availableHousings);
        when(housingRepository.markAvailableIfInIds(anyList()))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during availability import")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(housingRepository).count();
        verify(availabilityFetcher).fetchAvailabilityFromGraphQL();
        verify(housingRepository).markAvailableIfInIds(eq(availableHousingIds));
        verifyNoMoreInteractions(housingRepository);
    }

    @Test
    void runImport_WhenBulkUpdateAvailabilityFalseThrows_ShouldWrapAsUnexpectedException() {
        when(availabilityFetcher.fetchAvailabilityFromGraphQL()).thenReturn(availableHousings);
        when(housingRepository.markAvailableIfInIds(anyList())).thenReturn(1);
        when(housingRepository.markUnavailableIfNotInIds(anyList()))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during availability import")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(housingRepository).count();
        verify(availabilityFetcher).fetchAvailabilityFromGraphQL();
        verify(housingRepository).markAvailableIfInIds(eq(availableHousingIds));
        verify(housingRepository).markUnavailableIfNotInIds(eq(availableHousingIds));
        verifyNoMoreInteractions(housingRepository);
    }

    @Test
    void runImport_WhenUpdateAvailableFromDateThrows_ShouldWrapAsUnexpectedException() {
        when(availabilityFetcher.fetchAvailabilityFromGraphQL()).thenReturn(availableHousings);
        when(housingRepository.markAvailableIfInIds(anyList())).thenReturn(1);
        when(housingRepository.markUnavailableIfNotInIds(anyList())).thenReturn(1);

        when(housingRepository.updateAvailableFromDate(eq("1"), any()))
                .thenThrow(new RuntimeException("DB error 3"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during availability import")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(housingRepository).count();
        verify(availabilityFetcher).fetchAvailabilityFromGraphQL();
        verify(housingRepository).markAvailableIfInIds(anyList());
        verify(housingRepository).markUnavailableIfNotInIds(anyList());
        verify(housingRepository).updateAvailableFromDate(eq("1"), any());
    }
}
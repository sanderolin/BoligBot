package no.sanderolin.boligbot.service.housings;

import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousingServiceTest {

    @Mock
    private HousingRepository housingRepository;

    @InjectMocks
    private HousingService housingService;

    private List<HousingModel> mockHousings;

    @BeforeEach
    void setUp() {
        mockHousings = Arrays.asList(
                new HousingModel(),
                new HousingModel(),
                new HousingModel()
        );
    }

    @Test
    void searchHousings_ShouldCallRepositoryWithSpecificationAndPageable() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setCity("Trondheim")
                .setDistrict("Moholt")
                .setHousingType("1-room apartment")
                .setPage(1)
                .setSize(10)
                .build();

        Page<HousingModel> expectedPage = new PageImpl<>(mockHousings);
        when(housingRepository.findAll(ArgumentMatchers.<Specification<HousingModel>>any(), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<HousingModel> result = housingService.searchHousings(criteria);

        assertEquals(expectedPage, result);
        verify(housingRepository).findAll(ArgumentMatchers.<Specification<HousingModel>>any(), any(Pageable.class));
    }

    @Test
    void searchHousings_ShouldCreatePageableFromCriteria() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setPage(2)
                .setSize(15)
                .build();

        Page<HousingModel> expectedPage = new PageImpl<>(mockHousings);
        when(housingRepository.findAll(ArgumentMatchers.<Specification<HousingModel>>any(), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<HousingModel> result = housingService.searchHousings(criteria);
        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(housingRepository).findAll(ArgumentMatchers.<Specification<HousingModel>>any(), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(15, capturedPageable.getPageSize());
    }

    @Test
    void searchHousings_ShouldCreateSortFromCriteria() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setSortBy(HousingSortBy.PRICE_PER_MONTH)
                .setSortDirection(SortDirection.DESC)
                .build();

        Page<HousingModel> expectedPage = new PageImpl<>(List.of());
        when(housingRepository.findAll(ArgumentMatchers.<Specification<HousingModel>>any(), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<HousingModel> result = housingService.searchHousings(criteria);
        assertSame(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(housingRepository).findAll(ArgumentMatchers.<Specification<HousingModel>>any(), pageableCaptor.capture());

        Pageable p = pageableCaptor.getValue();
        Sort.Order primary = p.getSort().getOrderFor("pricePerMonth");
        assertNotNull(primary);
        assertEquals(Sort.Direction.DESC, primary.getDirection());
        assertEquals(Sort.NullHandling.NATIVE, primary.getNullHandling());

        Sort.Order tie = p.getSort().getOrderFor("rentalObjectId");
        assertNotNull(tie);
        assertEquals(Sort.Direction.ASC, tie.getDirection());
    }


    @Test
    void searchHousings_WithEmptyResult_ShouldReturnEmptyPage() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder().build();
        Page<HousingModel> emptyPage = new PageImpl<>(List.of());
        when(housingRepository.findAll(ArgumentMatchers.<Specification<HousingModel>>any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<HousingModel> result = housingService.searchHousings(criteria);

        assertEquals(emptyPage, result);
        assertTrue(result.isEmpty());
        verify(housingRepository).findAll(ArgumentMatchers.<Specification<HousingModel>>any(), any(Pageable.class));
    }

    @Test
    void searchHousings_RepositoryThrowsException_ShouldPropagateException() {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder().build();
        when(housingRepository.findAll(ArgumentMatchers.<Specification<HousingModel>>any(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> housingService.searchHousings(criteria));
        verify(housingRepository).findAll(ArgumentMatchers.<Specification<HousingModel>>any(), any(Pageable.class));
    }

    @Test
    void getHousingByRentalObjectId_WithExistingRentalObjectId_ShouldReturnHousing() {
        String rentalObjectId = "r123";
        HousingModel model = new HousingModel();

        when(housingRepository.findById(rentalObjectId)).thenReturn(java.util.Optional.of(model));

        HousingModel result = housingService.getHousingByRentalObjectId(rentalObjectId);

        assertSame(model, result);
        verify(housingRepository).findById(rentalObjectId);
        verifyNoMoreInteractions(housingRepository);
    }

    @Test
    void getHousingByRentalObjectId_WithMissingRentalObjectId_ShouldThrowObjectNotFoundException() {
        String rentalObjectId = "missing-42";
        when(housingRepository.findById(rentalObjectId)).thenReturn(java.util.Optional.empty());

        assertThrows(ObjectNotFoundException.class, () ->
            housingService.getHousingByRentalObjectId(rentalObjectId)
        );

        verify(housingRepository).findById(rentalObjectId);
    }

}
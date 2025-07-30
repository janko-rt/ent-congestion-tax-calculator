package hr.janko.ent.ctc.service.impl;

import hr.janko.ent.ctc.data.model.CityModel;
import hr.janko.ent.ctc.data.model.TaxModel;
import hr.janko.ent.ctc.data.model.TaxStatus;
import hr.janko.ent.ctc.data.model.TaxTimetableModel;
import hr.janko.ent.ctc.generated.model.TaxRequestDto;
import hr.janko.ent.ctc.service.CityService;
import hr.janko.ent.ctc.service.TaxProcessingService;
import hr.janko.ent.ctc.service.TaxService;
import hr.janko.ent.ctc.service.TaxTimetableService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaxProcessingServiceImplTest {

    public static final String UUID = "1234-4321";
    public static final LocalDateTime CORRECT_TIMESTAMP = LocalDateTime.of(2013, 7, 12, 13, 13, 13);
    public static final LocalDateTime WEEKEND_TIMESTAMP = LocalDateTime.of(2013, 7, 13, 13, 13, 13);
    public static final BigDecimal AMOUNT = new BigDecimal("13.13");
    public static final long TIMETABLE_ID = 13L;
    public static final LocalTime START_TIME = LocalTime.of(12, 19, 19);
    public static final LocalTime END_TIME = LocalTime.of(14, 19, 19);
    public static final String REGISTRATION_PLATE_NUMBER = "ZG5544TR";
    public static final String CURRENCY = "SEK";
    private final TaxService taxService = mock(TaxService.class);
    private final TaxTimetableService taxTimetableService = mock(TaxTimetableService.class);
    private final CityService cityService = mock(CityService.class);

    private final TaxProcessingService taxProcessingService = new TaxProcessingServiceImpl(taxService, taxTimetableService, cityService);

    @Test
    void processIncomingTaxRequest_registrationPlateNull_returnedTaxModelError(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto();
        TaxModel taxModel = TaxModel.builder().uuid(UUID).build();
        when(taxService.saveTax(any())).thenReturn(taxModel);
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.ERROR);
        assertThat(result.getErrorMessage()).isEqualTo("Registration plate number missing");
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_cityIdNull_returnedTaxModelError(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER);
        TaxModel taxModel = TaxModel.builder().uuid(UUID).build();
        when(taxService.saveTax(any())).thenReturn(taxModel);
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.ERROR);
        assertThat(result.getErrorMessage()).isEqualTo("City id missing");
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_timestampIdNull_returnedTaxModelError(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER)
                .cityId(1);
        TaxModel taxModel = TaxModel.builder().uuid(UUID).build();
        when(taxService.saveTax(any())).thenReturn(taxModel);
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.ERROR);
        assertThat(result.getErrorMessage()).isEqualTo("Timestamp missing");
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_timestampNot2013IdNull_returnedTaxModelError(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER)
                .cityId(1)
                .timestamp(LocalDateTime.now());
        TaxModel taxModel = TaxModel.builder().uuid(UUID).build();
        when(taxService.saveTax(any())).thenReturn(taxModel);
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.ERROR);
        assertThat(result.getErrorMessage()).isEqualTo("Timestamp is not in 2013");
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_cityNotFound_returnedTaxModelError(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER)
                .cityId(1)
                .timestamp(LocalDateTime.of(2013,7,13, 13,13,13));
        TaxModel taxModel = TaxModel.builder().uuid(UUID).build();
        when(taxService.saveTax(any())).thenReturn(taxModel);
        when(cityService.fetchById(any())).thenReturn(Optional.empty());
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.ERROR);
        assertThat(result.getErrorMessage()).isEqualTo("City not found");
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_noTaxTablesFound_returnedTaxModelError(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER)
                .cityId(1)
                .timestamp(CORRECT_TIMESTAMP);
        TaxModel taxModel = TaxModel.builder().uuid(UUID).recordedDate(CORRECT_TIMESTAMP).build();
        CityModel cityModel = mock(CityModel.class);

        when(taxService.saveTax(any())).thenReturn(taxModel);
        when(cityService.fetchById(any())).thenReturn(Optional.of(cityModel));
        when(cityModel.getTaxTimetables()).thenReturn(List.of());
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.ERROR);
        assertThat(result.getErrorMessage()).isEqualTo("Incorrect number of tax tables");
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_weekend_returnedTaxModelIgnored(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER)
                .cityId(1)
                .timestamp(WEEKEND_TIMESTAMP);
        TaxModel taxModel = TaxModel.builder().taxStatus(TaxStatus.NEW).uuid(UUID).recordedDate(WEEKEND_TIMESTAMP).build();
        CityModel cityModel = mock(CityModel.class);
        TaxTimetableModel taxTimetableModel = new TaxTimetableModel();
        taxTimetableModel.setAmount(AMOUNT);
        taxTimetableModel.setId(TIMETABLE_ID);
        taxTimetableModel.setStartTime(START_TIME);
        taxTimetableModel.setEndTime(END_TIME);

        when(taxService.saveTax(any())).thenReturn(taxModel);
        when(cityService.fetchById(any())).thenReturn(Optional.of(cityModel));
        when(cityModel.getTaxTimetables()).thenReturn(List.of(taxTimetableModel));
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.IGNORED);
        assertThat(result.getErrorMessage()).isEqualTo("Weekend");
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_singleChargeRuleFailed_returnedTaxModelCanceled(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER)
                .cityId(1)
                .timestamp(CORRECT_TIMESTAMP);
        TaxModel taxModel = TaxModel.builder().taxStatus(TaxStatus.NEW).uuid(UUID).recordedDate(CORRECT_TIMESTAMP).build();
        TaxModel taxModelAfterSingleCharge = TaxModel.builder().taxStatus(TaxStatus.CANCELED).uuid(UUID).recordedDate(WEEKEND_TIMESTAMP).build();
        CityModel cityModel = mock(CityModel.class);
        TaxTimetableModel taxTimetableModel = new TaxTimetableModel();
        taxTimetableModel.setAmount(AMOUNT);
        taxTimetableModel.setId(TIMETABLE_ID);
        taxTimetableModel.setStartTime(START_TIME);
        taxTimetableModel.setEndTime(END_TIME);

        when(taxService.saveTax(any())).thenReturn(taxModel);
        when(cityService.fetchById(any())).thenReturn(Optional.of(cityModel));
        when(cityModel.getTaxTimetables()).thenReturn(List.of(taxTimetableModel));
        when(taxService.validateSingleChargeRule(any())).thenReturn(taxModelAfterSingleCharge);
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.CANCELED);
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_maximumChargeForDay_returnedTaxModelCanceled(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER)
                .cityId(1)
                .timestamp(CORRECT_TIMESTAMP);
        TaxModel taxModel = TaxModel.builder().taxStatus(TaxStatus.NEW).uuid(UUID).recordedDate(CORRECT_TIMESTAMP).build();
        TaxModel taxModelAfterSingleCharge = TaxModel.builder().taxStatus(TaxStatus.CANCELED).uuid(UUID).recordedDate(WEEKEND_TIMESTAMP).build();
        CityModel cityModel = mock(CityModel.class);
        TaxTimetableModel taxTimetableModel = new TaxTimetableModel();
        taxTimetableModel.setAmount(AMOUNT);
        taxTimetableModel.setId(TIMETABLE_ID);
        taxTimetableModel.setStartTime(START_TIME);
        taxTimetableModel.setEndTime(END_TIME);

        when(taxService.saveTax(any())).thenReturn(taxModel);
        when(cityService.fetchById(any())).thenReturn(Optional.of(cityModel));
        when(cityModel.getTaxTimetables()).thenReturn(List.of(taxTimetableModel));
        when(taxService.validateSingleChargeRule(any())).thenReturn(taxModel);
        when(taxService.checkMaximumLimitForDayIsNotReached(any())).thenReturn(taxModelAfterSingleCharge);
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getTaxStatus()).isEqualTo(TaxStatus.CANCELED);
        verify(taxService,times(2)).saveTax(any());
    }

    @Test
    void processIncomingTaxRequest_successfullyProcessed_returnedTaxModelDue(){
        //given
        TaxRequestDto taxRequestDto = new TaxRequestDto()
                .registrationPlateNumber(REGISTRATION_PLATE_NUMBER)
                .cityId(1)
                .timestamp(CORRECT_TIMESTAMP);
        TaxModel taxModel = mock(TaxModel.class);
        CityModel cityModel = mock(CityModel.class);
        TaxTimetableModel taxTimetableModel = new TaxTimetableModel();
        taxTimetableModel.setAmount(AMOUNT);
        taxTimetableModel.setId(TIMETABLE_ID);
        taxTimetableModel.setStartTime(START_TIME);
        taxTimetableModel.setEndTime(END_TIME);

        when(taxService.saveTax(any())).thenReturn(taxModel);
        when(cityService.fetchById(any())).thenReturn(Optional.of(cityModel));
        when(cityModel.getTaxTimetables()).thenReturn(List.of(taxTimetableModel));
        when(cityModel.getCurrency()).thenReturn(CURRENCY);
        when(taxService.validateSingleChargeRule(any())).thenReturn(taxModel);
        when(taxService.checkMaximumLimitForDayIsNotReached(any())).thenReturn(taxModel);

        when(taxModel.getTaxStatus()).thenReturn(TaxStatus.NEW);
        when(taxModel.getUuid()).thenReturn(UUID);
        when(taxModel.getRecordedDate()).thenReturn(CORRECT_TIMESTAMP);
        //when
        var result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);
        //then
        assertThat(result).isNotNull();
        verify(taxService,times(2)).saveTax(any());
        verify(taxModel,times(1)).setTaxStatus(TaxStatus.DUE);
        verify(taxModel,times(1)).setAmount(AMOUNT);
        verify(taxModel,times(1)).setTaxTimetableId(TIMETABLE_ID);
        verify(taxModel,times(1)).setCurrency(CURRENCY);
    }

}
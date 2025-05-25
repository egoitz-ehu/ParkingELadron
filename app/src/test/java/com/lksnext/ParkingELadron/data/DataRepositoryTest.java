package com.lksnext.ParkingELadron.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.ParkingELadron.data.DataRepository;

public class DataRepositoryTest {

    private DataRepository dataRepository;
    private FirebaseFirestore mockFirestore;

    @Before
    public void setUp() {
        // Limpia el singleton antes de cada test
        DataRepository.resetInstance();

        // Crea el mock de Firestore
        mockFirestore = mock(FirebaseFirestore.class);

        // Crea la instancia singleton con el mock
        DataRepository.setInstance(DataRepository.createForTest(mockFirestore));
        dataRepository = DataRepository.getInstance();
    }

    @After
    public void tearDown() {
        // Limpia el singleton después de cada test para evitar efectos colaterales
        DataRepository.resetInstance();
    }

    @Test
    public void testIsSpotAvailable_NoReservations() {
        // Test when reservations list is null
        boolean result = dataRepository.isSpotAvailable(null, "2025-05-24", "10:00", "12:00");
        assertTrue(result);

        // Test when reservations list is empty
        result = dataRepository.isSpotAvailable(new ArrayList<>(), "2025-05-24", "10:00", "12:00");
        assertTrue(result);
    }

    @Test
    public void testIsSpotAvailable_NoOverlap() {
        // Test when there are reservations but no overlap
        List<Map<String, Object>> reservations = new ArrayList<>();
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("day", "2025-05-24");
        reservation.put("startTime", "08:00");
        reservation.put("endTime", "09:00");
        reservations.add(reservation);

        boolean result = dataRepository.isSpotAvailable(reservations, "2025-05-24", "10:00", "12:00");
        assertTrue(result); // No overlap
    }

    @Test
    public void testIsSpotAvailable_Overlap() {
        // Test when there is an overlapping reservation
        List<Map<String, Object>> reservations = new ArrayList<>();
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("day", "2025-05-24");
        reservation.put("startTime", "10:30");
        reservation.put("endTime", "11:30");
        reservations.add(reservation);

        boolean result = dataRepository.isSpotAvailable(reservations, "2025-05-24", "10:00", "12:00");
        assertFalse(result); // Overlap
    }

    @Test
    public void testIsSpotAvailable_DifferentDay() {
        // Test when the reservation is on a different day
        List<Map<String, Object>> reservations = new ArrayList<>();
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("day", "2025-05-23");
        reservation.put("startTime", "10:00");
        reservation.put("endTime", "12:00");
        reservations.add(reservation);

        boolean result = dataRepository.isSpotAvailable(reservations, "2025-05-24", "10:00", "12:00");
        assertTrue(result); // Different day
    }

    @Test
    public void testTimeOverlaps_NoOverlapBefore() {
        // Test when the first interval ends before the second interval starts
        boolean result = dataRepository.timeOverlaps("08:00", "10:00", "10:00", "12:00");
        assertFalse(result); // No overlap
    }

    @Test
    public void testTimeOverlaps_NoOverlapAfter() {
        // Test when the second interval ends before the first interval starts
        boolean result = dataRepository.timeOverlaps("10:00", "12:00", "08:00", "10:00");
        assertFalse(result); // No overlap
    }

    @Test
    public void testTimeOverlaps_FullOverlap() {
        // Test when one interval completely overlaps the other
        boolean result = dataRepository.timeOverlaps("09:00", "12:00", "10:00", "11:00");
        assertTrue(result); // Overlap
    }

    @Test
    public void testTimeOverlaps_PartialOverlapStart() {
        // Test when the first interval partially overlaps the start of the second interval
        boolean result = dataRepository.timeOverlaps("09:00", "11:00", "10:00", "12:00");
        assertTrue(result); // Overlap
    }

    @Test
    public void testTimeOverlaps_PartialOverlapEnd() {
        // Test when the first interval partially overlaps the end of the second interval
        boolean result = dataRepository.timeOverlaps("11:00", "13:00", "10:00", "12:00");
        assertTrue(result); // Overlap
    }

    @Test
    public void testTimeOverlaps_ExactMatch() {
        // Test when the two intervals exactly match
        boolean result = dataRepository.timeOverlaps("10:00", "12:00", "10:00", "12:00");
        assertTrue(result); // Overlap
    }
}
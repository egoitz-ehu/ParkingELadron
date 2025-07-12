package com.lksnext.ParkingELadron.data;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import android.os.Looper;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;
import com.lksnext.ParkingELadron.util.LiveDataTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class DataRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DataRepository dataRepository;
    private MockedStatic<Looper> mockedLooper;

    @Mock
    FirebaseFirestore mockFirestore;

    @Mock
    CollectionReference mockCollection;

    @Mock
    DocumentReference mockDocRef;

    @Mock
    WriteBatch mockBatch;

    @Mock
    Task<Void> mockVoidTask;

    @Mock
    Task<DocumentSnapshot> mockDocTask;

    @Mock
    Task<QuerySnapshot> mockQueryTask;

    @Mock
    DocumentSnapshot mockDocSnap;

    @Mock
    QuerySnapshot mockQuerySnap;

    @Mock
    Task<com.google.firebase.firestore.DocumentReference> mockAddReservationTask;

    @Mock
    com.google.firebase.firestore.DocumentReference mockAddedDocRef;

    @Mock
    Looper mockMainLooper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedLooper = mockStatic(Looper.class);
        mockedLooper.when(Looper::getMainLooper).thenReturn(mockMainLooper);

        dataRepository = DataRepository.createForTest(mockFirestore);

        // Configuraciones de mock comunes para evitar NullPointerException
        when(mockFirestore.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocRef);
        when(mockDocRef.collection(anyString())).thenReturn(mockCollection);

        // Mock para las Tasks
        when(mockVoidTask.addOnSuccessListener(any())).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnFailureListener(any())).thenReturn(mockVoidTask);
        //when(mockVoidTask.addOnCompleteListener(any())).thenReturn(mockVoidTask);
        when(mockDocTask.addOnSuccessListener(any())).thenReturn(mockDocTask);
        when(mockDocTask.addOnFailureListener(any())).thenReturn(mockDocTask);
        //when(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask);
        when(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask);
        //when(mockAddReservationTask.addOnSuccessListener(any())).thenReturn(mockAddReservationTask);
        when(mockAddReservationTask.addOnFailureListener(any())).thenReturn(mockAddReservationTask);

        // Mock para update
        when(mockDocRef.update(anyMap())).thenReturn(mockVoidTask);
        when(mockDocRef.update(anyString(), any())).thenReturn(mockVoidTask);
        when(mockDocRef.delete()).thenReturn(mockVoidTask); // Asegura que delete() devuelve mockVoidTask
    }

    @After
    public void tearDown() {
        mockedLooper.close();
    }

    // Tests de lógica pura

    @Test
    public void testIsSpotAvailable_NoReservations_ReturnsTrue() {
        boolean available = dataRepository.isSpotAvailable(null, "2025-07-12T10:00:00+02:00", "2025-07-12T11:00:00+02:00", null);
        assertTrue(available);
    }

    @Test
    public void testIsSpotAvailable_EmptyReservations_ReturnsTrue() {
        boolean available = dataRepository.isSpotAvailable(new ArrayList<>(), "2025-07-12T10:00:00+02:00", "2025-07-12T11:00:00+02:00", null);
        assertTrue(available);
    }

    @Test
    public void testIsSpotAvailable_Overlap_ReturnsFalse() {
        List<Map<String, Object>> reservations = new ArrayList<>();
        Map<String, Object> res = new HashMap<>();
        res.put("startTime", "2025-07-12T10:30:00+02:00");
        res.put("endTime", "2025-07-12T11:30:00+02:00");
        reservations.add(res);
        boolean available = dataRepository.isSpotAvailable(reservations, "2025-07-12T11:00:00+02:00", "2025-07-12T12:00:00+02:00", null);
        assertFalse(available);
    }

    @Test
    public void testIsSpotAvailable_NoOverlap_ReturnsTrue() {
        List<Map<String, Object>> reservations = new ArrayList<>();
        Map<String, Object> res = new HashMap<>();
        res.put("startTime", "2025-07-12T08:00:00+02:00");
        res.put("endTime", "2025-07-12T09:00:00+02:00");
        reservations.add(res);
        boolean available = dataRepository.isSpotAvailable(reservations, "2025-07-12T09:30:00+02:00", "2025-07-12T10:00:00+02:00", null);
        assertTrue(available);
    }

    @Test
    public void testIsSpotAvailable_IgnoreReservationId() {
        List<Map<String, Object>> reservations = new ArrayList<>();
        Map<String, Object> res = new HashMap<>();
        res.put("reservationId", "abc");
        res.put("startTime", "2025-07-12T10:00:00+02:00");
        res.put("endTime", "2025-07-12T11:00:00+02:00");
        reservations.add(res);
        boolean available = dataRepository.isSpotAvailable(reservations, "2025-07-12T10:30:00+02:00", "2025-07-12T11:30:00+02:00", "abc");
        assertTrue(available);
    }

    @Test
    public void testTimeOverlaps_NoOverlap() {
        assertFalse(dataRepository.timeOverlaps("08:00", "09:00", "09:00", "10:00"));
        assertFalse(dataRepository.timeOverlaps("10:00", "11:00", "08:00", "10:00"));
    }

    @Test
    public void testTimeOverlaps_Overlap() {
        assertTrue(dataRepository.timeOverlaps("08:00", "10:00", "09:00", "11:00"));
        assertTrue(dataRepository.timeOverlaps("09:00", "11:00", "08:00", "10:00"));
        assertTrue(dataRepository.timeOverlaps("08:00", "12:00", "09:00", "10:00"));
    }

    @Test
    public void testIsDatabaseInitialized_True() throws InterruptedException {
        when(mockFirestore.collection("parking")).thenReturn(mockCollection);
        when(mockCollection.document("estado")).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockDocTask);

        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            when(mockDocSnap.exists()).thenReturn(true);
            when(mockDocSnap.contains("isInitialized")).thenReturn(true);
            when(mockDocSnap.getBoolean("isInitialized")).thenReturn(true);
            listener.onSuccess(mockDocSnap);
            return mockDocTask;
        }).when(mockDocTask).addOnSuccessListener(any());

        when(mockDocTask.addOnFailureListener(any())).thenReturn(mockDocTask);

        LiveData<Boolean> result = dataRepository.isDatabaseInitialized();
        Boolean value = LiveDataTestUtil.getValue(result);
        assertTrue(value);
    }

    @Test
    public void testIsDatabaseInitialized_False() throws InterruptedException {
        when(mockFirestore.collection("parking")).thenReturn(mockCollection);
        when(mockCollection.document("estado")).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockDocTask);

        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            when(mockDocSnap.exists()).thenReturn(false);
            listener.onSuccess(mockDocSnap);
            return mockDocTask;
        }).when(mockDocTask).addOnSuccessListener(any());

        when(mockDocTask.addOnFailureListener(any())).thenReturn(mockDocTask);

        LiveData<Boolean> result = dataRepository.isDatabaseInitialized();
        Boolean value = LiveDataTestUtil.getValue(result);
        assertFalse(value);
    }

    @Test
    public void testIsDatabaseInitialized_Error() throws InterruptedException {
        when(mockFirestore.collection("parking")).thenReturn(mockCollection);
        when(mockCollection.document("estado")).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockDocTask);

        when(mockDocTask.addOnSuccessListener(any())).thenReturn(mockDocTask);

        doAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new Exception("error"));
            return mockDocTask;
        }).when(mockDocTask).addOnFailureListener(any());

        LiveData<Boolean> result = dataRepository.isDatabaseInitialized();
        Boolean value = LiveDataTestUtil.getValue(result);
        assertFalse(value);
    }

    @Test
    public void testInitializeDatabase() {
        // Configurar WriteBatch mock
        when(mockFirestore.batch()).thenReturn(mockBatch);

        // Permitir que mockBatch.set() devuelva mockBatch para encadenamiento
        when(mockBatch.set(any(DocumentReference.class), any())).thenReturn(mockBatch);
        when(mockBatch.set(any(DocumentReference.class), any(), any(SetOptions.class))).thenReturn(mockBatch);

        when(mockBatch.commit()).thenReturn(mockVoidTask);

        // Mockear el listener de addOnSuccessListener
        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockVoidTask;
        }).when(mockVoidTask).addOnSuccessListener(any());

        // Mockear el listener de addOnFailureListener
        doAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            return mockVoidTask;
        }).when(mockVoidTask).addOnFailureListener(any());

        // No verificamos antes de llamar al método
        dataRepository.initializeDatabase();

        // Verificamos después para asegurar que se llamó al menos una vez
        verify(mockFirestore).batch();
        // Ya no verificamos el número exacto de veces que se llama a set()
        verify(mockBatch, atLeastOnce()).set(any(DocumentReference.class), any(), any(SetOptions.class));
        verify(mockBatch).commit();
    }

    @Test
    public void testCreateReservation_Success() {
        String parkingId = "parking1";
        String spotId = "spot1";
        String day = "2025-07-12";
        String startTime = "2025-07-12T10:00:00+02:00";
        String endTime = "2025-07-12T11:00:00+02:00";
        String userId = "user1";
        String type = "NORMAL";

        // Configurar el mock para collection().add()
        when(mockCollection.add(any(Map.class))).thenReturn(mockAddReservationTask);
//        when(mockAddReservationTask.getResult()).thenReturn(mockAddedDocRef);
        when(mockAddedDocRef.getId()).thenReturn("reserva123");

        DataRepository.OnReservationCompleteListener mockListener = mock(DataRepository.OnReservationCompleteListener.class);

        // Simular el callback de éxito de add
        doAnswer(invocation -> {
            OnSuccessListener<com.google.firebase.firestore.DocumentReference> listener = invocation.getArgument(0);
            listener.onSuccess(mockAddedDocRef);
            return mockAddReservationTask;
        }).when(mockAddReservationTask).addOnSuccessListener(any());

        // Bypasear la llamada a updateSpotWithReservation
//        doNothing().when(mockListener).onReservationSuccess(anyString(), anyString(), anyString());

        // Ejecutar el método a probar
        dataRepository.createReservation(parkingId, spotId, day, startTime, endTime, userId, type, mockListener);

        // Verificar que se haya llamado a collection("reservations").add()
        verify(mockCollection).add(any(Map.class));
    }

    @Test
    public void testGetUserReservations_Success() throws InterruptedException {
        String userId = "user1";

        when(mockFirestore.collection("reservations")).thenReturn(mockCollection);
        when(mockCollection.whereEqualTo("userId", userId)).thenReturn(mockCollection);
        when(mockCollection.get()).thenReturn(mockQueryTask);

        QueryDocumentSnapshot mockDoc = mock(QueryDocumentSnapshot.class);
        DocumentReference mockDocRef = mock(DocumentReference.class);
        List<DocumentSnapshot> docList = new ArrayList<>();
        docList.add(mockDoc);

        when(mockDoc.getString("day")).thenReturn("2025-07-12");
        when(mockDoc.getString("startTime")).thenReturn("2025-07-12T10:00:00+02:00");
        when(mockDoc.getString("endTime")).thenReturn("2025-07-12T11:00:00+02:00");
        when(mockDoc.getString("spotType")).thenReturn("NORMAL");
        when(mockDoc.getString("state")).thenReturn("Reservado");
        when(mockDoc.getString("spotId")).thenReturn("spot1");
        when(mockDoc.getString("userId")).thenReturn(userId);
        when(mockDoc.getId()).thenReturn("reserva123");
        when(mockDoc.getString("parkingId")).thenReturn("parking1");
        when(mockDoc.getReference()).thenReturn(mockDocRef);

        when(mockQuerySnap.getDocuments()).thenReturn(docList);
        when(mockQueryTask.isSuccessful()).thenReturn(true);
        when(mockQueryTask.getResult()).thenReturn(mockQuerySnap);

        doAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onComplete(mockQueryTask);
            return null;
        }).when(mockQueryTask).addOnCompleteListener(any());

        dataRepository.getUserReservations(userId);

        List<Reserva> result = LiveDataTestUtil.getValue(dataRepository.getReservationsLiveData());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("reserva123", result.get(0).getId());
        assertEquals(userId, result.get(0).getUsuarioId());
    }

    @Test
    public void testStoreWorkerId() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        String workerId = "worker123";
        String reservationId = "reserva123";
        String title = "notificationWorkerId1";

        when(mockFirestore.collection("reservations")).thenReturn(mockCollection);
        when(mockCollection.document(reservationId)).thenReturn(mockDocRef);

        List<Reserva> reservas = new ArrayList<>();
        Date fecha = new Date();
        Plaza plaza = new Plaza("spot1", TiposPlaza.NORMAL);
        Reserva reserva = new Reserva(fecha, "10:00", "11:00", plaza, "user1", EstadoReserva.Reservado, reservationId, "parking1");
        reservas.add(reserva);

        MutableLiveData<List<Reserva>> liveData = new MutableLiveData<>(reservas);

        java.lang.reflect.Field field = DataRepository.class.getDeclaredField("reservationsLiveData");
        field.setAccessible(true);
        field.set(dataRepository, liveData);

        dataRepository.storeWorkerId(workerId, reservationId, title);

        verify(mockDocRef).update(any(Map.class));

        List<Reserva> updatedReservas = LiveDataTestUtil.getValue(dataRepository.getReservationsLiveData());
        assertEquals(workerId, updatedReservas.get(0).getNotificationWorkerId1());
    }

    @Test
    public void testDeleteReservation_Success() {
        Date fecha = new Date();
        Plaza plaza = new Plaza("spot1", TiposPlaza.NORMAL);
        Reserva reserva = new Reserva(fecha, "10:00", "11:00", plaza, "user1", EstadoReserva.Reservado, "reserva123", "parking1");

        when(mockFirestore.collection("reservations")).thenReturn(mockCollection);
        when(mockCollection.document("reserva123")).thenReturn(mockDocRef);
        when(mockDocRef.delete()).thenReturn(mockVoidTask);

        DataRepository.OnReservationRemoveListener mockListener = mock(DataRepository.OnReservationRemoveListener.class);

        // Mock del comportamiento de delete completado con éxito
        doAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            when(mockVoidTask.isSuccessful()).thenReturn(true);
            listener.onComplete(mockVoidTask);
            return null;
        }).when(mockVoidTask).addOnCompleteListener(any());

        // Preparar datos en memoria para la reserva
        List<Reserva> reservas = new ArrayList<>();
        reservas.add(reserva);
        MutableLiveData<List<Reserva>> liveData = new MutableLiveData<>(reservas);

        try {
            java.lang.reflect.Field field = DataRepository.class.getDeclaredField("reservationsLiveData");
            field.setAccessible(true);
            field.set(dataRepository, liveData);
        } catch (Exception e) {
            fail("No se pudo inyectar el LiveData: " + e.getMessage());
        }

        // Mockear deleteSpotReservation directamente
        doAnswer(invocation -> {
            // Simular que la operación de update fue exitosa
            Task<Void> mockUpdateTask = mock(Task.class);
            //when(mockUpdateTask.addOnSuccessListener(any())).thenReturn(mockUpdateTask);
            when(mockUpdateTask.addOnFailureListener(any())).thenReturn(mockUpdateTask);

            // Ejecutar el callback de éxito
            doAnswer(innerInvocation -> {
                OnSuccessListener<Void> successListener = innerInvocation.getArgument(0);
                successListener.onSuccess(null);
                return mockUpdateTask;
            }).when(mockUpdateTask).addOnSuccessListener(any());

            return mockUpdateTask;
        }).when(mockDocRef).update(eq("reservations"), any(FieldValue.class));

        dataRepository.deleteReservation(reserva, mockListener);

        // Verificar que se llamó al método delete de la colección "reservations"
        verify(mockDocRef).delete();
    }

    @Test
    public void testEditReservation_Success() {
        String oldId = "reserva123";
        String oldDay = "2025-07-12";
        String oldStartTime = "2025-07-12T10:00:00+02:00";
        String oldEndTime = "2025-07-12T11:00:00+02:00";
        String oldSpotId = "spot1";
        String parkingId = "parking1";
        String newDay = "2025-07-13";
        String newStartTime = "2025-07-13T09:00:00+02:00";
        String newEndTime = "2025-07-13T10:00:00+02:00";
        Plaza newPlaza = new Plaza("spot2", TiposPlaza.NORMAL);

        // Configurar los mocks para la consulta de spots
        when(mockCollection.whereEqualTo("id", newPlaza.getId())).thenReturn(mockCollection);
        when(mockCollection.get()).thenReturn(mockQueryTask);

        // Configurar el mock para el documento de la plaza
        QueryDocumentSnapshot mockSpotDoc = mock(QueryDocumentSnapshot.class);
        List<DocumentSnapshot> spotDocs = new ArrayList<>();
        spotDocs.add(mockSpotDoc);

        // Configurar datos del spot
        Map<String, Object> spotData = new HashMap<>();
        spotData.put("id", newPlaza.getId());
        spotData.put("type", newPlaza.getType().toString());
        when(mockSpotDoc.getData()).thenReturn(spotData);
        //when(mockSpotDoc.getString("id")).thenReturn(newPlaza.getId());
        //when(mockSpotDoc.getString("type")).thenReturn(newPlaza.getType().toString());

        // Configurar QuerySnapshot
        when(mockQuerySnap.isEmpty()).thenReturn(false);
        //when(mockQuerySnap.getDocuments()).thenReturn(spotDocs);
        doReturn(spotDocs.iterator()).when(mockQuerySnap).iterator();
        //when(mockQueryTask.isSuccessful()).thenReturn(true);
        //when(mockQueryTask.getResult()).thenReturn(mockQuerySnap);

        // Configurar listener mock
        DataRepository.OnReservationCompleteListener mockListener = mock(DataRepository.OnReservationCompleteListener.class);

        // Simular la ejecución de addOnSuccessListener
        doAnswer(invocation -> {
            OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockQuerySnap);
            return mockQueryTask;
        }).when(mockQueryTask).addOnSuccessListener(any());

        // Mock para el documento de reserva y task de update
        Task<Void> mockUpdateTask = mock(Task.class);
        when(mockDocRef.update(anyString(), any(), anyString(), any(), anyString(), any(),
                               anyString(), any(), anyString(), any())).thenReturn(mockUpdateTask);
        //when(mockUpdateTask.addOnSuccessListener(any())).thenReturn(mockUpdateTask);

        // Simular éxito en la actualización
        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockUpdateTask;
        }).when(mockUpdateTask).addOnSuccessListener(any());

        // Ejecutar el método
        dataRepository.editReservation(
                oldId, newDay, newEndTime, parkingId, newPlaza, newStartTime,
                oldSpotId, oldDay, oldStartTime, oldEndTime, mockListener);

        // Verificaciones
        verify(mockCollection).whereEqualTo("id", newPlaza.getId());
        verify(mockQueryTask).addOnSuccessListener(any());
    }

    @Test
    public void testGetParkingSpots() throws Exception {
        String parkingId = "parking1";
        String day = "12/07/2025";
        String startTime = "10:00";
        String endTime = "11:00";

        when(mockFirestore.collection("parking")).thenReturn(mockCollection);
        when(mockCollection.document(parkingId)).thenReturn(mockDocRef);
        when(mockDocRef.collection("parkingSpots")).thenReturn(mockCollection);
        when(mockCollection.get()).thenReturn(mockQueryTask);

        QueryDocumentSnapshot mockSpot1 = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot mockSpot2 = mock(QueryDocumentSnapshot.class);
        List<DocumentSnapshot> spots = new ArrayList<>();
        spots.add(mockSpot1);
        spots.add(mockSpot2);

        // Configurar datos de los spots
        when(mockSpot1.getString("id")).thenReturn("spot1");
        when(mockSpot1.getString("type")).thenReturn("NORMAL");
        when(mockSpot1.get("reservations")).thenReturn(null);

        when(mockSpot2.getString("id")).thenReturn("spot2");
        when(mockSpot2.getString("type")).thenReturn("ELECTRICO");

        List<Map<String, Object>> reservationsSpot2 = new ArrayList<>();
        Map<String, Object> reserva = new HashMap<>();
        reserva.put("day", "2025-07-12");
        reserva.put("startTime", "2025-07-12T09:30:00+02:00");
        reserva.put("endTime", "2025-07-12T10:30:00+02:00");
        reserva.put("reservationId", "reserva123");
        reservationsSpot2.add(reserva);
        when(mockSpot2.get("reservations")).thenReturn(reservationsSpot2);

        when(mockQuerySnap.getDocuments()).thenReturn(spots);
        when(mockQueryTask.isSuccessful()).thenReturn(true);
        when(mockQueryTask.getResult()).thenReturn(mockQuerySnap);

        doAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onComplete(mockQueryTask);
            return null;
        }).when(mockQueryTask).addOnCompleteListener(any());

        dataRepository.getParkingSpots(parkingId, day, startTime, endTime, null);

        List<Plaza> plazas = LiveDataTestUtil.getValue(dataRepository.getPlazasLiveData());
        assertNotNull(plazas);
        assertEquals(2, plazas.size());

        assertEquals("spot1", plazas.get(0).getId());
        assertEquals(TiposPlaza.NORMAL, plazas.get(0).getType());
        assertTrue(plazas.get(0).isAvailable());

        assertEquals("spot2", plazas.get(1).getId());
        assertEquals(TiposPlaza.ELECTRICO, plazas.get(1).getType());
    }
}

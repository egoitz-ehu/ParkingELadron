package com.lksnext.ParkingELadron.view.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;
import com.lksnext.ParkingELadron.view.activity.SelectParkingSpotActivity;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RunWith(AndroidJUnit4.class)
public class CrearFragmentTest {

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testFillDataAndOpenSpotSelector() {
        FragmentScenario<CrearFragment> scenario = FragmentScenario.launchInContainer(
                CrearFragment.class,
                null,  // Bundle de argumentos
                R.style.Theme_ParkingELadron  // Tema
        );

        scenario.onFragment(fragment -> {
        });

        onView(withId(R.id.cvDia)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.cvHoraInicio)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.cvHoraSalida)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.btnSeleccionar)).perform(click());

        intended(hasComponent(SelectParkingSpotActivity.class.getName()));
    }

    @Test
    public void testEmptyDataAndNotOpenSpotSelector() {
        FragmentScenario<CrearFragment> scenario = FragmentScenario.launchInContainer(
                CrearFragment.class,
                null,  // Bundle de argumentos
                R.style.Theme_ParkingELadron  // Tema
        );

        scenario.onFragment(fragment -> {
        });

        onView(withId(R.id.btnSeleccionar)).perform(click());

        intended(Matchers.not(hasComponent(SelectParkingSpotActivity.class.getName())));
    }

    private class FakeRepository extends DataRepository {

        private MutableLiveData<Reserva> reservaMutableLiveData = new MutableLiveData<>();

        public FakeRepository() {
            super(null); // Pasar un contexto falso o nulo
        }

        @Override
        public void createReservation(String parkingId, String spotId, String day, String startTime, String endTime, String userId, String type, OnReservationCompleteListener listener) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                this.reservaMutableLiveData.setValue(new Reserva(format.parse(day), startTime, endTime, new Plaza(spotId, TiposPlaza.valueOf(type)), userId, EstadoReserva.Reservado,null, parkingId));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            listener.onReservationSuccess("a","a","a");
        }
    }

    @Test
    public void testCrearReservaExitosaMuestraToastYReseteaFormulario() {
        FragmentScenario<CrearFragment> scenario = FragmentScenario.launchInContainer(
                CrearFragment.class,
                null,  // Bundle de argumentos
                R.style.Theme_ParkingELadron  // Tema
        );

        scenario.onFragment(fragment -> {
            Plaza plaza = new Plaza(
                    "P1", TiposPlaza.NORMAL
            );
            DataRepository repository = new FakeRepository();
            fragment.getViewModel().setDataRepository(repository);
            fragment.getViewModel().setPlazaSeleccionada(plaza);
        });

        onView(withId(R.id.cvDia)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.cvHoraInicio)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.cvHoraSalida)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.btnCrear)).perform(click());

        onView(withId(R.id.tvDia))
                .check(matches(withText(R.string.crear_selectDia)));
        onView(withId(R.id.tvHoraInicio))
                .check(matches(withText(R.string.crear_selectHoraEntrada)));
        onView(withId(R.id.tvHoraSalida))
                .check(matches(withText(R.string.crear_selectHoraSalida)));
    }

    @Test
    public void testCrearReservaErrorDatosNoValidos() {
            FragmentScenario<CrearFragment> scenario = FragmentScenario.launchInContainer(
                    CrearFragment.class,
                    null,  // Bundle de argumentos
                    R.style.Theme_ParkingELadron  // Tema
            );

            scenario.onFragment(fragment -> {
            });

            onView(withId(R.id.cvDia)).perform(click());
            onView(withId(android.R.id.button1)).perform(click());

            onView(withId(R.id.cvHoraInicio)).perform(click());
            onView(withId(android.R.id.button1)).perform(click());

            onView(withId(R.id.cvHoraSalida)).perform(click());
            onView(withId(android.R.id.button1)).perform(click());

            onView(withId(R.id.btnCrear)).perform(click());

            onView(withId(R.id.tvDia))
                    .check(matches(Matchers.not(withText(R.string.crear_selectDia))));
            onView(withId(R.id.tvHoraInicio))
                    .check(matches(Matchers.not(withText(R.string.crear_selectHoraEntrada))));
            onView(withId(R.id.tvHoraSalida))
                    .check(matches(Matchers.not(withText(R.string.crear_selectHoraSalida))));
    }

    private android.app.Activity getActivity(FragmentScenario<?> scenario) {
        final android.app.Activity[] activity = new android.app.Activity[1];
        scenario.onFragment(fragment -> activity[0] = fragment.requireActivity());
        return activity[0];
    }
}
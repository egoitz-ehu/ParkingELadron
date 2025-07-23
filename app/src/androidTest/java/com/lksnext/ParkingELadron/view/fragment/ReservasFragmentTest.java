package com.lksnext.ParkingELadron.view.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lksnext.ParkingELadron.R;
import com.lksnext.ParkingELadron.data.DataRepository;
import com.lksnext.ParkingELadron.domain.DateUtil;
import com.lksnext.ParkingELadron.domain.EstadoReserva;
import com.lksnext.ParkingELadron.domain.Plaza;
import com.lksnext.ParkingELadron.domain.Reserva;
import com.lksnext.ParkingELadron.domain.TiposPlaza;
import com.lksnext.ParkingELadron.view.activity.SelectParkingSpotActivity;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ReservasFragmentTest {

    private class FakeRepository extends DataRepository {
        private MutableLiveData<List<Reserva>> fakeReservaLiveData = new MutableLiveData<>();

        public FakeRepository() {
            super(null);
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
                Reserva r = new Reserva(format.parse("2024.06.10"), "2024-06-10T10:00:00Z", "2024-06-10T12:00:00Z", new Plaza("1", TiposPlaza.NORMAL, true), "userId", EstadoReserva.Reservado, "id", "parkingId");
                List<Reserva> reservas = new ArrayList<>();
                reservas.add(r);
                fakeReservaLiveData.setValue(reservas);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        public LiveData<List<Reserva>> getReservationsLiveData() {
            return fakeReservaLiveData;
        }
    }

    @Test
    public void testOpenReservaDialog() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        FragmentScenario<ReservasFragment> scenario = FragmentScenario.launchInContainer(
                ReservasFragment.class, null, R.style.Theme_ParkingELadron);

        final RecyclerView[] recyclerView = new RecyclerView[1];
        final RecyclerViewIdlingResource[] idlingResource = new RecyclerViewIdlingResource[1];

        scenario.onFragment(fragment -> {
            DataRepository repository = new FakeRepository();
            fragment.getViewModel().setDataRepository(repository);
            recyclerView[0] = fragment.getView().findViewById(R.id.recyclerViewReservas);
            idlingResource[0] = new RecyclerViewIdlingResource(recyclerView[0]);
            IdlingRegistry.getInstance().register(idlingResource[0]);
        });

        onView(withId(R.id.recyclerViewReservas))
                .check(matches(isDisplayed()));

        onView(withId(R.id.recyclerViewReservas))
                .perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.cvReservaDialog))
                .check(matches(isDisplayed()));

        SimpleDateFormat format2 = new SimpleDateFormat("yyyy.MM.dd");

        onView(withId(R.id.tvSpot))
                .check(matches(withText("Spot P" + 1)));

        IdlingRegistry.getInstance().unregister(idlingResource[0]);
    }

    private class RecyclerViewIdlingResource implements IdlingResource {
        private final RecyclerView recyclerView;
        private ResourceCallback resourceCallback;
        private boolean isIdle;

        public RecyclerViewIdlingResource(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            this.isIdle = false;
        }

        @Override
        public String getName() {
            return RecyclerViewIdlingResource.class.getName();
        }

        @Override
        public boolean isIdleNow() {
            if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
                if (!isIdle) {
                    isIdle = true;
                    if (resourceCallback != null) {
                        resourceCallback.onTransitionToIdle();
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            this.resourceCallback = callback;
        }
    }
}

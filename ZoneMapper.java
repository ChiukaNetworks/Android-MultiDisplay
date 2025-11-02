import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.occupantzone.CarOccupantZoneManager;
import android.car.occupantzone.CarOccupantZoneManager.OccupantZoneInfo;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;
import java.util.ArrayList;
import java.util.List;

public class ZoneMapper {

    private final Context mContext;
    private final CarOccupantZoneManager mCzm;
    private final DisplayManager mDm;

    public static class ZoneDisplay {
        public final OccupantZoneInfo zone;
        public final Display display;

        public ZoneDisplay(OccupantZoneInfo zone, Display display) {
            this.zone = zone;
            this.display = display;
        }
    }

    public ZoneMapper(Context context) {
        mContext = context;
        Car car = Car.createCar(context);
        mCzm = (CarOccupantZoneManager) car.getCarManager(Car.CAR_OCCUPANT_ZONE_SERVICE);
        mDm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    }

    public List<ZoneDisplay> map() {
        List<ZoneDisplay> result = new ArrayList<>();

        try {
            List<OccupantZoneInfo> zones = mCzm.getAllOccupantZones();
            for (OccupantZoneInfo zone : zones) {

                // Try MAIN display for that occupant zone
                Display display = mCzm.getDisplayForOccupant(zone,
                        CarOccupantZoneManager.DISPLAY_TYPE_MAIN);

                // Fallback to cluster display if MAIN not found
                if (display == null) {
                    display = mCzm.getDisplayForOccupant(zone,
                            CarOccupantZoneManager.DISPLAY_TYPE_INSTRUMENT_CLUSTER);
                }

                if (display != null) {
                    result.add(new ZoneDisplay(zone, display));
                }
            }

        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }

        return result;
    }

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;

public final class LaunchUtils {

    private LaunchUtils() {}

    public static void launchOn(Context context, int displayId, Intent intent) {
        ActivityOptions opts = ActivityOptions.makeBasic();
        // Optional: enable adjacent/multi-window if you need it on large panels
        // opts.setLaunchWindowingMode(WindowConfiguration.WINDOWING_MODE_MULTI_WINDOW);
        opts.setLaunchDisplayId(displayId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent, opts.toBundle());
    }
}

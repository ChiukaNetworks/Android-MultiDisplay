CompatHostActivity — TaskView/ActivityView embedding variant
Note: TaskView is a system API on AAOS (com.android.wm.shell.taskview.TaskView). If you’re building a priv-app/system app, this applies. If not, use ActivityView from the public SDK where available.
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;

// If you have system access, import the platform TaskView:
// import com.android.wm.shell.taskview.TaskView;
// import com.android.wm.shell.taskview.TaskViewListener;

// If using ActivityView (public), swap types accordingly.

public class CompatTaskViewHostActivity extends Activity {

    private Object taskView; // replace with TaskView/ActivityView type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate TaskView/ActivityView
        // taskView = new TaskView(this);
        // setContentView((View) taskView);

        ComponentName component = new ComponentName("com.example", "com.example.MainActivity");
        int launchDisplayId = (getDisplay() != null) ? getDisplay().getDisplayId() : Display.DEFAULT_DISPLAY;

        // If using TaskView:
        // ((TaskView) taskView).initialize(new TaskViewListener() { /* handle lifecycle */ });

        ActivityOptions opts = ActivityOptions.makeBasic();
        opts.setLaunchDisplayId(launchDisplayId);

        Intent i = new Intent().setComponent(component);
        // ((TaskView) taskView).startActivity(i, opts.toBundle());

        // If using ActivityView instead, use its API to start the activity in the embedded container.
    }
}

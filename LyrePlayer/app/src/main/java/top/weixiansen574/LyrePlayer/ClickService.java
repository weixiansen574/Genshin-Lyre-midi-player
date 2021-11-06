package top.weixiansen574.LyrePlayer;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

public class ClickService extends AccessibilityService {
    public static ClickService mService;

    @RequiresApi(api = Build.VERSION_CODES.O)

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;
    }
    public static boolean isStart() {
        return mService != null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
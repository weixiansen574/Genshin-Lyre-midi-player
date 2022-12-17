package top.weixiansen574.LyrePlayer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import top.weixiansen574.LyrePlayer.ClickService;
import top.weixiansen574.LyrePlayer.R;

public class AccessibilityUtil {
    public static boolean checkPermission(final Context context) {
        if (!ClickService.isStart()) {
            new AlertDialog.Builder(context).setTitle(R.string.Accessibility_required).setMessage(R.string.Accessibility_required_msg).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    } catch (Exception e) {
                        context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                        e.printStackTrace();
                    }
                }
            }).setNeutralButton("使用root自动开启无障碍", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (ShellUtils.execCommand(new String[]{
                            "settings put secure enabled_accessibility_services " + context.getPackageName() + "/" + context.getPackageName() + ".ClickService",
                            "settings put secure accessibility_enabled 1",
                            "settings put secure enabled_accessibility_services \"\"",//关闭再开启，保证能成功
                            "settings put secure enabled_accessibility_services " + context.getPackageName() + "/" + context.getPackageName() + ".ClickService",
                            "settings put secure accessibility_enabled 1",
                    }, true).result == 0) {
                                Toast.makeText(context, "已使用root开启无障碍,请再次点击“启动悬浮窗”以打开悬浮窗", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "你的手机没有root权限或拒绝了授权", Toast.LENGTH_LONG).show();
                    }
                }
            }).show();
            return false;
        } else {
            return true;
        }
    }
}

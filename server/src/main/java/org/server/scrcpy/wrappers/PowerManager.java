package org.server.scrcpy.wrappers;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.IInterface;
import org.server.scrcpy.Ln;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public final class PowerManager {
    private final IInterface manager;
    private final Method isScreenOnMethod;
    private Method goToSleepMethod;
    private Method wakeUpMethod;

    public PowerManager(IInterface manager) {
        this.manager = manager;
        try {
            @SuppressLint("ObsoleteSdkInt") // we may lower minSdkVersion in the future
                    String methodName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH ? "isInteractive" : "isScreenOn";
            isScreenOnMethod = manager.getClass().getMethod(methodName);
            
            // 获取 goToSleep 和 wakeUp 方法
            try {
                goToSleepMethod = manager.getClass().getMethod("goToSleep", long.class);
            } catch (NoSuchMethodException e) {
                Ln.w("Could not find goToSleep method");
            }
            try {
                wakeUpMethod = manager.getClass().getMethod("wakeUp", long.class);
            } catch (NoSuchMethodException e) {
                Ln.w("Could not find wakeUp method");
            }
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public boolean isScreenOn() {
        try {
            return (Boolean) isScreenOnMethod.invoke(manager);
        } catch (InvocationTargetException | IllegalAccessException e) {
            // throw new AssertionError(e);
            return false;
        }
    }
    
    public void goToSleep() {
        if (goToSleepMethod != null) {
            try {
                goToSleepMethod.invoke(manager, System.currentTimeMillis());
                Ln.i("Screen turned off successfully");
            } catch (InvocationTargetException | IllegalAccessException e) {
                Ln.e("Could not turn off screen", e);
            }
        } else {
            Ln.w("goToSleep method not available");
        }
    }
    
    public void wakeUp() {
        if (wakeUpMethod != null) {
            try {
                wakeUpMethod.invoke(manager, System.currentTimeMillis());
                Ln.i("Screen turned on successfully");
            } catch (InvocationTargetException | IllegalAccessException e) {
                Ln.e("Could not turn on screen", e);
            }
        } else {
            Ln.w("wakeUp method not available");
        }
    }
}
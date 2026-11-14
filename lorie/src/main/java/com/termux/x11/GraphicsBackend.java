package com.termux.x11;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.system.Os;
import android.util.Log;

/** Optional Android GLES-on-Vulkan selection for the command-line server process. */
final class GraphicsBackend {
    private static final String ENVIRONMENT = "TERMUX_X11_RENDERER";

    static boolean wantsAngle() {
        return "angle-vulkan".equals(System.getenv(ENVIRONMENT));
    }

    static void configure(Context systemContext) {
        String backend = System.getenv(ENVIRONMENT);
        if (backend == null || backend.isEmpty() || "native".equals(backend))
            return;
        if (!wantsAngle())
            throw new IllegalArgumentException(ENVIRONMENT + " must be native or angle-vulkan");
        if (Build.VERSION.SDK_INT < 29)
            throw new IllegalStateException("angle-vulkan requires Android 10 or newer and a system ANGLE driver");

        try {
            // app_process does not perform an application's GraphicsEnvironment setup.
            // Supply process-local settings instead of changing Settings.Global for the device.
            Context appContext = systemContext.createPackageContext(BuildConfig.APPLICATION_ID, 0);
            Bundle settings = new Bundle();
            settings.putString("angle_gl_driver_selection_pkgs", BuildConfig.APPLICATION_ID);
            settings.putString("angle_gl_driver_selection_values", "angle");
            Os.setenv("ANGLE_DEFAULT_PLATFORM", "vulkan", true);

            // This framework API is hidden and varies between Android releases. Keep it
            // behind an explicit opt-in, and report failures instead of pretending to use ANGLE.
            Class<?> type = Class.forName("android.os.GraphicsEnvironment");
            Object graphics = type.getMethod("getInstance").invoke(null);
            type.getMethod("setLayerPaths", ClassLoader.class, String.class, String.class)
                    .invoke(graphics, CmdEntryPoint.class.getClassLoader(), "", "");
            type.getMethod("setup", Context.class, Bundle.class).invoke(graphics, appContext, settings);
            Log.i("X11Graphics", "Requested ANGLE Vulkan; check gles-renderer GL renderer for the active driver");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to configure ANGLE Vulkan. Unset " + ENVIRONMENT
                    + " to restore the native renderer.", e);
        }
    }

    private GraphicsBackend() {}
}

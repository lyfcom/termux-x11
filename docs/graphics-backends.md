# Graphics backends in this fork

This fork includes upstream PR [#1085](https://github.com/termux/termux-x11/pull/1085),
which displays the pointer in a separate Android surface. Plain cursor movement
can then avoid submitting a complete desktop GL frame. This is independent of
the graphics API used by applications inside Debian.

## Optional ANGLE Vulkan renderer

The normal renderer is unchanged. To request the Android system ANGLE driver for
a new X11 server process, run:

```sh
TERMUX_X11_RENDERER=angle-vulkan termux-x11 :0
```

Stop the existing server before reusing its display number. Stopping a desktop's
X server closes its clients, so save work first. This setting takes effect at
process startup; changing it cannot switch an already running server.

To return to the native Android GLES driver, omit the variable or use
`TERMUX_X11_RENDERER=native`. No global Android settings are changed. The ANGLE
setup uses a hidden Android framework API, so it is experimental, requires
Android 10 or newer, and needs a compatible installed system ANGLE driver.

This is GLES translated to Vulkan by Android ANGLE, rather than a new native
Vulkan renderer. It does not switch Debian's Mesa driver or select Turnip.
Look for the actual driver in the server's Android logs:

```sh
logcat -d -s X11Graphics:I gles-renderer:D ANGLE:I
```

The `GL renderer` line should contain `ANGLE` and `Vulkan`. A successful setup
request alone does not establish that the platform loaded the requested driver.

## Initial device checks

On Android 16 with Adreno 830, a standalone ANGLE probe selected the system
Vulkan driver and passed a red-pixel render/readback check. An isolated X11
display using the same GraphicsEnvironment setup loaded `libEGL_angle.so`,
`libGLESv2_angle.so` and `vulkan.adreno.so`; its log identified ANGLE on Vulkan,
and `xdpyinfo` connected successfully. This confirms initialization and X11
connectivity. It does not measure pointer latency or validate all desktop/game
presentation paths. Full visual and latency comparisons remain device tests.

The final signed build at `1189a537343fab54e12d4259cf82968da77e953a` was then
started directly from its APK on isolated displays, with both the default native
backend and `TERMUX_X11_RENDERER=angle-vulkan`. Both passed `xdpyinfo` and a Debian
`glxinfo -B` application-context check. The final APK's own renderer log confirmed
`ANGLE (Qualcomm, Vulkan 1.3.284 (Adreno (TM) 830 ...))` in ANGLE mode. Its APK and
companion packages passed the workflow's certificate check and local SHA-256
verification. The test servers were stopped without replacing the installed app
or restarting the user's existing display.

Signed artifacts: [GitHub Actions build](https://github.com/lyfcom/termux-x11/actions/runs/43613230211).

The signed baseline without the cursor patch is tagged
`signed-baseline-20261114`, for comparison and rollback. Use matching APK and
companion packages as described in [fork-builds.md](fork-builds.md).

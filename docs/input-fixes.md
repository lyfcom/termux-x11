# Input fixes

Hardware wheel events use 120 units per Android wheel-axis unit, matching the
server's division by 120. Fractional scroll values remain supported.

The X server patch also backports upstream scroll-button emulation fixes
[0a22502c](https://gitlab.freedesktop.org/xorg/xserver/-/commit/0a22502c34f2ea9799a67386498f657d769c7af8)
and [d2158d40](https://gitlab.freedesktop.org/xorg/xserver/-/commit/d2158d4063dd87e630ca4c853ac07b88f3662c8f).
Without these, a fractional scroll remainder can swallow the first whole wheel
step in the opposite direction. Button events now follow crossings of increment
multiples. The second fix preserves support for negative increments.

A focused check of the actual old and patched C functions reproduces the lost
reverse detent and verifies its delivery after the backport, with both increment
signs. Device/game behavior still needs a user trial.

This native fix requires restarting the X server; updating the APK alone leaves
the running server unchanged. Restarting closes the desktop's applications.

# lyfcom builds

The **Build APK and companion** workflow builds the standalone Android APK and
its matching Termux companion packages. Pushes to `master` and manual runs use
the persistent private signing key. Pull request runs use the upstream public
test key and never receive signing secrets. Builds are uploaded as Actions
artifacts with their commit, certificate digest and SHA-256 checksums.

The private key is held in Actions Secrets, outside the source tree:

- `X11_KEYSTORE_BASE64`: base64-encoded PKCS12 keystore
- `X11_KEYSTORE_PASSWORD`: keystore password
- `X11_KEY_ALIAS`: key alias
- `X11_KEY_PASSWORD`: private key password

The public certificate is `signing-certificate.pem`. Its SHA-256 fingerprint is
`FE:C7:55:E6:6A:C8:B1:03:6D:46:40:51:51:4E:9D:FB:0A:5E:BE:0F:85:F8:24:A2:10:D1:70:CA:16:58:D8:39`.
Signed builds fail if the secrets are missing or the APK certificate differs.
Back up the keystore and passwords privately; GitHub cannot export secret values.

Use the APK and companion `.deb` from the same artifact. The companion validates
the Android application's certificate, so the upstream companion cannot launch
this privately signed APK. The new certificate also prevents installing this APK
as an update over an upstream-signed installation. Preserve the existing app's
data/settings and original APK before choosing to replace it. These builds do
not install themselves or modify an existing desktop session.

Local builds can set `X11_KEYSTORE_PATH`, `X11_KEYSTORE_PASSWORD`, `X11_KEY_ALIAS`
and `X11_KEY_PASSWORD` to use a private PKCS12 key. Set `X11_REQUIRE_SIGNING=true`
to prohibit falling back to the upstream test key. Never commit private keys.

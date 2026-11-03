# X to Any

An Android app that intercepts "Post on X" (Tweet) links and hands the text to any app you like, such as Bluesky or a Nostr client, via the share sheet.

## Supported links

| Link | Text taken from |
| --- | --- |
| `https://x.com/intent/post`, `https://x.com/intent/tweet` | `text`, `hashtags`, `url` |
| `https://twitter.com/share` | `text`, `hashtags`, `url` |
| `https://x.com/compose/post`, `https://x.com/compose/tweet` | `text`, `hashtags`, `url` |
| `https://twitter.com/home?status=...` | `status` |
| `twitter://post?message=...` | `message` |

`twitter.com`, `x.com`, and their `www.` / `mobile.` subdomains are handled.
The `via` parameter is dropped because an X account name is meaningless on other services.

Any other X link (posts, profiles, ...) is forwarded to the X app, or to the default browser if the X app is not installed.

## Setup

Android 12 and later only open links in apps that are verified or user-approved for the domain, and the X app is verified for `x.com` and `twitter.com`.
Therefore you need to move link handling from X to this app:

1. Open X to Any and tap "Open link settings for X", then set X to open its links in the browser instead of the app.
   The wording depends on the device: turn off "Open supported links", or switch from opening links in the app to opening them in the browser.
2. Back in X to Any, tap "Open link settings for this app", then add all the listed links.

The settings screen shows whether all the links are enabled.

On Android 11 and earlier, the system shows an app chooser for these links instead.

## Choosing the app to post with

The settings screen (the launcher icon) lets you pick the app to post with.
The X app itself is not listed.
"Choose every time" shows the system share sheet.
The settings screen never appears when a link is opened.

## Build

```sh
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Requires an Android SDK (`ANDROID_HOME`) and JDK 17 or later.

The debug build has the application ID `io.github.thinca.xtoany.debug`, so it can be installed alongside the release build.
Only one of them can be approved for the X links at a time.

## Release

Releases are built and signed by GitHub Actions (`.github/workflows/release.yml`).
Bump `versionCode` and `versionName` in `app/build.gradle.kts`, then push a tag `v<versionName>`:

```sh
git tag v0.1.0
git push origin v0.1.0
```

The workflow requires these repository secrets:

| Secret | Value |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | The PKCS12 keystore encoded with `base64 -w0` |
| `RELEASE_KEYSTORE_PASSWORD` | The keystore password, also used as the key password |
| `RELEASE_KEY_ALIAS` | The key alias |

To build a signed release locally, set `RELEASE_KEYSTORE_PATH`, `RELEASE_KEYSTORE_PASSWORD`, and `RELEASE_KEY_ALIAS` and run `./gradlew assembleRelease`.

# STOP-INSTALLING-BLOATWARE

## My dad won't stop installing & letting his carrier (Metro T-Mobile) install junk bloatware, spam, and malware apps on every phone he touches.

> [!CAUTION] 
> All of you who despise the very sight of an AI agent touching anything should shield your eyes and turn the other way.
>
> Also go away paige. I dont know how you would get here but go away.

This is a fully slopped Android app that blocks newly installed apps until they're approved with a PIN. Built for a Samsung phone where the carrier (Metro by T-Mobile) silently pushes junk apps.



Disclaimer for the third time in case you cant read: this is fully slopped with absolutely no human involvement except this readme that i'm writing right now.

From my brief testing this blocks both play store and sideloaded APKs (idk which one he was doing to get that much shit on his phone) and shows an overlay app thru an accessibility service that will encourage him to uninstall the app or it'll let him keep it if he really wants it (hopefully hes not brain damaged enough to do that tho, we'll see)

If you have a dad like mine then you should set it to a pin that you'll remember but he won't.

Future plans are to basically make it like reverse parental controls but for old non-tech savvy parents. That's if I have to ever touch his phone again.

The rest is what Claude Opus 5.5 xhigh Ultracode wrote. Because honestly i cba. dad doesnt seem to ever learn :(

bye reader!

-----


- On setup, every app already on the phone is approved.
- Any app installed later (carrier push, Play Store, APK) gets a full-screen cover right after install and every time it's opened, with a big **Remove this app** button. **Not now** asks "Are you sure?" first.
- Preinstalled system apps are never blocked. Apps installed by Samsung's region installer (`omcagent`) are auto-approved.
- No launcher icon. To manage, tap the faint **Manage** link on any block screen, or run `adb shell am start -n dev.installguard/.MainActivity`. Enter the PIN to approve, revoke, uninstall, or change the PIN.

## Install (USB + adb)

1. On the phone: Settings > About phone > Software information > tap **Build number** 7 times, then Settings > Developer options > turn on **USB debugging**.
2. Plug it in and accept the prompt on the phone.
3. Run `.\install.ps1`. It installs the app, turns on the accessibility service, makes it a device admin (so it can't be uninstalled directly), and exempts it from battery optimization.
4. Set a PIN on the phone.

Installing over adb avoids Android's "restricted settings" lock on sideloaded accessibility services.

## Limits

Someone who knows where to look can turn off the accessibility service (Settings > Accessibility > Installed apps) or the device admin, which disables the protection.

## Build

`.\gradlew.bat assembleDebug` (needs JDK 17+ and an Android SDK with platform 37, set in `local.properties`).

# Scorller 🏠

Swipe it out with your crew.

## Why this exists

Every group trip ends up with the same problem: someone drops 30 Airbnb links in the group chat, everyone has an opinion, nobody agrees, and the thread turns into chaos. Scorller fixes that.

You create a session for the trip, everyone shares their favorite Airbnbs into it, and then you all swipe left or right on each one, Tinder style. The app tallies the votes live, so instead of scrolling through 30 links arguing in a group chat, you end up with a clean ranked list and can see at a glance which 3 to 5 places everyone actually likes.

## How it works

1. **Create a session.** One person starts a session (e.g. "Lisbon trip") and gets a short join code.
2. **Everyone joins.** Friends enter the code, or you just share the app's own share-sheet link straight from Airbnb.
3. **Add the options.** Paste an Airbnb link and the app pulls in the title, description, and photos automatically. You can also add things manually.
4. **Swipe.** Everyone goes through the deck on their own phone: swipe right to vote yes, left to pass. Tap a photo to see the full gallery first.
5. **Check the results.** A live-updating leaderboard shows each listing's yes count, ranked, with a badge for anything the whole group agreed on unanimously.

No accounts, no sign-up. Each phone gets an anonymous local identity used only to label votes within a session.

## Features

- Create or join sessions with a short code, or via Android's native Share sheet
- Automatic link preview (title, description, and full photo gallery) for supported sites
- A real swipe-deck UI with drag gestures, like/nope stamps, and a full-screen photo viewer
- Live results that update in real time as votes come in, with ranking and an "unanimous" badge
- A list of your recent sessions on the home screen so you can jump between multiple trips (Airbnbs, restaurants, activities, anything you need the group to vote on)

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- Firebase Firestore for live sync across devices
- Coil for image loading, Jsoup for on-device link scraping (no backend server)
- DataStore for local, anonymous per-device identity

## Setup

This project needs its own Firebase project to run:

1. Create a project at [console.firebase.google.com](https://console.firebase.google.com).
2. Register an Android app with package name `com.scorller`.
3. Download the generated `google-services.json` and place it at `app/google-services.json` (this file is gitignored and won't be committed).
4. Enable Cloud Firestore (Native mode). Since there's no login system, the security rules allow open read/write on the sessions/items/participants paths, so test mode is fine to start with.
5. Build and run from Android Studio, or `./gradlew assembleDebug`.

## Known limitations

Link preview works well for Airbnb, since its pages expose enough data to scrape without running JavaScript. Sites with heavier bot protection (Booking.com) or that require a login to see post data (Instagram) will fall back to manual entry: you can still add the title, notes, and a photo URL yourself.

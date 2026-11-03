# Crashing Activity and Lifecycle

This project demonstrates Android Activity launch modes and lifecycle behaviors.

## Features
- Implementation of `Standard`, `SingleTop`, `SingleTask`, and `SingleInstance` activities.
- Visual logs showing Task IDs and instance hashes.
- Demonstrates `onNewIntent` usage.

## How to use
Run the app and use the buttons to launch different activities. Check the logs under the tag `LaunchModes` or view the on-screen information to see how activities are managed within tasks.

# Android Launch Modes, Tasks and onNewIntent: Simple Notes

## 1. Task = a browser tab

- **Task** = one browser tab (a back stack of screens)
- **Activity** = one page in that tab's history
- **Back button** = go back one page in the current tab
- **Recents screen** = the tab switcher
- Every task has a `taskId`. Same ID means same tab.

By default, `startActivity()` pushes the new screen onto the **caller's task**. That is why most launches in this project show the same task ID.

> If your activity is opened from another app without any task instruction, it lives inside *that app's* task. Close the other app and your screen goes with it. To avoid that, use `singleTask` (with a different `taskAffinity`) or `FLAG_ACTIVITY_NEW_TASK` so it gets its own task.

## 2. The four launch modes

| Mode | Plain English | Typical use |
|---|---|---|
| `standard` | Always make a new copy | Product detail, forms |
| `singleTop` | Reuse the copy **only if it is on top right now** | Chat from notification, search results |
| `singleTask` | One copy per task. Reuse it and **destroy everything above it** | Home / main hub |
| `singleInstance` | One copy, **alone in its own task** | Call screen, alarm screen |

Golden rule: **singleTop only looks at the top of the stack at the moment of launch.**

## 3. onNewIntent in layman terms

Think of an activity as a **hotel room**.

- `onCreate()` = a **new room is built** and the guest checks in.
- `onNewIntent()` = the room already exists, so reception **slips a new note under the door** instead of building another room.

So `onNewIntent` is Android saying:

> "I did not create a new screen. I reused the one you already have. Here is the new request. Update yourself."

### When it fires

It fires **instead of** `onCreate` whenever an existing instance is reused:

- `singleTop` and the activity is on top
- `singleTask` and the instance already exists in the task
- `singleInstance` and the instance already exists
- `FLAG_ACTIVITY_SINGLE_TOP` or `FLAG_ACTIVITY_CLEAR_TOP` reused an instance

It does **not** fire for `standard`, because a new instance always goes through `onCreate`.

### Lifecycle order on reuse

- Screen visible on top: `onPause` -> `onNewIntent` -> `onResume`
- Screen was in the background: `onNewIntent` -> `onRestart` -> `onStart` -> `onResume`

## 4. The #1 gotcha: call setIntent()

`getIntent()` keeps returning the **old** intent that created the activity, even after a new one arrives. You must replace it yourself.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIntent(intent)            // first launch
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)               // without this, getIntent() returns the OLD one
    handleIntent(intent)            // reused launch
}

private fun handleIntent(intent: Intent) {
    val chatId = intent.getStringExtra("chat_id") ?: return
    loadChat(chatId)
}
```

**Rule:** put the "read extras and load data" logic in one function and call it from **both** `onCreate` and `onNewIntent`.

## 5. Examples

### Chat notification (singleTop)

1. You are chatting with Priya. A notification from **Rahul** arrives and you tap it.
2. ChatActivity is on top, so it is reused and `onNewIntent` fires.
3. Without `handleIntent`, the screen would still show Priya. With it, the screen switches to Rahul.

### Search results (singleTop)

- Stack `Home -> SearchResults`, search again: reused, `onNewIntent`, results update.
- Stack `Home -> SearchResults -> ProductDetail`, search again: SearchResults is **not** on top, so a **new** instance is created and `onNewIntent` does **not** fire.

### Home tab (singleTask)

- Stack `Home -> Cart -> Address -> Payment -> Success`.
- Launch Home: Home is reused, Cart, Address, Payment and Success are **destroyed**, `onNewIntent` fires on Home.
- Risk: unsaved work on the destroyed screens is lost (for example, a half-filled transfer form). Persist draft state.

### Double-tap problem (use debouncing, not singleTop)

- `singleTop` would stop double-tap duplicates, but it would also stop **Article A -> Article B** from stacking, because A is on top and gets reused.
- Better: keep `standard` and ignore a second tap within about 500 ms.

## 6. What the logs showed (mini project)

```
Stack 571: SingleTask, Standard  -> launch SingleTask
  -> SingleTask onNewIntent, Standard onDestroy      (popped what was above)
Launch SingleInstance
  -> task 572 (new tab), task 571 stays alive
Launch SingleTask from 572
  -> onNewIntent on the same instance in task 571    (no new instance)
```

- Counters like `count=7` belong to a single instance.
- A different hash in `onCreate` logs means a different object, so a new instance was created.


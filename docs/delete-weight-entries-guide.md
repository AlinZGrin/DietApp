# How to Delete Weight Entries

You have several ways to delete weight entries from your Room database and Firestore:

## 1. Using DatabaseDebugger (For Development/Testing Only)

**Note: Debug buttons have been removed from the UI. Use these methods programmatically for testing only.**

### Add to your ViewModel:

```kotlin
// In ChartsViewModel or any ViewModel
@Inject lateinit var databaseDebugger: DatabaseDebugger

fun deleteAllWeightEntries() {
    viewModelScope.launch {
        authState.collect { state ->
            if (state is AuthState.Authenticated) {
                databaseDebugger.deleteAllWeightEntries(state.userId)
            }
        }
    }
}

fun deleteAllWeightEntriesGlobal() {
    viewModelScope.launch {
        databaseDebugger.deleteAllWeightEntriesGlobal()
    }
}
```

### Temporarily Add Debug Buttons for Testing:

```kotlin
// In your Compose screen (temporarily for testing - DO NOT commit to production)
Button(onClick = { viewModel.deleteAllWeightEntries() }) {
    Text("Delete My Weight Entries")
}

Button(onClick = { viewModel.deleteAllWeightEntriesGlobal() }) {
    Text("Delete ALL Weight Entries")
}
```

## 2. Using Repository Directly

### In your ViewModel:

```kotlin
// Delete all weight entries for current user
fun deleteMyWeightEntries() {
    viewModelScope.launch {
        authState.collect { state ->
            if (state is AuthState.Authenticated) {
                val deletedCount = weightRepository.deleteAllWeightEntries(state.userId)
                println("Deleted $deletedCount weight entries")
            }
        }
    }
}

// Delete ALL weight entries (all users)
fun deleteAllWeightEntriesEverywhere() {
    viewModelScope.launch {
        val deletedCount = weightRepository.deleteAllWeightEntriesGlobal()
        println("Deleted $deletedCount weight entries globally")
    }
}
```

## 3. Available Delete Methods

### Weight Entries:

- `weightRepository.deleteAllWeightEntries(userId)` - Delete for specific user
- `weightRepository.deleteAllWeightEntriesGlobal()` - Delete for all users
- `weightRepository.deleteWeightEntry(entry)` - Delete single entry

### Food Logs:

- `foodRepository.deleteAllFoodLogs(userId)` - Delete all food logs for user
- `foodRepository.deleteFoodLog(log)` - Delete single food log

### Complete User Data:

- `databaseDebugger.clearAllUserData(userId)` - Delete all data for user

## 4. Quick Test Commands

### Check what's in database first:

```kotlin
// In your ViewModel
fun debugDatabase() {
    viewModelScope.launch {
        if (authState.value is AuthState.Authenticated) {
            val userId = (authState.value as AuthState.Authenticated).userId
            databaseDebugger.printDatabaseSummary(userId)
        }
    }
}
```

### Delete and verify:

```kotlin
fun deleteAndVerify() {
    viewModelScope.launch {
        if (authState.value is AuthState.Authenticated) {
            val userId = (authState.value as AuthState.Authenticated).userId

            // Print before
            databaseDebugger.printDatabaseSummary(userId)

            // Delete
            databaseDebugger.deleteAllWeightEntries(userId)

            // Print after
            databaseDebugger.printDatabaseSummary(userId)
        }
    }
}
```

## 5. Note About Firestore and Room Sync

The WeightRepository methods handle both **local Room database** and **Firestore (cloud)** automatically:

- `deleteAllWeightEntriesWithFirestore(userId)` - Deletes from both Room and Firestore for a specific user
- `deleteAllWeightEntriesGlobalWithFirestore()` - Deletes from both Room and Firestore for all users

The debug methods focus on local data only, but the repository methods provide full sync capabilities.

## 6. Nuclear Option: ADB Database Deletion

If programmatic deletion doesn't work (e.g., due to Firestore sync issues), you can directly delete the Room database files:

### Prerequisites:

1. Add ADB to your PATH: `C:\Users\aling\AppData\Local\Android\Sdk\platform-tools`
2. Enable USB Debugging on your device
3. Connect device via USB

### Commands:

```powershell
# Stop the app
adb shell am force-stop com.dietapp.data.database

# Delete all database files
adb shell "run-as com.dietapp.data.database rm /data/data/com.dietapp.data.database/databases/diet_app_database"
adb shell "run-as com.dietapp.data.database rm /data/data/com.dietapp.data.database/databases/diet_app_database-shm"
adb shell "run-as com.dietapp.data.database rm /data/data/com.dietapp.data.database/databases/diet_app_database-wal"

# Verify deletion
adb shell "run-as com.dietapp.data.database ls -la /data/data/com.dietapp.data.database/databases/"

# Restart the app (Room will recreate empty database)
```

⚠️ **Warning**: If your app syncs from Firestore to Room on startup, you MUST also delete Firestore data or it will re-sync back to Room!

## 7. Android Studio Database Inspector SQL Commands

You can execute SQL commands directly in Android Studio's Database Inspector, but there are limitations:

### Prerequisites:

1. **App must be running** and actively connected
2. Database must be **online** (not offline)

### SQL Commands:

```sql
-- Check entry count
SELECT COUNT(*) FROM weight_entries;

-- View all entries
SELECT * FROM weight_entries LIMIT 100;

-- Check by user
SELECT userId, COUNT(*) as count FROM weight_entries GROUP BY userId;

-- Delete all entries (only works if database is online)
DELETE FROM weight_entries;
```

### ⚠️ Limitations:

- **"Modifier statements are disabled on offline databases"** - You can only run DELETE/UPDATE commands when the app is actively running
- **Read-only when offline** - You can only SELECT when the database is offline
- **No Firestore sync** - SQL commands only affect Room, not Firestore

### Alternative if Database Inspector doesn't work:

Use ADB with SQLite commands instead (see Section 8 below).

## 8. ADB SQLite Commands (Alternative to Database Inspector)

If Database Inspector shows "offline" or disables modifiers, use ADB directly:

```powershell
# Connect to database via ADB (app must be stopped)
adb shell am force-stop com.dietapp.data.database

# Access SQLite directly
adb shell "run-as com.dietapp.data.database sqlite3 /data/data/com.dietapp.data.database/databases/diet_app_database"
```

Then in SQLite prompt:

```sql
-- Check entries
SELECT COUNT(*) FROM weight_entries;

-- Delete all entries
DELETE FROM weight_entries;

-- Exit SQLite
.quit
```

**Note**: This requires SQLite to be available on your device (not always available on newer Android versions).

## 9. Firestore Sync Issue

**Important**: If you see entries reappearing after any deletion method, your app is likely syncing from Firestore back to Room.

### Solution:

1. **First** delete from Firestore using the emergency cleanup button or Firebase Console
2. **Then** delete Room database using ADB or SQL
3. **Or** use the `nukeDatabaseEmergency()` method which handles both

### Check Firestore Console:

- Go to Firebase Console → Firestore Database
- Look for `WeightEntries` collection
- Manually delete all documents if needed

## 10. Sample Data Creation Issue

**Important**: Your app automatically creates sample weight entries on startup!

### The Problem:

- `SeedDataManager` creates 10-11 sample weight entries every time a user authenticates
- This happens in `DietApp.kt` → `seedSampleUserData()` → `seedSampleWeightEntries()`
- Even after deleting all data, the app recreates sample entries

### The Solution:

**Temporarily disable sample data creation** in `SeedDataManager.kt`:

```kotlin
// In seedSampleUserData() method, comment out this line:
// seedSampleWeightEntries(userId)
```

### Location:

- File: `android-app/app/src/main/java/com/dietapp/data/seeddata/SeedDataManager.kt`
- Method: `seedSampleUserData(userId: String)`
- Line 45: Comment out `seedSampleWeightEntries(userId)`

### For Production:

Consider adding a flag to disable sample data in production builds:

```kotlin
fun seedSampleUserData(userId: String) {
    if (BuildConfig.DEBUG) {
        // Only create sample data in debug builds
        seedSampleWeightEntries(userId)
    }
}
```

## Production App Behavior

In the production app, users can:

- Add weight entries via the "+" floating action button
- View their weight progress and recent entries
- Toggle between metric (kg) and imperial (lbs) units
- Delete individual entries (if needed, implement via long-press or swipe actions)

**Debug/delete buttons have been removed from the UI for production use.**

## Quick Start for Development:

1. **Temporarily** add debug buttons to your UI for testing
2. Use `deleteAllWeightEntries(userId)` to delete weight entries for the current user
3. Check Android Studio Logcat to see the results
4. **Remove debug buttons before committing to production**

## Clean UI

The SimpleProgressTrackingScreen now has a clean, production-ready interface with:

- Add weight entry button (floating action button)
- Weight progress summary
- Recent weight entries list
- Unit toggle (kg/lbs)
- Clean, user-friendly design without debug clutter

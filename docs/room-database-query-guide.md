# Room Database Query Guide

This guide shows you how to query your Room database in the Diet App.

## 1. Using Database Inspector (Easiest - Android Studio)

### Enable Database Inspector:

1. Open **Android Studio**
2. Run your app on an emulator or device
3. Go to **View** → **Tool Windows** → **Database Inspector**
4. Select your app process
5. You'll see `diet_app_database` with all tables

### Tables Available:

- `foods` - Food items from USDA API
- `food_logs` - User food consumption entries
- `user_profiles` - User settings and info
- `weight_entries` - Weight tracking data
- `goals` - Diet and fitness goals
- `water_intake` - Water consumption
- `exercise_logs` - Exercise tracking

## 2. Using ADB Shell (Command Line)

### Connect to Device Database:

```bash
# Connect to your device/emulator
adb shell

# Navigate to app database (requires root or debuggable app)
cd /data/data/com.dietapp/databases/

# Open SQLite database
sqlite3 diet_app_database

# List all tables
.tables

# Query examples:
SELECT * FROM foods LIMIT 10;
SELECT * FROM food_logs WHERE userId = 'your-user-id';
SELECT * FROM user_profiles;
SELECT * FROM weight_entries ORDER BY date DESC;

# Exit SQLite
.quit
```

## 3. Using Repository Methods (In Code)

Your app already has repository methods to query the database:

### Food Queries:

```kotlin
// In a ViewModel or Repository
foodRepository.searchFoods("apple") // Search for foods
foodRepository.getAllFoods() // Get all foods
foodRepository.getFoodById("food-id") // Get specific food
```

### Food Log Queries:

```kotlin
// Get food logs for a date range
foodRepository.getFoodLogsInRange(userId, startDate, endDate)

// Get today's food logs
foodRepository.getFoodLogsByDate(userId, Date())

// Get specific meal logs
foodRepository.getFoodLogsByMealAndDate(userId, "breakfast", Date())
```

### User Profile Queries:

```kotlin
// Get user profile
userRepository.getUserProfile(userId)

// Get user profile as Flow (reactive)
userRepository.getUserProfileFlow(userId)
```

### Weight Queries:

```kotlin
// Get weight entries in date range
weightRepository.getWeightEntriesInRange(userId, startDate, endDate)

// Get all weight entries
weightRepository.getAllWeightEntries(userId)
```

## 4. Custom Queries in DAOs

You can add custom queries to your DAO interfaces:

### Example Custom Query:

```kotlin
// In FoodLogDao.kt
@Query("""
    SELECT fl.*, f.description as foodName
    FROM food_logs fl
    JOIN foods f ON fl.foodId = f.id
    WHERE fl.userId = :userId
    AND date(fl.date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')
    ORDER BY fl.createdAt DESC
""")
suspend fun getFoodLogsWithNames(userId: String, date: Date): List<FoodLogWithName>
```

## 5. Debug Database Content

### Add Debug Function to Repository:

```kotlin
// In FoodRepository.kt
suspend fun debugDatabaseContent() {
    val allFoods = foodDao.getAllFoods(50).first()
    val allLogs = foodLogDao.getFoodLogsByDateRange(
        "current-user-id",
        Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000), // 30 days ago
        Date()
    ).first()

    println("DEBUG: Total foods in database: ${allFoods.size}")
    println("DEBUG: Total logs in last 30 days: ${allLogs.size}")

    allFoods.take(5).forEach { food ->
        println("DEBUG: Food - ${food.description} (${food.calories} cal)")
    }

    allLogs.take(5).forEach { log ->
        println("DEBUG: Log - ${log.foodId} on ${log.date} (${log.calories} cal)")
    }
}
```

### Call Debug Function:

```kotlin
// In a ViewModel
viewModelScope.launch {
    foodRepository.debugDatabaseContent()
}
```

## 6. Database Export (For Analysis)

### Export Database File:

```bash
# Pull database from device to computer
adb pull /data/data/com.dietapp/databases/diet_app_database ./database_backup.db

# Open with SQLite browser or any SQLite tool
```

## 7. Common Queries You Might Need

### Check Data Existence:

```sql
-- Count records in each table
SELECT COUNT(*) as food_count FROM foods;
SELECT COUNT(*) as log_count FROM food_logs;
SELECT COUNT(*) as profile_count FROM user_profiles;
SELECT COUNT(*) as weight_count FROM weight_entries;
```

### Recent Activity:

```sql
-- Recent food logs
SELECT fl.*, f.description
FROM food_logs fl
JOIN foods f ON fl.foodId = f.id
ORDER BY fl.createdAt DESC
LIMIT 10;

-- Recent weight entries
SELECT * FROM weight_entries
ORDER BY date DESC
LIMIT 10;
```

### User Summary:

```sql
-- User's total calories today
SELECT SUM(calories) as total_calories
FROM food_logs
WHERE userId = 'your-user-id'
AND date(date/1000, 'unixepoch') = date('now');
```

## Quick Start: Check Your Database

1. **Android Studio**: Use Database Inspector while app is running
2. **ADB**: `adb shell` → `sqlite3 /data/data/com.dietapp/databases/diet_app_database`
3. **Code**: Add debug prints in your repositories
4. **Export**: `adb pull` the database file for external analysis

Choose the method that works best for your debugging needs!

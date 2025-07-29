# Firestore Collections Initialization

This script helps initialize the required Firestore collections for your Diet App.

## Required Collections

Your app expects these Firestore collections:

1. **`foods`** - Food items from USDA API searches
2. **`foodLogs`** - User food consumption entries
3. **`weightEntries`** - User weight tracking data
4. **`goals`** - User diet and fitness goals

Note: `userProfiles` are stored locally only (Room database), not in Firestore.

## Setup Instructions

### Option 1: Use the Firebase Console (Recommended)

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Go to **Firestore Database**
4. Click **"Start collection"** for each collection:
   - `foods`
   - `foodLogs`
   - `weightEntries`
   - `goals`
5. Add a temporary document to each collection (you can delete it later)

### Option 2: Use the Node.js Script

1. Install Node.js dependencies:

   ```bash
   npm install firebase-admin
   ```

2. Download your service account key:

   - Go to Firebase Console → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Save the JSON file to this directory

3. Update the script:

   - Edit `initialize-firestore.js`
   - Replace `./path-to-your-service-account-key.json` with your actual file path

4. Run the script:

   ```bash
   node initialize-firestore.js
   ```

5. Optional: Clean up sample data:
   ```bash
   node initialize-firestore.js --cleanup
   ```

## Verification

After initialization, you should see these collections in your Firestore Console. The app will automatically start syncing data to these collections when users:

- Search and save foods
- Log food entries
- Track weight
- Set fitness goals

## Troubleshooting

If you're still not seeing data in Firestore:

1. Check that Firebase Authentication is working
2. Verify Firestore rules allow authenticated users to read/write
3. Check the app logs for any sync errors
4. Ensure you're logged in to the app with a valid Firebase user

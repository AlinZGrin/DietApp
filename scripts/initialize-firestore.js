const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
// You'll need to download your service account key from Firebase Console
// and replace the path below
const serviceAccount = require('./path-to-your-service-account-key.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function initializeCollections() {
  console.log('Initializing Firestore collections...');

  try {
    // Initialize foods collection with a sample food item
    await db.collection('foods').doc('sample-food').set({
      id: 'sample-food',
      description: 'Sample Food Item',
      brandName: '',
      calories: 100,
      protein: 5.0,
      carbs: 15.0,
      fat: 3.0,
      fiber: 2.0,
      sugar: 8.0,
      sodium: 50.0,
      potassium: 200.0,
      servingSize: '1 serving',
      servingUnit: 'serving',
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    console.log('✓ Created foods collection');

    // Initialize foodLogs collection with a sample log
    await db.collection('foodLogs').add({
      userId: 'sample-user',
      foodId: 'sample-food',
      quantity: 1.0,
      calories: 100,
      protein: 5.0,
      carbs: 15.0,
      fat: 3.0,
      fiber: 2.0,
      sugar: 8.0,
      sodium: 50.0,
      potassium: 200.0,
      date: admin.firestore.Timestamp.now(),
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    console.log('✓ Created foodLogs collection');

    // Initialize weightEntries collection with a sample entry
    await db.collection('weightEntries').add({
      userId: 'sample-user',
      weight: 70.0,
      date: admin.firestore.Timestamp.now(),
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    console.log('✓ Created weightEntries collection');

    // Initialize goals collection with a sample goal
    await db.collection('goals').add({
      userId: 'sample-user',
      type: 'weight_loss',
      targetWeight: 65.0,
      currentWeight: 70.0,
      targetCalories: 1800,
      targetProtein: 120.0,
      targetCarbs: 180.0,
      targetFat: 60.0,
      startDate: admin.firestore.Timestamp.now(),
      targetDate: admin.firestore.Timestamp.fromDate(new Date(Date.now() + 90 * 24 * 60 * 60 * 1000)), // 90 days from now
      isActive: true,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    console.log('✓ Created goals collection');

    console.log('All collections initialized successfully!');

    // Clean up sample data (optional)
    const shouldCleanup = process.argv.includes('--cleanup');
    if (shouldCleanup) {
      console.log('\nCleaning up sample data...');
      await db.collection('foods').doc('sample-food').delete();

      const foodLogsSnapshot = await db.collection('foodLogs').where('userId', '==', 'sample-user').get();
      foodLogsSnapshot.forEach(doc => doc.ref.delete());

      const weightEntriesSnapshot = await db.collection('weightEntries').where('userId', '==', 'sample-user').get();
      weightEntriesSnapshot.forEach(doc => doc.ref.delete());

      const goalsSnapshot = await db.collection('goals').where('userId', '==', 'sample-user').get();
      goalsSnapshot.forEach(doc => doc.ref.delete());

      console.log('✓ Cleaned up sample data');
    }

  } catch (error) {
    console.error('Error initializing collections:', error);
  } finally {
    process.exit(0);
  }
}

initializeCollections();

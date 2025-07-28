const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
// You'll need to download your service account key from Firebase Console
const serviceAccount = require('./serviceAccountKey.json'); // You need to download this

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Collections to clear
const collections = [
  'food_logs',
  'foods',
  'goals',
  'weight_entries',
  'users'
];

async function deleteCollection(collectionName) {
  console.log(`Deleting collection: ${collectionName}`);

  const collectionRef = db.collection(collectionName);
  const query = collectionRef.limit(500); // Process in batches

  return new Promise((resolve, reject) => {
    deleteQueryBatch(query, resolve).catch(reject);
  });
}

async function deleteQueryBatch(query, resolve) {
  const snapshot = await query.get();

  const batchSize = snapshot.size;
  if (batchSize === 0) {
    // When there are no documents left, we are done
    resolve();
    return;
  }

  // Delete documents in a batch
  const batch = db.batch();
  snapshot.docs.forEach((doc) => {
    batch.delete(doc.ref);
  });

  await batch.commit();
  console.log(`Deleted ${batchSize} documents`);

  // Recurse on the next process tick, to avoid
  // exploding the stack.
  process.nextTick(() => {
    deleteQueryBatch(query, resolve);
  });
}

async function clearAllData() {
  try {
    console.log('Starting to clear all Firestore data...');

    for (const collection of collections) {
      await deleteCollection(collection);
      console.log(`✅ Cleared collection: ${collection}`);
    }

    console.log('🎉 All test data has been cleared from Firestore!');
    process.exit(0);
  } catch (error) {
    console.error('❌ Error clearing data:', error);
    process.exit(1);
  }
}

// Run the cleanup
clearAllData();

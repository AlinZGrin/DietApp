/*
Manual Firestore Collections Setup

Navigate to: https://console.firebase.google.com/project/YOUR_PROJECT_ID/firestore/data

1. Click "Start collection"
2. Collection ID: "foods"
3. Add a sample document with these fields:

Document data for "foods" collection:
- id: "test-food"
- description: "Test Food Item"
- calories: 100
- protein: 5.0
- carbs: 15.0
- fat: 3.0
- createdAt: (timestamp)

4. Repeat for other collections:

Document data for "foodLogs" collection:
- userId: "test-user"
- foodId: "test-food"
- quantity: 1.0
- calories: 100
- protein: 5.0
- carbs: 15.0
- fat: 3.0
- date: (timestamp)

Document data for "weightEntries" collection:
- userId: "test-user"
- weight: 70.0
- date: (timestamp)

Document data for "goals" collection:
- userId: "test-user"
- type: "weight_loss"
- targetWeight: 65.0
- currentWeight: 70.0
- targetCalories: 1800
- isActive: true
*/

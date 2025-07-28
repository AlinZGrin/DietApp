#!/bin/bash

# Clear all Firestore collections using Firebase CLI
# This script deletes all documents from the main collections

echo "🔥 Starting to clear all Firestore test data..."
echo "⚠️  This will delete ALL data in the following collections:"
echo "   - food_logs"
echo "   - foods"
echo "   - goals"
echo "   - weight_entries"
echo "   - users"
echo ""

read -p "Are you sure you want to continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "❌ Operation cancelled."
    exit 1
fi

echo "🗑️  Deleting all documents from collections..."

# Delete all documents from each collection
# Note: This requires recursive deletion for collections with subcollections

firebase firestore:delete --all-collections --force

echo ""
echo "🎉 All test data has been cleared from Firestore!"
echo "✅ Your database is now clean."

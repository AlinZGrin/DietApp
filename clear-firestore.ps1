# Clear all Firestore collections using Firebase CLI
# This script deletes all documents from the main collections

Write-Host "🔥 Starting to clear all Firestore test data..." -ForegroundColor Red
Write-Host "⚠️  This will delete ALL data in the following collections:" -ForegroundColor Yellow
Write-Host "   - food_logs" -ForegroundColor Yellow
Write-Host "   - foods" -ForegroundColor Yellow
Write-Host "   - goals" -ForegroundColor Yellow
Write-Host "   - weight_entries" -ForegroundColor Yellow
Write-Host "   - users" -ForegroundColor Yellow
Write-Host ""

$confirmation = Read-Host "Are you sure you want to continue? (y/N)"
if ($confirmation -ne 'y' -and $confirmation -ne 'Y') {
    Write-Host "❌ Operation cancelled." -ForegroundColor Red
    exit 1
}

Write-Host "🗑️  Deleting all documents from collections..." -ForegroundColor Yellow

# Delete all documents from all collections
firebase firestore:delete --all-collections --force

Write-Host ""
Write-Host "🎉 All test data has been cleared from Firestore!" -ForegroundColor Green
Write-Host "✅ Your database is now clean." -ForegroundColor Green

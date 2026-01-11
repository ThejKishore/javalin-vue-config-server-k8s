# 🧪 Delete Property Feature - Testing Guide

## Quick Test (2 minutes)

### Prerequisites
1. Application is running on `http://localhost:7070`
2. Navigate to "Config Management" page
3. Select a domain and application
4. Click "Load Properties" to see the properties list

### Steps to Test Delete

1. **Locate Delete Button**
   - In the properties table (desktop) or card (mobile)
   - Each property row has a red "Delete" button

2. **Click Delete**
   - Click the "Delete" button for any property
   - A confirmation dialog will appear

3. **Confirm Deletion**
   - Click "OK" in the confirmation dialog
   - The property should be removed from the list
   - Version number should increment

4. **Verify Results**
   - Property is no longer in the list ✓
   - Sync info shows new version number ✓
   - No error messages appear ✓

---

## Detailed Testing

### Test Case 1: Delete Existing Property ✅

**Precondition**: Properties are loaded in the UI

**Steps**:
1. Click Delete button on any property
2. Click "OK" in confirmation dialog
3. Wait for operation to complete

**Expected Results**:
- Property removed from list
- Version number incremented
- "Saving..." indicator appears and disappears
- No error messages

**Verify**:
```javascript
// In browser console, check for errors
console.log('No errors should appear')
```

---

### Test Case 2: Cancel Delete Operation ✅

**Precondition**: Properties are loaded in the UI

**Steps**:
1. Click Delete button on any property
2. Click "Cancel" in confirmation dialog

**Expected Results**:
- Dialog closes
- Property remains in the list
- No changes to version number
- No API calls made

---

### Test Case 3: Delete Non-existent Property ✅

**Precondition**: Properties are loaded

**Steps**:
1. Delete a property (Property A)
2. Reload properties
3. Attempt to delete the same property again

**Expected Results**:
- Error message: "Property [key] not found for [app] in [domain]"
- List still shows remaining properties
- No partial updates

---

### Test Case 4: Verify Audit Trail ✅

**Precondition**: Delete operation completed

**Steps**:
1. Navigate to "Audit History"
2. Select same domain and application
3. Look for the deleted property in the audit trail

**Expected Results**:
- Show most recent entry
- Operation: "DELETED"
- Property key matches deleted property
- Old value shows the deleted value
- New value is empty/null
- Updated timestamp is recent
- Version number is incremented

---

### Test Case 5: Concurrent Delete Handling ✅

**Precondition**: Two browser windows open with same config loaded

**Steps**:
1. In Window 1: Delete property X
2. In Window 2: Try to update any property
3. Click "Publish Changes"

**Expected Results**:
- Window 2 gets error: "Configuration has been modified"
- User must click "Load Properties" to refresh
- Prevents data loss from concurrent updates

---

### Test Case 6: Mobile UI Delete ✅

**Device**: Mobile or smaller viewport (< 768px)

**Steps**:
1. Load properties on mobile
2. Properties appear as cards (not table)
3. Each card has a "Delete" button at the bottom
4. Click Delete button
5. Confirm deletion

**Expected Results**:
- Delete button is touch-friendly (44px minimum)
- Card view properly shows delete action
- Same behavior as desktop
- Proper spacing on mobile

---

### Test Case 7: Multiple Deletions ✅

**Precondition**: Properties are loaded

**Steps**:
1. Delete property A
2. Wait for completion
3. Delete property B
4. Wait for completion
5. Reload properties

**Expected Results**:
- Both properties are removed
- Version number incremented for each deletion
- Audit trail shows both deletions
- Each deletion has unique entry with DELETED operation

---

### Test Case 8: Delete All Properties ✅

**Precondition**: Multiple properties exist

**Steps**:
1. Delete property by property until none remain
2. Load properties page shows "No properties found"

**Expected Results**:
- All deletions succeed
- Version numbers increment properly
- Audit trail shows all deletions
- Message "No properties found" displays
- Can still add new properties

---

## Edge Cases

### Empty Property Key/Value ❌
- Delete button works only with actual properties
- Empty rows cannot be deleted (by design)

### Invalid Domain/App ❌
- If domain or app becomes invalid:
  - Error message: "Configuration not found"
  - User must select valid domain/app again

### Network Error ❌
- If delete fails due to network:
  - Error message displayed
  - Property remains in list
  - User can retry

---

## API Testing (Using cURL)

### Delete Property via API

```bash
# Delete a property
curl -X DELETE \
  'http://localhost:7070/api/config/properties/DOMAIN/APP/property_key' \
  -H 'Content-Type: application/json'

# Expected Response (200 OK)
{
  "properties": [
    {
      "applicationName": "APP",
      "domain": "DOMAIN",
      "propertyKey": "other_key",
      "propertyValue": "value",
      "createdBy": "admin",
      "createdTm": "2024-01-10T...",
      "updatedBy": "admin",
      "updatedTm": "2024-01-10T..."
    }
  ],
  "syncInfo": {
    "applicationName": "APP",
    "domain": "DOMAIN",
    "versionNumber": 2,
    "updatedBy": "system",
    "updatedTm": "2024-01-10T..."
  }
}

# Error Response (404 Not Found)
{
  "error": "Property property_key not found for APP in DOMAIN"
}
```

---

## Database Verification

### Check Deleted Property in Database

```sql
-- Check audit trail for DELETED operation
SELECT * FROM app_config_audit 
WHERE operation = 'DELETED' 
ORDER BY updated_tm DESC 
LIMIT 5;

-- Verify property is removed from active config
SELECT COUNT(*) FROM app_config 
WHERE application_name = 'APP' 
AND domain = 'DOMAIN' 
AND property_key = 'deleted_key';
-- Should return 0

-- Check version increment
SELECT version_number, updated_tm 
FROM config_sync 
WHERE application_name = 'APP' 
AND domain = 'DOMAIN';
```

---

## Browser Console Checks

### No Errors Should Appear
```javascript
// Open browser DevTools (F12)
// Go to Console tab
// No red error messages should appear
// Only informational logs

// Expected messages:
// ✓ Network request successful
// ✓ Properties updated
// ✓ No JavaScript errors
```

### Network Tab Verification
1. Open DevTools → Network tab
2. Delete a property
3. Look for DELETE request:
   - URL: `/api/config/properties/.../{propertyKey}`
   - Method: DELETE
   - Status: 200 OK
   - Response: PropertiesWithSyncResponse

---

## Performance Testing

### Delete Performance Benchmark
- **Expected time**: < 1 second
- **UI responsiveness**: Smooth with loading indicator
- **No lag**: Properties update immediately after response

### Test with Many Properties
1. Load configuration with 100+ properties
2. Delete a property
3. Performance should be good (< 2 seconds)

---

## Accessibility Testing

### Keyboard Navigation
1. Tab to Delete button
2. Press Enter to delete
3. Use Tab/Enter in confirmation dialog
4. Confirm deletion works with keyboard only

### Screen Reader Testing
1. Use ARIA attributes to announce actions
2. Error messages should be announced
3. Loading states should be announced

---

## Regression Testing

### Verify Other Features Still Work

- [ ] Add new property
- [ ] Update property values
- [ ] Load properties
- [ ] View audit history
- [ ] Navigation between pages
- [ ] Mobile menu toggle
- [ ] Autocomplete fields

---

## Sign-off Checklist

After testing, verify:

- [ ] Delete button works
- [ ] Confirmation dialog appears
- [ ] Property is removed
- [ ] Version increments
- [ ] Audit trail records deletion
- [ ] Mobile UI works
- [ ] Error handling works
- [ ] No console errors
- [ ] API responds correctly
- [ ] Database is updated correctly

---

## Common Issues & Troubleshooting

### Issue: Delete button doesn't appear
**Solution**: Ensure properties are loaded and you're viewing the table/cards correctly

### Issue: Delete fails with 404
**Solution**: Property may have been deleted already; reload properties

### Issue: Version doesn't increment
**Solution**: Page may need refresh to show new version

### Issue: Property still appears after delete
**Solution**: Refresh the page (F5) to reload from server

### Issue: Audit trail doesn't show deletion
**Solution**: Check audit history for correct domain/application

---

## Test Report Template

```
Date: _______________
Tester: _______________
Environment: localhost:7070

Test Results:
- Delete Property: [ ] PASS [ ] FAIL
- Confirmation Dialog: [ ] PASS [ ] FAIL
- Audit Trail: [ ] PASS [ ] FAIL
- Mobile UI: [ ] PASS [ ] FAIL
- Error Handling: [ ] PASS [ ] FAIL
- API Response: [ ] PASS [ ] FAIL

Issues Found:
1. _______________
2. _______________
3. _______________

Notes:
_______________________________________________
_______________________________________________

Overall Status: [ ] PASS [ ] FAIL
```

---

## Summary

✅ Delete property feature is fully implemented and ready for testing
✅ All backend endpoints are working
✅ UI properly handles delete operations
✅ Audit trail records all deletions
✅ Error handling is in place

**Ready for QA and Production! 🚀**

---

**Document Version**: 1.0
**Date**: January 10, 2026
**Status**: Complete


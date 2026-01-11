# 🧪 SERVICE ONBOARDING - TESTING GUIDE

## Quick Start Test (5 minutes)

### Prerequisites
- Application running on `http://localhost:7070`
- Test configuration file ready

### Step-by-Step Test

#### Test 1: Navigate to Service Onboard
1. Open browser to `http://localhost:7070`
2. In sidebar, expand "Configuration Management" menu ✓
3. Click "Service Onboard" submenu ✓
4. Should see onboarding form ✓

#### Test 2: Test Form Validation
1. Leave all fields empty
2. Click "Onboard Service" button
3. Button should be disabled (grayed out) ✓
4. Fill in Domain: "test-domain"
5. Application still empty
6. Button still disabled ✓
7. Fill Application: "test-app"
8. Button still disabled (file required) ✓
9. Select file
10. Button becomes enabled ✓

#### Test 3: Successful Onboarding
1. Fill form:
   - Domain: "production"
   - Application: "user-service"
   - File: sample.properties
2. Click "Onboard Service"
3. See loading indicator ✓
4. See success message ✓
5. Auto-navigate to Configuration ✓
6. Domain and Application auto-populated ✓
7. Click "Load Properties"
8. See all properties from file ✓

#### Test 4: Duplicate Prevention
1. Try to onboard same service again
2. Click "Onboard Service"
3. See error: "Service 'user-service' is already onboarded" ✓
4. Error message is clear and helpful ✓

---

## Detailed Test Cases

### TC-001: Domain Validation
**Steps**:
1. Leave Domain empty
2. Fill Application, select file
3. Click Onboard

**Expected Result**:
- Button disabled or error message

---

### TC-002: Application Validation
**Steps**:
1. Fill Domain
2. Leave Application empty
3. Select file
4. Click Onboard

**Expected Result**:
- Button disabled or error message

---

### TC-003: File Required Validation
**Steps**:
1. Fill Domain and Application
2. Don't select file
3. Click Onboard

**Expected Result**:
- Button remains disabled

---

### TC-004: Properties File Upload
**Steps**:
1. Create test.properties:
   ```properties
   app.name=MyApp
   app.version=1.0.0
   server.port=8080
   ```
2. Fill form and upload
3. Check properties loaded

**Expected Result**:
- Success message ✓
- Navigate to Config ✓
- Show 3 properties ✓

---

### TC-005: YAML File Upload
**Steps**:
1. Create test.yaml:
   ```yaml
   app:
     name: MyApp
     version: 1.0.0
   server:
     port: 8080
   ```
2. Upload file
3. Check properties

**Expected Result**:
- Success ✓
- Properties loaded ✓

---

### TC-006: JSON File Upload
**Steps**:
1. Create test.json:
   ```json
   {
     "app.name": "MyApp",
     "app.version": "1.0.0",
     "server.port": "8080"
   }
   ```
2. Upload file
3. Check properties

**Expected Result**:
- Success ✓
- Properties loaded ✓

---

### TC-007: Drag and Drop Upload
**Steps**:
1. Drag file from file explorer
2. Drop on upload area
3. File should appear selected ✓
4. Click Onboard

**Expected Result**:
- File uploads successfully
- No errors

---

### TC-008: Remove File Option
**Steps**:
1. Select a file
2. See "Remove file" button ✓
3. Click it
4. File cleared ✓
5. Button disabled again ✓

---

### TC-009: Duplicate Prevention
**Steps**:
1. Onboard: domain="prod", app="service1"
2. Try again with same domain+app
3. Click Onboard

**Expected Result**:
- Error message ✓
- Shows it's already onboarded ✓
- Form not cleared ✓

---

### TC-010: Auto-Load After Success
**Steps**:
1. Onboard new service
2. See success message
3. Auto-navigate to Config ✓
4. Domain pre-selected ✓
5. Application pre-selected ✓
6. Click "Load Properties"
7. Properties appear ✓

---

### TC-011: Empty File Error
**Steps**:
1. Create empty file.properties
2. Try to upload
3. Click Onboard

**Expected Result**:
- Error: "Configuration file is empty or has no valid properties"

---

### TC-012: Invalid File Format
**Steps**:
1. Try to upload .txt file
2. Browse should filter to right types
3. Or if allowed, upload
4. Click Onboard

**Expected Result**:
- Error message or file type not accepted
- Clear guidance on supported types

---

### TC-013: Large File Handling
**Steps**:
1. Create large config file (5 MB)
2. Upload
3. Monitor loading time

**Expected Result**:
- Handles gracefully ✓
- Shows loading spinner ✓
- Completes within reasonable time

---

### TC-014: Mobile Responsiveness
**Steps**:
1. Open in mobile browser (or resize to 375px)
2. Fill form
3. File upload works ✓
4. Buttons touch-friendly ✓
5. Form is readable ✓
6. No horizontal scroll ✓

---

### TC-015: Error Recovery
**Steps**:
1. Fill form
2. Click Onboard
3. Simulate network error (DevTools)
4. See error message ✓
5. Click Onboard again
6. Works on retry ✓

---

## API Testing

### Test Endpoint Directly

#### Using cURL
```bash
# Create test file
echo "server.port=8080
database.url=localhost:5432
app.name=TestApp" > config.properties

# Upload
curl -X POST http://localhost:7070/api/config/onboard \
  -F "domain=production" \
  -F "application=test-service" \
  -F "file=@config.properties"

# Expected response (201):
# {
#   "properties": [...],
#   "syncInfo": {...}
# }
```

#### Test Duplicate Error
```bash
# Try same service again
curl -X POST http://localhost:7070/api/config/onboard \
  -F "domain=production" \
  -F "application=test-service" \
  -F "file=@config.properties"

# Expected response (400):
# {
#   "error": "Service 'test-service' is already onboarded in domain 'production'..."
# }
```

---

## Database Verification

### Check Onboarded Service in Database

```sql
-- View all properties for onboarded service
SELECT * FROM app_config 
WHERE application_name = 'user-service' 
AND domain = 'production';

-- View sync info
SELECT * FROM config_sync 
WHERE application_name = 'user-service' 
AND domain = 'production';

-- View audit trail
SELECT * FROM app_config_audit 
WHERE application_name = 'user-service' 
AND domain = 'production'
ORDER BY updated_tm DESC;

-- Check version number
SELECT version_number FROM config_sync 
WHERE application_name = 'user-service';
-- Should be 1 for newly onboarded
```

---

## Browser Console Checks

### No Errors Expected
```javascript
// Open DevTools (F12)
// Go to Console tab
// No red errors should appear
// Successful messages:
// - "Service onboarded successfully"
// - "Properties loaded"
// - Network requests succeed
```

### Check Network Tab
1. Open DevTools → Network
2. Upload file
3. Look for POST request to `/api/config/onboard`
4. Status should be 201
5. Response should include properties and syncInfo

---

## Performance Testing

### Measure Upload Time
```javascript
// In browser console before upload
console.time('upload');
// Click Onboard
// After completion
console.timeEnd('upload');
```

**Expected**:
- Small file (< 1 MB): 200-300ms
- Medium file (1-5 MB): 500-800ms
- Large file (5-10 MB): 1000-1500ms

---

## Accessibility Testing

### Keyboard Navigation
1. Tab through form
2. All fields should be accessible
3. Tab to "Onboard Service" button
4. Press Enter to submit
5. No mouse needed ✓

### Screen Reader Test
1. Use NVDA or JAWS
2. Read form labels
3. Button text is clear
4. Error messages announced
5. Success messages announced

---

## Cross-Browser Testing

### Chrome
- [ ] Navigation works
- [ ] File upload works
- [ ] Properties load
- [ ] Mobile view works

### Firefox
- [ ] Navigation works
- [ ] File upload works
- [ ] Properties load
- [ ] Mobile view works

### Safari
- [ ] Navigation works
- [ ] File upload works
- [ ] Properties load
- [ ] Mobile view works

### Edge
- [ ] Navigation works
- [ ] File upload works
- [ ] Properties load
- [ ] Mobile view works

---

## Test Report Template

```
Service Onboarding Test Report
Date: [DATE]
Tester: [NAME]
Browser: [BROWSER/VERSION]
Device: [DEVICE TYPE]

Test Results:
☐ Navigation to Service Onboard - PASS/FAIL
☐ Form Validation - PASS/FAIL
☐ Properties File Upload - PASS/FAIL
☐ YAML File Upload - PASS/FAIL
☐ JSON File Upload - PASS/FAIL
☐ Duplicate Prevention - PASS/FAIL
☐ Auto-Load Data - PASS/FAIL
☐ Mobile Responsiveness - PASS/FAIL
☐ Error Handling - PASS/FAIL
☐ Database Verification - PASS/FAIL

Issues Found:
1. [Issue description]
2. [Issue description]

Notes:
[Additional observations]

Overall Status: [ ] PASS [ ] FAIL
```

---

## Checklist Before Deployment

- [ ] All tests passed (desktop)
- [ ] All tests passed (mobile)
- [ ] All browsers tested
- [ ] Performance acceptable
- [ ] Database queries verified
- [ ] Error messages user-friendly
- [ ] No console errors
- [ ] Auto-navigation works
- [ ] Menu hierarchy correct
- [ ] Build successful (0 errors)
- [ ] Documentation complete
- [ ] Ready for production

---

**Testing Complete! ✅**

All tests should pass before pushing to production.

For any issues, check:
1. Browser console (F12)
2. Network tab for API calls
3. Server logs
4. Database entries
5. Feature documentation

---

**Date**: January 10, 2026
**Version**: 1.0.0
**Status**: Ready for QA Testing


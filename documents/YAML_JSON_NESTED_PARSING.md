# YAML and JSON Nested Structure Parsing

## Overview

The Service Onboarding feature now supports **nested YAML and JSON structures** that are automatically flattened into dot-notation keys for storage in the database.

## How It Works

### Input Format (YAML)
```yaml
spring:
    application:
        name: spring-mongo
    data:
        jdbcurl: jdbc:mysql://localhost:3306/mydatabase
        username: myusername
        password: '{env}mysecretpassword'
        mongodb:
            uri: mongodb://localhost:27017/mydatabase
            database: mydatabase
            collection: mycollection
            password: mypassword

asdsads:
    sdsdsds: ssdsdjds
```

### Database Output
```
| propertyKey                     | propertyValue                              |
| ------------------------------- | ------------------------------------------ |
| spring.application.name         | spring-mongo                               |
| spring.data.jdbcurl             | jdbc:mysql://localhost:3306/mydatabase    |
| spring.data.username            | myusername                                 |
| spring.data.password            | '{env}mysecretpassword'                    |
| spring.data.mongodb.uri         | mongodb://localhost:27017/mydatabase      |
| spring.data.mongodb.database    | mydatabase                                 |
| spring.data.mongodb.collection  | mycollection                               |
| spring.data.mongodb.password    | mypassword                                 |
| asdsads.sdsdsds                 | ssdsdjds                                   |
```

## Supported File Formats

### 1. **Properties Files (.properties)**
Flat key-value format (already supported, unchanged):
```properties
spring.application.name=spring-mongo
spring.data.jdbcurl=jdbc:mysql://localhost:3306/mydatabase
server.port=8080
```

### 2. **YAML Files (.yaml, .yml)**
Nested hierarchical format (now with nested support):
```yaml
spring:
    application:
        name: spring-mongo
    data:
        mongodb:
            uri: mongodb://localhost:27017
```

**Flattened to:**
```
spring.application.name = spring-mongo
spring.data.mongodb.uri = mongodb://localhost:27017
```

### 3. **JSON Files (.json)**
Nested object format (now with nested support):
```json
{
  "spring": {
    "application": {
      "name": "spring-mongo"
    },
    "data": {
      "mongodb": {
        "uri": "mongodb://localhost:27017"
      }
    }
  }
}
```

**Flattened to:**
```
spring.application.name = spring-mongo
spring.data.mongodb.uri = mongodb://localhost:27017
```

## Implementation Details

### YAML Parser (parseYamlFile)
The YAML parser uses **indentation-based tracking** to build the key hierarchy:

1. **Line-by-line processing**: Reads each line of the YAML file
2. **Indentation tracking**: Calculates the indentation level to determine nesting depth
3. **Stack management**: Uses a stack to track the current key path
4. **Dot-notation construction**: Joins stacked keys with dots to create the full property key
5. **Value extraction**: Extracts values and stores them with their full dotted key

**Example Processing:**
```
Line 1: "spring:"          → Push "spring" to stack
Line 2: "    application:" → Push "application" to stack
Line 3: "        name: spring-mongo" → Key path: spring.application.name
```

### JSON Parser (parseJsonFile)
The JSON parser uses **recursive traversal** to flatten nested objects:

1. **Recursive traversal**: Walks through all object properties
2. **Prefix building**: Builds the key path as it traverses
3. **Leaf detection**: When a leaf value is found, stores it with the full path
4. **Dot-notation construction**: Joins path components with dots

**Key Features:**
- Handles deeply nested objects of any depth
- Skips arrays (not processed)
- Recursively flattens all object levels

### Flattening Algorithm
Both parsers use a similar flattening approach:

```
Input:  { "a": { "b": { "c": "value" } } }
        or
        a:
          b:
            c: value

Process:
  → prefix: ""
  → prefix: "a"
  → prefix: "a.b"
  → prefix: "a.b.c" → store "a.b.c": "value"

Output: { "a.b.c": "value" }
```

## Code Changes

### ConfigService.kt

**Modified Methods:**
1. `parseYamlFile(content: String): Map<String, String>`
   - Now handles nested YAML structures
   - Uses stack-based indentation tracking
   - Flattens to dot notation

2. `parseJsonFile(content: String): Map<String, String>`
   - Now handles nested JSON objects
   - Uses recursive traversal
   - Flattens to dot notation

3. `flattenJsonNode(prefix, node, properties)` (NEW)
   - Helper method for recursive JSON flattening
   - Processes nested objects and leaf values

## Example: Service Onboarding Flow

### Step 1: User Uploads YAML File
```yaml
database:
    host: localhost
    port: 5432
    credentials:
        username: admin
        password: secret123
```

### Step 2: Backend Parses the File
The `parseYamlFile()` method:
- Reads the YAML structure
- Builds the key paths: `database.host`, `database.port`, `database.credentials.username`, `database.credentials.password`
- Extracts the values

### Step 3: Data Stored in Database
```
| propertyKey                    | propertyValue |
| ------------------------------ | ------------- |
| database.host                  | localhost     |
| database.port                  | 5432          |
| database.credentials.username  | admin         |
| database.credentials.password  | secret123     |
```

### Step 4: Query and Retrieve
When users retrieve properties:
- They see all flattened keys with their values
- Can modify individual values
- Can delete individual keys
- Audit trail tracks all changes

## Benefits

1. **Hierarchical Organization**: Supports complex nested configurations like Spring Boot's `application.yml`
2. **Flat Storage Model**: Stores all values in a single table with dot-notation keys
3. **Simple Querying**: Easy to search and filter by key prefix
4. **Standard Format**: Matches the way cloud platforms and configuration servers handle properties
5. **Backwards Compatible**: Properties files continue to work as before

## Edge Cases Handled

### Empty Values (YAML)
```yaml
parent:
    empty:
    child: value
```
- `empty:` with no value is treated as a parent key (skipped)
- `child: value` is stored as `parent.child`

### Comments
```yaml
# This is a comment
spring:
    # Nested comment
    name: app
```
- Comment lines (starting with #) are ignored

### Quoted Values
```yaml
spring:
    password: "'{env}mysecretpassword'"
```
- Single and double quotes are removed during parsing
- Value stored as: `'{env}mysecretpassword'`

### Null/Empty JSON Values
```json
{
  "spring": {
    "active": null,
    "name": "app"
  }
}
```
- Null values are stored as "null" string
- Empty strings are stored as empty values

## Testing

### Test Case 1: Nested YAML
**Input File:**
```yaml
app:
    server:
        port: 8080
        ssl:
            enabled: true
            keystore: /path/to/keystore
```

**Expected Database Entries:**
- `app.server.port` → `8080`
- `app.server.ssl.enabled` → `true`
- `app.server.ssl.keystore` → `/path/to/keystore`

### Test Case 2: Nested JSON
**Input File:**
```json
{
  "database": {
    "primary": {
      "host": "db1.example.com",
      "port": 5432
    },
    "secondary": {
      "host": "db2.example.com",
      "port": 5432
    }
  }
}
```

**Expected Database Entries:**
- `database.primary.host` → `db1.example.com`
- `database.primary.port` → `5432`
- `database.secondary.host` → `db2.example.com`
- `database.secondary.port` → `5432`

## Limitations & Future Enhancements

### Current Limitations
1. **Arrays**: JSON arrays are skipped (not flattened to indexed keys)
2. **Deep Nesting**: No practical limit, but very deep structures may affect readability

### Potential Future Enhancements
1. **Array Support**: Flatten arrays to indexed keys (e.g., `servers[0]`, `servers[1]`)
2. **Type Preservation**: Store value types (int, boolean, etc.) instead of treating everything as strings
3. **TOML Support**: Add support for TOML configuration format
4. **Variable Interpolation**: Support environment variable placeholders like `${env.VAR_NAME}`
5. **Validation**: Add schema validation before storing properties

## File Parsing Flow (Diagram)

```
User uploads file
        ↓
Determine file type (.properties, .yaml, .json)
        ↓
Parse based on type:
    ├─ .properties → parsePropertiesFile()
    ├─ .yaml/.yml  → parseYamlFile()
    └─ .json       → parseJsonFile()
        ↓
Flatten to Map<String, String> with dot-notation keys
        ↓
Validate (no empty keys, no empty values)
        ↓
Insert into database
        ↓
Create audit records
        ↓
Return success response to UI
```

## API Endpoint

**POST /api/config/onboard**

Request:
```
Form Data:
- domain: "production"
- application: "user-service"
- file: <YAML/JSON/Properties file>
```

Response (201 Created):
```json
{
  "properties": [
    {
      "propertyKey": "spring.application.name",
      "propertyValue": "user-service",
      "applicationName": "user-service",
      "domain": "production",
      "createdBy": "system",
      "createdTm": "2026-01-10T12:00:00Z",
      "updatedBy": "system",
      "updatedTm": "2026-01-10T12:00:00Z"
    },
    // ... more properties
  ],
  "syncInfo": {
    "applicationName": "user-service",
    "domain": "production",
    "versionNumber": 1,
    "updatedBy": "system",
    "updatedTm": "2026-01-10T12:00:00Z"
  }
}
```

## Summary

The YAML and JSON parsing now fully supports **nested hierarchical structures** and automatically flattens them into a simple dot-notation key-value format for database storage. This approach:

- ✅ Supports complex configurations (like Spring Boot's `application.yml`)
- ✅ Stores data in a simple, queryable format
- ✅ Maintains backwards compatibility with properties files
- ✅ Enables easy search and filtering by key prefix
- ✅ Integrates seamlessly with the existing database schema

---

**Implementation Date**: January 10, 2026
**Status**: ✅ Complete and Tested
**Build Status**: ✅ Successful


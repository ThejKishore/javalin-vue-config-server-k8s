### Requirement
Create a Platform Shared service in Javalin (kotlin).
Use jdbi , Koin , Vue3, h2 db ,style using tailwindcss
A Vue Layout menu in sidebar

#### 1 ConfigServer

Requirements for Config Server.

##### 1.1 Table Storage Requirements

    It should persists the data to the db 
    There should be tables 
    1. Table Name : AppConfig
        Column name : ApplicationName - varchar2
        Column name : Domain          - varchar(5)
        Column name : propertyKey     - varchar2
        Column name : propertyValue.  - varchar2
        Column name : createdBy       - varchar2
        Column name : createdTm       - timestamp
        Column name : updatedBy.      - varchar2
        Column name : updatedTm.      - timestamp

    2. Table Name : ConfigSync
        Column name : ApplicationName -varchar2
        Column name : Domain          - varchar(5)
        Column name : versionNumber   - int
        Column name : updatedBy.      - varchar2
        Column name : updatedTm.      - timestamp

    3. Table Name : AppConfigAudit
        Column name : ApplicationName    - varchar2
        Column name : Domain             - varchar(5)
        Column name : propertyKey        - varchar2
        Column name : oldpropertyValue.  - varchar2
        Column name : newpropertyValue.  - varchar2
        Column name : operation.         - enum (added|modified|deleted)
        Column name : updatedBy.         - varchar2
        Column name : updatedTm.         - timestamp
        Column name : versionNumber      - int

##### 1.2 It should have endpoints following endpoints

    1 . get - > /config/yml/{domain}/{application}
    should return yaml from the AppConfig using ApplicationName , Domain
    should also include the ConfigSync versionNo , updatedTm 

    2 . get -> /config/sync/{domain}/{application} -- this will be used by sidecar to poll for version change 
    should return the data in json from  ConfigSync versionNo , updatedTm 

    3. get -> /config/meta -- this should return a json that will be used in the ConfigServer Mgmt view where user could select the domain and application within domain.
    This would be used within the Domain dropdown list , application dropdown list

    4. get - > /config/properties/{domain}/{application}
    should return properties like json from the AppConfig using ApplicationName , Domain
    should also include the ConfigSync versionNo , updatedTm currently serving
    this would be used to render in the table inside the Config Server Mgmt view.

    5. put - > /config/properties/{domain}/{application}
    an endpoint to do bulk update of propertvalues 
    This would be used in the table inside the Config Server Mgmt view to update the values.
    And on click of publish changes it would go to backend check there is no recent update happened using the versionnumber & updateTm in the ConfigSync.
    And will do a batch update to the AppConfig table for the give {domain}/{application}

    6. post - > /config/properties/{domain}/{application}
    an endpoint to add new propertykey and propertyvalue to the AppConfig table for the give {domain}/{application}
    This would be used in the Config Server Mgmt view using a form to add the key value pairs.
    And on click of submit changes it would go to backend check there is no duplicate key in the AppConfig table  or the give {domain}/{application} , 
    then insert the record and update the versionnumber by incrementing & updateTm in the ConfigSync.

    All these operation should have the AppConfigAudit entry as well

there should be a new screen that allows user to view the history of AppConfigAudit in a table sort in desecnding based on the versionnumber

make the layout extenedable for future admin screens.

#### 2 RestClient UI for admin to test


This is multimodule project , main app is in the root src 
and the restclient is in the restclient module
and the vue app is in the main module
and the configserver is in the configserver module
shared is where the models are defined 
use domain layering to separate the concerns

### Reference
[javalin-vue3](https://javalin.io/tutorials/simple-frontends-with-javalin-and-vue)
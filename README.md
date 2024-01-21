# Rest_Assured_API_Automation_Project
This project is the automation of the [API Postman Project](https://github.com/StargazerTEF/Postman_project_API_automation) using Rest Assure library in Java.

### Getting started

- Download zipped [API project](https://github.com/davellanedam/node-express-mongodb-jwt-rest-api-skeleton)
- Unpack the zipped file
- In the extracted folder you will find a file `.env.example`
- Create a new file by copying and pasting the file and then renaming it to `.env`
- Open terminal from the project folder and run following commands respectively: `npm install`, `npm run fresh` and `npm run dev` to run a server.
- Server will be started with url `http://localhost:3000` which is a base url for the project

### Dependencies used
- Java 
- Maven
- RestAssured
- Hamcrest
- Jackson-databind
- TestNG
- Javafaker 

### API features

- Custom email/password user system with basic security
- HTTP request logger in development mode
- User roles
- User profile
- Users list for admin area
- Cities model
- Testing with mocha/chai for API endpoints
- Ability to refresh token
- JWT Tokens, make requests with a token after login with Authorization header with value Bearer yourToken where yourToken is the signed and encrypted token given in the response from the login process

#### Tests are categorized in the following groups:
- Authorization tests: API requests for login, registration, verification, password reset and refreshing tokens
- City tests: API requests for cities data, filtering, creating new cities, editing and deleting created cities
- Profile tests: API requests for registering profile, login, getting profile information, editing profile and changing password
- User tests: API requests for creating users, login, filtering results, editing data and deleting created users

### Running the tests

- Tests are being organized and implemented using `testng.xml`. To run the tests from the command line use:   
  `mvn clean test -Dsurefire.suiteXmlFiles=testng.xml`
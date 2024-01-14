package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;
import payload.Payload;
import reusableMethods.ReusableMethods;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static io.restassured.RestAssured.given;

public class AuthTests {
    @Test(priority = 1)
    public void getHomeAPIUrl() {
        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .when().get()
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        Assert.assertTrue(response.contains("API home"));
    }

    @Test(priority = 2)
    public void postEmptyLogin() throws IOException {
        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .when().post("/login")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        int numberOfMessages = js.get("errors.msg.msg.size()");

        ArrayList<String> expectedMessages = new ArrayList<>(Arrays.asList("MISSING", "IS_EMPTY", "EMAIL_IS_NOT_VALID", "MISSING", "IS_EMPTY", "PASSWORD_TOO_SHORT_MIN_5"));

        ArrayList<String> errorMessages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            errorMessages.add(js.getString("errors.msg.msg[" + i + "]"));

        }

        boolean doErrorMessagesMatch = true;

        for (int i = 0; i < numberOfMessages; i++) {
            if (!expectedMessages.get(i).equals(errorMessages.get(i))) {
                doErrorMessagesMatch = false;
                break;
            }
        }

        Assert.assertTrue(doErrorMessagesMatch);
    }

    @Test(priority = 3)
    public void postAdminLogin() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.adminLoginRequestBody())
                .when().post("/login")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String adminToken = js.get("token");
        PrintWriter out = new PrintWriter("data/adminToken.txt");
        out.print(adminToken);
        out.close();
    }

    @Test(priority = 4)
    public void postValidRegistration() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.userRegistrationRequestBody(ReusableMethods.randomEmail(), "pass123"))
                .when().post("/register")
                .then().log().all().assertThat().statusCode(201).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String userToken = js.get("token");
        String userId = js.get("user._id");
        String newUserName = js.get("user.name");
        String newUserEmail = js.get("user.email");
        String newUserRole = js.get("user.role");
        String newUserPassword = "pass123";
        String userVerification = js.get("user.verification");

        PrintWriter out = new PrintWriter("data/userToken.txt");
        out.print(userToken);
        out.close();

        PrintWriter out1 = new PrintWriter("data/userVerification.txt");
        out1.print(userVerification);
        out1.close();

        PrintWriter out2 = new PrintWriter("data/userId.txt");
        out2.print(userId);
        out2.close();

        PrintWriter out3 = new PrintWriter("data/newUserName.txt");
        out3.print(newUserName);
        out3.close();

        PrintWriter out4 = new PrintWriter("data/newUserEmail.txt");
        out4.print(newUserEmail);
        out4.close();

        PrintWriter out5 = new PrintWriter("data/newUserPassword.txt");
        out5.print(newUserPassword);
        out5.close();

        Assert.assertEquals(new String(Files.readAllBytes(Paths.get("data/newUserName.txt"))), newUserName);
        Assert.assertEquals(new String(Files.readAllBytes(Paths.get("data/newUserEmail.txt"))), newUserEmail);
        Assert.assertEquals(newUserRole, "user");
    }

    @Test(priority = 5)
    public void postExistingEmailRegistration() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.userRegistrationRequestBody(new String(Files.readAllBytes(Paths.get("data/newUserEmail.txt"))), ReusableMethods.randomPassword()))
                .when().post("/register")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg");

        Assert.assertEquals(errorMessage, "EMAIL_ALREADY_EXISTS");
    }

    @Test(priority = 6)
    public void postValidVerification() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.newUserValidationRequestBody())
                .when().post("/verify")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        boolean isVerified = js.get("verified");

        Assert.assertTrue(isVerified);
    }

    @Test(priority = 7)
    public void postInvalidVerification() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.newUserValidationRequestBody())
                .when().post("/verify")
                .then().log().all().assertThat().statusCode(404).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg");

        Assert.assertEquals(errorMessage, "NOT_FOUND_OR_ALREADY_VERIFIED");
    }

    @Test(priority = 8)
    public void postForgotWithValidEmail() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.validUserEmail())
                .when().post("/forgot")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);

        String resetVerification = js.get("verification");
        PrintWriter out = new PrintWriter("data/resetVerification.txt");
        out.print(resetVerification);
        out.close();

        String message = js.get("msg");

        Assert.assertEquals(message, "RESET_EMAIL_SENT");
    }

    @Test(priority = 9)
    public void postReset() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.resetPassword())
                .when().post("/reset")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String message = js.get("msg");

        Assert.assertEquals(message, "PASSWORD_CHANGED");
    }

    @Test(priority = 10)
    public void getToken() {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .when().get("/token")
                .then().log().all().assertThat().statusCode(401).extract().response().asString();

        Assert.assertEquals(response, "Unauthorized");
    }

    @Test(priority = 11)
    public void getNewToken() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .when().get("/token")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String userToken = js.get("token");
        PrintWriter out = new PrintWriter("data/userToken.txt");
        out.println(userToken);
        out.close();

        Assert.assertTrue(response.contains("token"));
    }

    @Test(priority = 12)
    public void deleteRegisteredUser() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .when().delete(Payload.getUserByIdMethod())
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String message = js.get("msg");

        Assert.assertEquals(message, "DELETED");

        File f0 = new File("data/userToken.txt");
        File f1 = new File("data/userId.txt");
        File f2 = new File("data/userVerification.txt");
        File f3 = new File("data/resetVerification.txt");
        File f4 = new File("data/newUserPassword.txt");
        File f5 = new File("data/newUserName.txt");
        File f6 = new File("data/newUserEmail.txt");
        File f7 = new File("data/adminToken.txt");

        ArrayList<File> files = new ArrayList<>(Arrays.asList(f0, f1, f2, f3, f4, f5, f6, f7));

        for (File file : files) {
            file.delete();
        }
    }
}

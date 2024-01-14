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

public class ProfileTests {
    @Test(priority = 1)
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

    @Test(priority = 2)
    public void postValidRegistration() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.userRegistrationRequestBody(ReusableMethods.randomEmail(), "11111"))
                .when().post("/register")
                .then().log().all().assertThat().statusCode(201).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String userId = js.get("user._id");
        PrintWriter out1 = new PrintWriter("data/userId.txt");
        out1.print(userId);
        out1.close();

        String newUserName = js.get("user.name");
        PrintWriter out2 = new PrintWriter("data/newUserName.txt");
        out2.print(newUserName);
        out2.close();

        String email = js.get("user.email");
        PrintWriter out3 = new PrintWriter("data/newUserEmail.txt");
        out3.print(email);
        out3.close();

        String password = "11111";
        PrintWriter out = new PrintWriter("data/newUserPassword.txt");
        out.print(password);
        out.close();

        Assert.assertTrue(response.contains("token"));
    }

    @Test(priority = 3)
    public void postLogin() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .body(Payload.validUserLoginRequestBody())
                .when().post("/login")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String userToken = js.get("token");
        PrintWriter out = new PrintWriter("data/userToken.txt");
        out.print(userToken);
        out.close();

        String userName = js.get("user.name");

        Assert.assertEquals(new String(Files.readAllBytes(Paths.get("data/newUserName.txt"))), userName);
    }

    @Test(priority = 4)
    public void getProfileWithoutAuth() {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .when().get("/profile")
                .then().log().all().assertThat().statusCode(401).extract().response().asString();

        Assert.assertEquals(response, "Unauthorized");
    }

    @Test(priority = 5)
    public void getLoggedInProfile() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .when().get("/profile")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        Assert.assertTrue(response.contains("name"));
        Assert.assertTrue(response.contains("email"));
    }

    @Test(priority = 6)
    public void patchProfileWithoutCredentials() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .when().patch("/profile")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        Assert.assertTrue(response.contains("errors"));
    }

    @Test(priority = 7)
    public void patchProfileSuccessfully() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .body(Payload.patchProfileWithValidInfo())
                .when().patch("/profile")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String name = js.get("name");
        String urlTwitter = js.get("urlTwitter");
        String urlGitHub = js.get("urlGitHub");
        String phone = js.get("phone");
        String city = js.get("city");
        String country = js.get("country");

        Assert.assertEquals(Payload.patchProfileWithValidInfo().get("name"), name);
        Assert.assertEquals(Payload.patchProfileWithValidInfo().get("urlTwitter"), urlTwitter);
        Assert.assertEquals(Payload.patchProfileWithValidInfo().get("urlGitHub"), urlGitHub);
        Assert.assertEquals(Payload.patchProfileWithValidInfo().get("phone"), phone);
        Assert.assertEquals(Payload.patchProfileWithValidInfo().get("city"), city);
        Assert.assertEquals(Payload.patchProfileWithValidInfo().get("country"), country);
    }

    @Test(priority = 8)
    public void patchProfileWithInvalidInfo() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .body(Payload.patchProfileWithInvalidInfo())
                .when().patch("/profile")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg[0].msg");

        Assert.assertEquals(errorMessage, "NOT_A_VALID_URL");
    }

    @Test(priority = 9)
    public void changePasswordWithInvalidInput() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .body(Payload.changePasswordRequestBody("invalidPassword", ReusableMethods.randomPassword()))
                .when().post("/profile/changePassword")
                .then().log().all().assertThat().statusCode(409).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg");

        Assert.assertEquals(errorMessage, "WRONG_PASSWORD");
    }

    @Test(priority = 10)
    public void changePasswordWithFewerCharactersThanDemanded() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .body(Payload.changePasswordRequestBody(new String(Files.readAllBytes(Paths.get("data/newUserPassword.txt"))), "1234"))
                .when().post("/profile/changePassword")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg[0].msg");

        Assert.assertEquals(errorMessage, "PASSWORD_TOO_SHORT_MIN_5");
    }

    @Test(priority = 11)
    public void changePasswordWithValidInput() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .body(Payload.changePasswordRequestBody(new String(Files.readAllBytes(Paths.get("data/newUserPassword.txt"))), ReusableMethods.randomPassword()))
                .when().post("/profile/changePassword")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String message = js.get("msg");

        Assert.assertEquals(message, "PASSWORD_CHANGED");
    }

    @Test(priority = 12)
    public void deleteProfile() throws IOException {

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
        File f2 = new File("data/newUserEmail.txt");
        File f3 = new File("data/newUserName.txt");
        File f4 = new File("data/newUserPassword.txt");
        File f5 = new File("data/adminToken.txt");

        ArrayList<File> files = new ArrayList<>(Arrays.asList(f0, f1, f2, f3, f4, f5));

        for (File file : files) {
            file.delete();
        }
    }
}

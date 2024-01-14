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

public class UserTests {
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
                .body(Payload.userRegistrationRequestBody(ReusableMethods.randomEmail(), "54321"))
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

        String password = "54321";
        PrintWriter out = new PrintWriter("data/newUserPassword.txt");
        out.print(password);
        out.close();

        Assert.assertTrue(response.contains("_id"));
        Assert.assertTrue(response.contains("name"));
        Assert.assertTrue(response.contains("email"));
        Assert.assertTrue(response.contains("verification"));
    }

    @Test(priority = 3)
    public void postUserLogin() throws IOException {

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

        Assert.assertTrue(response.contains("token"));
    }

    @Test(priority = 4)
    public void getUsersWithoutAuth() {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .when().get("/users")
                .then().log().all().assertThat().statusCode(401).extract().response().asString();

        Assert.assertEquals(response, "Unauthorized");
    }

    @Test(priority = 5)
    public void getUsersWithAdminAuth() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .when().get("/users")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        Assert.assertTrue(response.contains("docs"));
    }

    @Test(priority = 6)
    public void getUsersWithEmailFilter() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .queryParam("filter", "admin")
                .queryParam("fields", "email")
                .when().get("/users")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String email = js.get("docs[0].email");
        int numberOfDocs = js.getInt("totalDocs");

        Assert.assertTrue(response.contains("docs"));
        Assert.assertEquals(numberOfDocs, 1);
        Assert.assertTrue(email.contains("admin"));
    }

    @Test(priority = 7)
    public void getUsersWithRoleFilter() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .queryParam("filter", "user")
                .queryParam("fields", "role")
                .when().get("/users")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        int numberOfDocs = js.getInt("totalDocs");

        boolean areAllRolesUsers = true;

        for (int i = 0; i < numberOfDocs; i++) {
            if (!js.get("docs[" + i + "].role").equals("user")) {
                areAllRolesUsers = false;
                break;
            }
        }

        Assert.assertTrue(areAllRolesUsers);
    }

    @Test(priority = 8)
    public void postUserWithoutName() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .when().post("/users")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        Assert.assertTrue(response.contains("errors"));
    }

    @Test(priority = 9)
    public void postUserWithExistingEmail() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .body(Payload.postOrPatchUserRequestBody(ReusableMethods.randomFullName(), "user", new String(Files.readAllBytes(Paths.get("data/newUserEmail.txt")))))
                .when().post("/users")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg");

        Assert.assertEquals(errorMessage, "EMAIL_ALREADY_EXISTS");
    }

    @Test(priority = 10)
    public void postUserWithWrongRole() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .body(Payload.postOrPatchUserRequestBody(ReusableMethods.randomFullName(), "user1", ReusableMethods.randomEmail()))
                .when().post("/users")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg[0].msg");

        Assert.assertEquals(errorMessage, "USER_NOT_IN_KNOWN_ROLE");
    }

    @Test(priority = 11)
    public void getUserById() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .when().get(Payload.getUserByIdMethod())
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        Assert.assertTrue(response.contains("_id"));
        Assert.assertTrue(response.contains("name"));
    }

    @Test(priority = 12)
    public void patchUserById() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .body(Payload.postOrPatchUserRequestBody("Maja Majic", "user", "makica@gmail.com"))
                .when().patch(Payload.getUserByIdMethod())
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String userId = js.get("_id");
        String name = js.get("name");
        String email = js.get("email");

        Assert.assertEquals(new String(Files.readAllBytes(Paths.get("data/userId.txt"))), userId);
        Assert.assertEquals(name, "Maja Majic");
        Assert.assertEquals(email, "makica@gmail.com");
    }

    @Test(priority = 13)
    public void patchUserWithExistingEmail() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .body(Payload.postOrPatchUserRequestBody(ReusableMethods.randomFullName(), "user", "user@user.com"))
                .when().patch(Payload.getUserByIdMethod())
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg");

        Assert.assertEquals(errorMessage, "EMAIL_ALREADY_EXISTS");
    }

    @Test(priority = 14)
    public void patchUserWithUserAuth() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithUserToken())
                .body(Payload.postOrPatchUserRequestBody(ReusableMethods.randomFullName(), "user", ReusableMethods.randomEmail()))
                .when().patch(Payload.getUserByIdMethod())
                .then().log().all().assertThat().statusCode(401).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg");

        Assert.assertEquals(errorMessage, "UNAUTHORIZED");
    }

    @Test(priority = 15)
    public void deleteUser() throws IOException {

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
        File f3 = new File("data/adminToken.txt");
        File f4 = new File("data/newUserPassword.txt");
        File f5 = new File("data/newUserName.txt");

        ArrayList<File> files = new ArrayList<>(Arrays.asList(f0, f1, f2, f3, f4, f5));

        for (File file : files) {
            file.delete();
        }
    }
}

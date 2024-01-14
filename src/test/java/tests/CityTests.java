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

public class CityTests {
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
    public void getCitiesWithoutAuth() {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .when().get("/cities")
                .then().log().all().assertThat().statusCode(401).extract().response().asString();

        Assert.assertEquals(response, "Unauthorized");
    }

    @Test(priority = 3)
    public void getCitiesWithAuth() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .when().get("/cities")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        int numberOfCities = js.get("totalDocs");

        Assert.assertEquals(numberOfCities, 10);
    }

    @Test(priority = 4)
    public void getCitiesWithFilter() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .queryParam("filter", "Bucaramanga")
                .queryParam("fields", "name")
                .when().get("/cities")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        int numberOfCities = js.get("totalDocs");
        String cityName = js.get("docs[0].name");

        Assert.assertEquals(numberOfCities, 1);
        Assert.assertEquals(cityName, "Bucaramanga");
    }

    @Test(priority = 5)
    public void getCitiesWithLimit() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .queryParam("limit", 3)
                .when().get("/cities")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        int numberOfCities = js.get("totalDocs");
        int numberOfCitiesInResponseBody = js.get("docs.size()");

        Assert.assertEquals(numberOfCities, 10);
        Assert.assertEquals(numberOfCitiesInResponseBody, 3);
    }

    @Test(priority = 6)
    public void getCitiesSortedByName() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .queryParam("sort", "name")
                .queryParam("order", 1)
                .when().get("/cities")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        int numberOfCitiesInResponseBody = js.get("docs.size()");

        boolean areCitiesInAnAscendingOrder = true;

        for (int i = 0; i < numberOfCitiesInResponseBody - 1; i++) {
            if (js.getString("docs[" + i + "].name").compareTo(js.getString("docs[" + (i + 1) + "].name")) > 0) {
                areCitiesInAnAscendingOrder = false;
                break;
            }
        }

        Assert.assertEquals(numberOfCitiesInResponseBody, 5);
        Assert.assertTrue(areCitiesInAnAscendingOrder);
    }

    @Test(priority = 7)
    public void postCityWithoutName() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .when().post("/cities")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMsg = js.get("errors.msg[0].msg");
        String errorParam = js.getString("errors.msg[0].param").toUpperCase();
        String errorMessage = errorMsg + " " + errorParam;

        Assert.assertEquals(errorMessage, "MISSING NAME");
    }

    @Test(priority = 8)
    public void postCityWithValidName() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .body(Payload.postOrPatchCityNameRequestBody(ReusableMethods.randomCity()))
                .when().post("/cities")
                .then().log().all().assertThat().statusCode(201).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String cityName = js.get("name");
        PrintWriter out = new PrintWriter("data/cityName.txt");
        out.print(cityName);
        out.close();

        String cityId = js.get("_id");
        PrintWriter out1 = new PrintWriter("data/cityId.txt");
        out1.print(cityId);
        out1.close();

        Assert.assertTrue(response.contains("name"));
        Assert.assertTrue(response.contains("_id"));
    }

    @Test(priority = 9)
    public void postCityWithExistingName() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .body(Payload.postOrPatchCityNameRequestBody(new String(Files.readAllBytes(Paths.get("data/cityName.txt")))))
                .when().post("/cities")
                .then().log().all().assertThat().statusCode(422).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String errorMessage = js.get("errors.msg");

        Assert.assertEquals(errorMessage, "CITY_ALREADY_EXISTS");
    }

    @Test(priority = 10)
    public void getCityById() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .when().get(Payload.getCityByIdMethod())
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String cityName = js.get("name");

        Assert.assertEquals(new String(Files.readAllBytes(Paths.get("data/cityName.txt"))), cityName);
    }

    @Test(priority = 11)
    public void patchCityById() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all().header("Content-Type", "application/json")
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .body(Payload.postOrPatchCityNameRequestBody("Belgrade"))
                .when().patch(Payload.getCityByIdMethod())
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String cityName = js.getString("name");

        boolean isEqual = cityName.equals(new String(Files.readAllBytes(Paths.get("data/cityName.txt"))));
        System.out.println(isEqual);

        Assert.assertEquals(cityName, "Belgrade");
    }

    @Test(priority = 12)
    public void deleteCityById() throws IOException {

        RestAssured.baseURI = "http://localhost:3000";
        String response = given().log().all()
                .header("Authorization", Payload.authorizationHeaderWithAdminToken())
                .when().delete(Payload.getCityByIdMethod())
                .then().log().all().assertThat().statusCode(200).extract().response().asString();

        JsonPath js = new JsonPath(response);
        String message = js.getString("msg");

        Assert.assertEquals(message, "DELETED");

        File f0 = new File("data/adminToken.txt");
        File f1 = new File("data/cityId.txt");
        File f2 = new File("data/cityName.txt");

        ArrayList<File> files = new ArrayList<>(Arrays.asList(f0, f1, f2));

        for (File file : files) {
            file.delete();
        }
    }
}

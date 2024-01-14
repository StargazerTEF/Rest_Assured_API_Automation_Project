package payload;

import reusableMethods.ReusableMethods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Payload {
    public static HashMap<String, String> adminLoginRequestBody() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", "admin@admin.com");
        map.put("password", "12345");
        return map;
    }

    public static HashMap<String, String> userRegistrationRequestBody(String email, String password) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", ReusableMethods.randomFullName());
        map.put("email", email);
        map.put("password", password);
        return map;
    }

    public static HashMap<String, String> newUserValidationRequestBody() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", new String(Files.readAllBytes(Paths.get("data/userVerification.txt"))));
        return map;
    }

    public static HashMap<String, String> validUserEmail() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", new String(Files.readAllBytes(Paths.get("data/newUserEmail.txt"))));
        return map;
    }

    public static HashMap<String, String> resetPassword() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", new String(Files.readAllBytes(Paths.get("data/resetVerification.txt"))));
        map.put("password", new String(Files.readAllBytes(Paths.get("data/newUserPassword.txt"))));
        return map;
    }

    public static String authorizationHeaderWithUserToken() throws IOException {
        return "Bearer " + new String(Files.readAllBytes(Paths.get("data/userToken.txt")));
    }

    public static String authorizationHeaderWithAdminToken() throws IOException {
        return "Bearer " + new String(Files.readAllBytes(Paths.get("data/adminToken.txt")));
    }

    public static String getUserByIdMethod() throws IOException {
        return "/users/" + new String(Files.readAllBytes(Paths.get("data/userId.txt")));
    }


    public static String getCityByIdMethod() throws IOException {
        return "/cities/" + new String(Files.readAllBytes(Paths.get("data/cityId.txt")));
    }


    public static HashMap<String, String> postOrPatchCityNameRequestBody (String name) {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        return map;
    }

    public static HashMap<String, String> validUserLoginRequestBody() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", new String(Files.readAllBytes(Paths.get("data/newUserEmail.txt"))));
        map.put("password", new String(Files.readAllBytes(Paths.get("data/newUserPassword.txt"))));
        return map;
    }

    public static HashMap<String, String> patchProfileWithValidInfo() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "Test123456");
        map.put("urlTwitter", "https://hello.com");
        map.put("urlGitHub", "https://hello.io");
        map.put("phone", "123123123");
        map.put("city", "Bucaramanga");
        map.put("country", "Colombia");
        return map;
    }

    public static HashMap<String, String> patchProfileWithInvalidInfo() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "Test123456");
        map.put("urlTwitter", "invalidUrl");
        map.put("urlGitHub", "alsoInvalid");
        map.put("phone", "123123123");
        map.put("city", "Bucaramanga");
        map.put("country", "Colombia");
        return map;
    }

    public static HashMap<String, String> changePasswordRequestBody(String oldPassword, String newPassword) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("oldPassword", oldPassword);
        map.put("newPassword", newPassword);
        return map;
    }

    public static HashMap<String, String> postOrPatchUserRequestBody(String name, String role, String email) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("role", role);
        map.put("email", email);
        map.put("phone", ReusableMethods.randomPhoneNumber());
        map.put("city", ReusableMethods.randomCity());
        map.put("country", ReusableMethods.randomCountry());
        map.put("password", ReusableMethods.randomPassword());
        return map;
    }
}

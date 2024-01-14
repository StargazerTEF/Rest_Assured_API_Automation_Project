package reusableMethods;

import com.github.javafaker.Faker;

public class ReusableMethods {
    public static String randomFullName() {
        Faker faker = new Faker();
        return faker.name().fullName();
    }

    public static String randomEmail() {
        Faker faker = new Faker();
        return faker.internet().emailAddress();
    }

    public static String randomPassword() {
        Faker faker = new Faker();
        return faker.internet().password();
    }

    public static String randomCity() {
        Faker faker = new Faker();
        return faker.address().city();
    }

    public static String randomPhoneNumber() {
        Faker faker = new Faker();
        return faker.phoneNumber().cellPhone();
    }

    public static String randomCountry() {
        Faker faker = new Faker();
        return faker.country().name();
    }
}

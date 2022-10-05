package get;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import utils.ConfigReader;

import java.util.*;

import static io.restassured.RestAssured.given;

public class Users {

    @Test
    public void listUsersTest(){

        RestAssured.baseURI = ConfigReader.readProperty("baseURL");
        RestAssured.basePath = ConfigReader.readProperty("usersBasePath");

        int pageNumber = 1;
        Response response = given().accept(ContentType.JSON)
                .when().get("?page="+pageNumber+"")
                .then().statusCode(200).extract().response();

        Map<String,Object> parsedResponse = response.as(new TypeRef<Map<String, Object>>() {
        });
        List<Map<String,Object>> allUsers = (List<Map<String, Object>>) parsedResponse.get("data");
//validating page number
        Assert.assertEquals(pageNumber,parsedResponse.get("page"));

        while((int)parsedResponse.get("page")<(int)parsedResponse.get("total_pages")){
            pageNumber++;
            response = given().accept(ContentType.JSON)
                    .when().get("?page="+pageNumber+"")
                    .then().statusCode(200).extract().response();

            parsedResponse = response.as(new TypeRef<Map<String, Object>>() {
            });
            allUsers.addAll((Collection<? extends Map<String, Object>>) parsedResponse.get("data"));
//validating page number/s
            Assert.assertEquals(pageNumber,parsedResponse.get("page"));
        }

//validating user count
        Assert.assertEquals(allUsers.size(),parsedResponse.get("total"));

        Map<String,String> allEmails = new LinkedHashMap<>();

        for(int i=0; i< allUsers.size(); i++){
            Map<String,Object> user = allUsers.get(i);
            int expectedId = i+1;
            String expectedAvatar = "https://reqres.in/img/faces/"+expectedId+"-image.jpg";
            Assert.assertTrue(String.valueOf(user.get("email")).contains(String.valueOf(user.get("first_name")).toLowerCase()));
            Assert.assertTrue(String.valueOf(user.get("email")).contains(String.valueOf(user.get("last_name")).toLowerCase()));
            Assert.assertEquals(expectedId,user.get("id"));
            Assert.assertEquals(expectedAvatar,user.get("avatar"));
//storing user info in map
            String actualName = user.get("first_name")+"."+user.get("last_name");
            allEmails.put(actualName,String.valueOf(user.get("email")));
        }
    }

    @Test
    public void singleUserTest(){

        RestAssured.baseURI = ConfigReader.readProperty("baseURL");
        RestAssured.basePath = ConfigReader.readProperty("usersBasePath");

        for(int id=1; id<=12; id++){
            Response response = given().accept(ContentType.JSON)
                    .when().get("/"+id+"")
                    .then().statusCode(200).extract().response();

            JsonPath jsonPath = response.jsonPath();
            String expectedAvatar = "https://reqres.in/img/faces/"+id+"-image.jpg";
//validating single users
            Assert.assertTrue(jsonPath.getString("data.email").contains(jsonPath.getString("data.first_name").toLowerCase()));
            Assert.assertTrue(jsonPath.getString("data.email").contains(jsonPath.getString("data.last_name").toLowerCase()));
            Assert.assertEquals(id,jsonPath.getInt("data.id"));
            Assert.assertEquals(expectedAvatar,jsonPath.getString("data.avatar"));
            Assert.assertEquals(ConfigReader.readProperty("supportURL"),jsonPath.getString("support.url"));
        }
    }

    @Test
    public void userNotFoundTest(){

        RestAssured.baseURI = ConfigReader.readProperty("baseURL");
        RestAssured.basePath = ConfigReader.readProperty("usersBasePath");

        Response response = given().accept(ContentType.JSON)
                .when().get("/23")
                .then().statusCode(404).extract().response();
        Map parsedResponse = response.as(new TypeRef<Map>() {
        });
//validating empty response body
        Assert.assertTrue(parsedResponse.isEmpty());
    }
}

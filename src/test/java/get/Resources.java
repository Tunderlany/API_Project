package get;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import pojo.DataPojo;
import pojo.ResourcePojo;
import pojo.ResourcesPojo;
import pojo.SupportPojo;
import utils.ConfigReader;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class Resources {

    @Test
    public void listResourcesTest() {

        RestAssured.baseURI = ConfigReader.readProperty("baseURL");
        RestAssured.basePath = ConfigReader.readProperty("resourcesBasePath");

        int pageNumber = 1;
        Response response = given().accept(ContentType.JSON)
                .when().get("?page=" + pageNumber + "")
                .then().statusCode(200).extract().response();
        ResourcesPojo parsedResponse = response.as(ResourcesPojo.class);
        List<DataPojo> allData = parsedResponse.getData();

        while (parsedResponse.getPage() < parsedResponse.getTotal_pages()) {
            pageNumber++;
            response = given().accept(ContentType.JSON)
                    .when().get("?page=" + pageNumber + "")
                    .then().statusCode(200).extract().response();
            parsedResponse = response.as(ResourcesPojo.class);
            allData.addAll(parsedResponse.getData());
        }

        int sumOfIDs = 0;
        double totalYear = 0;

        for (int i = 0; i < allData.size(); i++) {
            sumOfIDs += allData.get(i).getId();
            totalYear += allData.get(i).getYear();
        }
// validating sum of IDs is 78
        Assert.assertEquals(78, sumOfIDs);

// validating average year is 2005.5
        Assert.assertTrue(totalYear / allData.size() == 2005.5);
    }


    @Test
    public void singleResourceTest() {

        RestAssured.baseURI = ConfigReader.readProperty("baseURL");
        RestAssured.basePath = ConfigReader.readProperty("resourcesBasePath");

        for (int id = 1; id <= 12; id++) {

            Response response = given().accept(ContentType.JSON)
                    .when().get("/" + id + "")
                    .then().statusCode(200).extract().response();

            ResourcePojo parsedResponse = response.as(ResourcePojo.class);
            DataPojo data = parsedResponse.getData();
            SupportPojo support = parsedResponse.getSupport();

// validating ID
            Assert.assertEquals(id, data.getId());

// validating year
            Assert.assertEquals(data.getYear(), id + 1999);

// validating support text
            Assert.assertEquals(ConfigReader.readProperty("supportText"), support.getText());

// validating color
            String color = data.getColor();
            Assert.assertEquals(7, color.length());
            Assert.assertEquals('#', color.charAt(0));
            Assert.assertEquals(color.toUpperCase(), color);
        }
    }


    @Test
    public void resourceNotFoundTest() {

        RestAssured.baseURI = ConfigReader.readProperty("baseURL");
        RestAssured.basePath = ConfigReader.readProperty("resourcesBasePath");

        Response response = given().accept(ContentType.JSON)
                .when().get("/23")
                .then().statusCode(404).extract().response();
        Map parsedResponse = response.as(new TypeRef<Map>() {
        });

// validating empty response body
        Assert.assertTrue(parsedResponse.isEmpty());
    }
}

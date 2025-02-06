package FundTransfer.API;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.ArrayList;
import static io.restassured.RestAssured.given;

public class FundTransferTest {

    ArrayList<String> DebitAccountId = new ArrayList<String>();

    ArrayList<String> CreditAccountId = new ArrayList<String>();

    /*
    Account Not Found
     */
    @Test(priority = 1)
    public void getAccountNotFound()
    {
        Response response = given()
                .contentType(ContentType.JSON)
                .get("/account/123456789")
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(), 404);
    }

    /*
    Create USD Account
     */
    @Test(priority = 2)
    public void CreateUSDAccount() {

        String requestBody = "{\"currency\": \"USD\"}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/account")
                .then()
                .extract().response();

            Assert.assertEquals(response.statusCode(), 201);
            DebitAccountId.add(response.jsonPath().getString("id"));
            Assert.assertEquals(response.jsonPath().getString("currency"), "USD");
            Assert.assertEquals(response.jsonPath().getString("balance"), "0");
    }

    /*
    Deposit Funds USD Account
     */
    @Test(priority = 3)
    public void DepositFunds() {

        String requestBody = "{\n" +
                "  \"accountId\": \""+DebitAccountId.get(0)+"\",\n" +
                "  \"amount\": 10000,\n" +
                "  \"currency\": \"USD\"\n" +
                "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("transaction/deposit")
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("currency"), "USD");
        Assert.assertEquals(response.jsonPath().getString("balance"), "10000.0");
    }

    /*
    Create EUR Account
     */
    @Test(priority = 4)
    public void CreateEURAccount() {

        String requestBody = "{\"currency\": \"EUR\"}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/account")
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(), 201);
        CreditAccountId.add(response.jsonPath().getString("id"));
        Assert.assertEquals(response.jsonPath().getString("currency"), "EUR");
        Assert.assertEquals(response.jsonPath().getString("balance"), "0");
    }

    /*
    Transfer Funds USD Account to EUR Account
     */
    @Test(priority = 5)
    public void TransferFunds() {

                String requestBody = "{\n" +
                "  \"debitAccountId\": \""+DebitAccountId.get(0)+"\",\n" +
                "  \"creditAccountId\": \""+CreditAccountId.get(0)+"\",\n" +
                "  \"amount\": 5000,\n" +
                "  \"currency\": \"EUR\"\n" +
                "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("transaction/transfer")
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("currency"), "USD");
        Assert.assertEquals(response.jsonPath().getString("balance"), "4570.0");
    }


    /*
    Withdraw Funds from EUR Account
     */
    @Test(priority = 6)
    public void WithDrawFunds() {

        String requestBody = "{\n" +
                "  \"accountId\": \""+CreditAccountId.get(0)+"\",\n" +
                "  \"amount\": 2500,\n" +
                "  \"currency\": \"EUR\"\n" +
                "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("transaction/withdraw")
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("currency"), "EUR");
        Assert.assertEquals(response.jsonPath().getString("balance"), "2500.0");
    }

    /*
    Get USD Account Balance
     */
    @Test(priority = 7)
    public void GetUSDAccountBalance() {

        Response response = given()
                .contentType(ContentType.JSON)
                .get("/account/"+DebitAccountId.get(0))
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("currency"), "USD");
        Assert.assertEquals(response.jsonPath().getString("balance"), "4570.0");
    }

    /*
    Get EUR Account Balance
     */
    @Test(priority = 8)
    public void GetEURAccountBalance() {

        Response response = given()
                .contentType(ContentType.JSON)
                .get("/account/"+CreditAccountId.get(0))
                .then()
                .extract().response();

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("currency"), "EUR");
        Assert.assertEquals(response.jsonPath().getString("balance"), "2500.0");
    }

    /*
    Insufficient Transfer of Funds
     */
    @Test(priority = 9)
    public void InsufficientTransferFunds() {

        String requestBody = "{\n" +
                "  \"debitAccountId\": \""+DebitAccountId.get(0)+"\",\n" +
                "  \"creditAccountId\": \""+CreditAccountId.get(0)+"\",\n" +
                "  \"amount\": 20000,\n" +
                "  \"currency\": \"EUR\"\n" +
                "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("transaction/transfer")
                .then()
                .extract().response();

        String bodyAsString = response.asString();
        Assert.assertTrue(bodyAsString.contains("Account "+DebitAccountId.get(0)+" has insufficient balance"), "Account "+DebitAccountId.get(0)+" has insufficient balance");
        Assert.assertEquals(response.statusCode(), 400);
    }

    /*
    Create Invalid Exchange Account
     */
    @Test()
    public void CreateAccountXYZ() {

        String requestBody = "{\"currency\": \"XYZ\"}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/account")
                .then()
                .extract().response();

        String bodyAsString = response.asString();
        Assert.assertTrue(bodyAsString.contains("The currency is not an uppercase char sequence of 3 letters, or not supported"), "At least one submitted property has an invalid value");
        Assert.assertEquals(response.statusCode(), 400);
    }

    /*
    Insufficient WithDraw Funds
     */
    @Test(priority = 10)
    public void InsufficientWithDrawFunds() {

        String requestBody = "{\n" +
                "  \"accountId\": \""+CreditAccountId.get(0)+"\",\n" +
                "  \"amount\": 10000,\n" +
                "  \"currency\": \"EUR\"\n" +
                "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("transaction/withdraw")
                .then()
                .extract().response();

        String bodyAsString = response.asString();
        Assert.assertTrue(bodyAsString.contains("Account "+CreditAccountId.get(0)+" has insufficient balance"), "Account "+CreditAccountId.get(0)+" has insufficient balance");
        Assert.assertEquals(response.statusCode(), 400);
    }

    /*
    Transfer Funds to Non Existence Account
     */
    @Test(priority = 11)
    public void TransferFundsNonAccount() {

        String requestBody = "{\n" +
                "  \"debitAccountId\": \""+DebitAccountId.get(0)+"\",\n" +
                "  \"creditAccountId\": \"123456789\",\n" +
                "  \"amount\": 1000,\n" +
                "  \"currency\": \"EUR\"\n" +
                "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("transaction/transfer")
                .then()
                .extract().response();

        String bodyAsString = response.asString();
        //Assert.assertTrue(bodyAsString.contains("At least one referenced account was not found"), "At least one referenced account was not found");
        Assert.assertEquals(response.statusCode(), 404);
    }
}


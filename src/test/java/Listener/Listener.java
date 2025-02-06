package Listener;

import io.restassured.RestAssured;
import org.testng.TestListenerAdapter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;

public class Listener extends TestListenerAdapter {

        @BeforeTest
        @Parameters("BaseURI")
        public void setup(String BaseURI) {
            RestAssured.baseURI = BaseURI;
        }
    }

package com.example;

// #test-top
import akka.actor.typed.ActorRef;
import akka.http.javadsl.model.*;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import org.junit.*;
import org.junit.runners.MethodSorters;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;

// #set-up
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserRoutesTest extends JUnitRouteTest {

    @ClassRule
    public static TestKitJunitResource testkit = new TestKitJunitResource();

    // #test-top
    // shared registry for all tests
    private static ActorRef<BookingRegistry.Command> BookingRegistry;
    private TestRoute appRoute;

    @BeforeClass
    public static void beforeClass() {
        BookingRegistry = testkit.spawn(BookingRegistry.create());
    }

    @Before
    public void before() {
        UserRoutes userRoutes = new UserRoutes(testkit.system(), BookingRegistry);
        appRoute = testRoute(userRoutes.userRoutes());
    }

    @AfterClass
    public static void afterClass() {
        testkit.stop(BookingRegistry);
    }

    // #set-up
    // #actual-test
    @Test
    public void test1NoUsers() {
        appRoute.run(HttpRequest.GET("/users")).assertStatusCode(StatusCodes.OK)
                .assertMediaType("application/json").assertEntity("{\"users\":[]}");
    }

    // #actual-test
    // #testing-post
    @Test
    public void test2HandlePOST() {
        appRoute.run(
                HttpRequest.POST("/users").withEntity(MediaTypes.APPLICATION_JSON.toContentType(),
                        "{\"name\": \"Kapi\", \"age\": 42, \"countryOfResidence\": \"jp\"}"))
                .assertStatusCode(StatusCodes.CREATED).assertMediaType("application/json")
                .assertEntity("{\"description\":\"User Kapi created.\"}");
    }
    // #testing-post

    @Test
    public void test3Remove() {
        appRoute.run(HttpRequest.DELETE("/users/Kapi")).assertStatusCode(StatusCodes.OK)
                .assertMediaType("application/json")
                .assertEntity("{\"description\":\"User Kapi deleted.\"}");

    }
    // #set-up
}
// #set-up

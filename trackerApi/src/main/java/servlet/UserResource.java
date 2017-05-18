/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import com.polsl.roadtracker.trackerapi.model.api.AuthResponse;
import com.google.appengine.repackaged.com.google.api.client.auth.oauth2.Credential;
import com.polsl.roadtracker.trackerapi.dao.UserDatastoreDao;
import com.polsl.roadtracker.trackerapi.model.api.BasicResponse;
import com.polsl.roadtracker.trackerapi.model.api.Credentials;
import com.polsl.roadtracker.trackerapi.model.api.LogoutData;
import com.polsl.roadtracker.trackerapi.model.api.RouteData;
import com.polsl.roadtracker.trackerapi.model.api.SensorResponse;

import com.polsl.roadtracker.trackerapi.model.SensorSettings;
import com.polsl.roadtracker.trackerapi.model.User;
import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import util.TokenGenerator;

/**
 *
 * @author alachman
 */
@Path("/users")
public class UserResource {

//    @GET
//    @Path("/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getUserById(@PathParam("id") Long id) {
//        try {
//            UserDatastoreDao dao = new UserDatastoreDao();
//            User user = dao.getUser(id);
//            return Response.ok(user).build();
//        } catch (Exception e) {
//            return Response.serverError().build();
//        }
//    }
//    @GET
//    @Path("/test")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getTestUser() {
//        User user = new User();
//        user.setEmail("email@email.com");
//        user.setAccelometer(2l);
//        user.setGyroscope(3l);
//        user.setPassword("haslo123");
//        return Response.ok(user).build();
//    }
    @GET
    @Path("/intervals/{authToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public SensorResponse getSensorSettings(@PathParam("authToken") String authToken) {
        UserDatastoreDao dao = new UserDatastoreDao();
        User user = dao.getUserByAuthToken(authToken);
        if (user == null) {
            return new SensorResponse(Response.status(Response.Status.NO_CONTENT).build(),
                    "No such registered user");
        }
        SensorSettings settings = new SensorSettings(user.getAccelometer(), user.getGyroscope(),
                user.getMagneticField(), user.getAmbientTemperature());
        return new SensorResponse(settings, Response.status(Response.Status.OK).build());
    }

    @PUT
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse registerUser(Credentials credentials) {
        try {
            UserDatastoreDao dao = new UserDatastoreDao();
            User user = new User(credentials.getName(), credentials.getEmail(),
                    credentials.getPassword());
            user.setDefaultSensorSettings();
            Long id = dao.createUser(user);
            User createdUser = dao.getUser(id);
            String authToken = TokenGenerator.generateTokenForUser(user.getId());
            createdUser.setAuthToken(authToken);
            dao.updateUser(createdUser);
            return new AuthResponse(Response.status(Response.Status.OK).build(),
                    "", authToken);
        } catch (Exception e) {
            return new AuthResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(),
                    "Error");
        }
    }

    @POST
    @Path("/auth")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public AuthResponse authenticateUser(Credentials credentials) {
        UserDatastoreDao dao = new UserDatastoreDao();
        User user = dao.getUserByEmail(credentials.getEmail());
        if (user == null) {
            return new AuthResponse(Response.status(Response.Status.NO_CONTENT).build(),
                    "No such registered user");
        }
        if (user.getPassword().equals(credentials.getPassword())) {
            String authToken = TokenGenerator.generateTokenForUser(user.getId());
            user.setAuthToken(authToken);
            dao.updateUser(user);
            return new AuthResponse(Response.ok().build(), "", authToken);
        } else {
            return new AuthResponse(Response.status(Response.Status.UNAUTHORIZED).build(),
                    "Wrong password");
        }
    }

    @POST
    @Path("/auth/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public BasicResponse logoutUser(LogoutData data) {
        UserDatastoreDao dao = new UserDatastoreDao();
        User user = dao.getUserByAuthToken(data.getAuthToken());
        if (user == null) {
            return new BasicResponse(Response.status(Response.Status.NO_CONTENT).build(),
                    "No such registered user");
        } else {
            return new BasicResponse(Response.status(Response.Status.OK).build());
        }
    }

    @POST
    @Path("/readings")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public BasicResponse getReadings(RouteData routeData) {
        UserDatastoreDao dao = new UserDatastoreDao();
        User user = dao.getUserByAuthToken(routeData.getAuthToken());
        if (user == null) {
            return new BasicResponse(Response.status(Response.Status.NO_CONTENT).build(),
                    "No such registered user");
        }
        String debugData = user.getDebugData();
        debugData = buildDebugData(debugData, routeData);

        user.setDebugData(debugData);
        dao.updateUser(user);
        return new BasicResponse(Response.status(Response.Status.OK).build());

    }

//    @POST
//    @Path("/login")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response login(@QueryParam("email") String email, @QueryParam("password") String password) {
//        UserDatastoreDao dao = new UserDatastoreDao();
//        User user = dao.getUserByEmail(email);
//        if (user == null) {
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//        if (user.getPassword().equals(password)) {
//            return Response.ok(user.getId()).build();
//        } else {
//            return Response.status(Response.Status.UNAUTHORIZED).build();
//        }
//    }
//    @PUT
//    @Path("/{id}/settings")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateSettings(@PathParam("id") Long id, SensorSettings sensorSettings) {
//        UserDatastoreDao dao = new UserDatastoreDao();
//        User user = dao.getUser(id);
//        user.setAccelometer(sensorSettings.getAccelometer());
//        user.setGyroscope(sensorSettings.getGyroscope());
//        user.setMagneticField(sensorSettings.getMagneticField());
//        user.setAmbientTemperature(sensorSettings.getAmbientTemperature());
//        dao.updateUser(user);
//        return Response.ok().build();
//    }
    private String buildDebugData(String original, RouteData routeData) {
        if (original == null) {
            original = new String();
        }
        StringBuilder builder = new StringBuilder(original);
        builder.append("Part: ");
        builder.append(routeData.getPackageNumber());
        builder.append(" ");
        return builder.toString();
    }
}

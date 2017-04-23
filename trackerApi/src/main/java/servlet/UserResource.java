/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import com.polsl.roadtracker.trackerapi.dao.UserDatastoreDao;

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

/**
 *
 * @author alachman
 */
@Path("/users")
public class UserResource {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("id") Long id) {
        try {
            UserDatastoreDao dao = new UserDatastoreDao();
            User user = dao.getUser(id);
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTestUser() {
        User user = new User();
        user.setEmail("email@email.com");
        user.setAccelometer(2l);
        user.setGyroscope(3l);
        user.setPassword("haslo123");
        return Response.ok(user).build();
    }

    @GET
    @Path("/{id}/settings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorSettings(@PathParam("id") Long id) {
        UserDatastoreDao dao = new UserDatastoreDao();
        User user = dao.getUser(id);
        SensorSettings settings = new SensorSettings(user.getAccelometer(), user.getGyroscope(),
                user.getMagneticField(), user.getAmbientTemperature());
        return Response.ok(settings).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(@QueryParam("email") String email, @QueryParam("password") String password) {
        try {
            UserDatastoreDao dao = new UserDatastoreDao();
            User user = new User(email, password);
            user.setDefaultSensorSettings();
            Long id = dao.createUser(user);
            User createdUser = dao.getUser(id);
            return Response.ok(createdUser.getId()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@QueryParam("email") String email, @QueryParam("password") String password) {
        UserDatastoreDao dao = new UserDatastoreDao();
        User user = dao.getUserByEmail(email);
        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (user.getPassword().equals(password)) {
            return Response.ok(user.getId()).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @PUT
    @Path("/{id}/settings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSettings(@PathParam("id") Long id, SensorSettings sensorSettings) {
        UserDatastoreDao dao = new UserDatastoreDao();
        User user = dao.getUser(id);
        user.setAccelometer(sensorSettings.getAccelometer());
        user.setGyroscope(sensorSettings.getGyroscope());
        user.setMagneticField(sensorSettings.getMagneticField());
        user.setAmbientTemperature(sensorSettings.getAmbientTemperature());
        dao.updateUser(user);
        return Response.ok().build();
    }
}

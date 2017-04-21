/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import com.polsl.roadtracker.trackerapi.model.User;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author alachman
 */

@Path("/user")
public class UserResource {
    
    @GET
    @Path("/data")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getUserData(){
//        User user = new User(12l, "lala", "password", 3l);
        return Response.ok().build();
    }
    
}

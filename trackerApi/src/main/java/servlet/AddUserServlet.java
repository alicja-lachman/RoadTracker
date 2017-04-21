/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.polsl.roadtracker.trackerapi.dao.DatastoreDao;
import com.polsl.roadtracker.trackerapi.dao.UserDao;
import com.polsl.roadtracker.trackerapi.model.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author alachman
 */
@SuppressWarnings("serial")
public class AddUserServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
     
        User user = new User(req.getParameter("email"), req.getParameter("password"),
                3l);
        UserDao dao = new DatastoreDao();
        try {
            Long id = dao.createUser(user);
            User createdUser = dao.getUser(id);
            resp.setStatus(200);
               String json = new Gson().toJson(createdUser);
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(json);
        

        } catch (Exception e) {
            resp.sendError(400);
            throw new ServletException("Error creating book", e);

        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polsl.roadtracker.trackerapi.dao;

import com.polsl.roadtracker.trackerapi.model.Result;
import com.polsl.roadtracker.trackerapi.model.User;
import java.sql.SQLException;

/**
 *
 * @author alachman
 */
public interface UserDao {

    Long createUser(User user) throws SQLException;

    User getUser(Long userId) throws SQLException;

    void updateUser(User user) throws SQLException;

    void deleteUser(Long userIId) throws SQLException;

    Result<User> listUsers(String startCursor) throws SQLException;
}

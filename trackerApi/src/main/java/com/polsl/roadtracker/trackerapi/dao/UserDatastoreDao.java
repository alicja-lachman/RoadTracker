/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polsl.roadtracker.trackerapi.dao;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.polsl.roadtracker.trackerapi.model.Result;
import com.polsl.roadtracker.trackerapi.model.User;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author alachman
 */
public class UserDatastoreDao {

    private DatastoreService datastore;
    private static final String USER_KIND = "User";

    public UserDatastoreDao() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    public User entityToUser(Entity entity) {
        return new User(entity.getKey().getId(), (String) entity.getProperty(User.NAME),
                (String) entity.getProperty(User.EMAIL),
                (String) entity.getProperty(User.PASSWORD), (Long) entity.getProperty(User.ACCELOMETER),
                (Long) entity.getProperty(User.GYROSCOPE),
                (Long) entity.getProperty(User.MAGNETIC_FIELD), (Long) entity.getProperty(User.AMBIENT_TEMPERATURE),
                (String) entity.getProperty(User.AUTH_TOKEN),
                (String) entity.getProperty(User.DEBUG_DATA));

    }

    public Long createUser(User user) {
        Entity entity = new Entity(USER_KIND);
        entity.setProperty(User.NAME, user.getName());
        entity.setProperty(User.EMAIL, user.getEmail());
        entity.setProperty(User.PASSWORD, user.getPassword());
        entity.setProperty(User.ACCELOMETER, user.getAccelometer());
        entity.setProperty(User.GYROSCOPE, user.getGyroscope());
        entity.setProperty(User.MAGNETIC_FIELD, user.getMagneticField());
        entity.setProperty(User.AMBIENT_TEMPERATURE, user.getAmbientTemperature());
        entity.setProperty(User.AUTH_TOKEN, user.getAuthToken());
        entity.setProperty(User.DEBUG_DATA, user.getDebugData());
        Key userKey = datastore.put(entity);
        return userKey.getId();
    }

    public User getUser(Long userId) {
        try {
            Entity entity = datastore.get(KeyFactory.createKey(USER_KIND, userId));
            return entityToUser(entity);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    public void updateUser(User user) {
        Key key = KeyFactory.createKey(USER_KIND, user.getId());
        Entity entity = new Entity(key);
        entity.setProperty(User.NAME, user.getName());
        entity.setProperty(User.EMAIL, user.getEmail());
        entity.setProperty(User.PASSWORD, user.getPassword());
        entity.setProperty(User.ACCELOMETER, user.getAccelometer());
        entity.setProperty(User.GYROSCOPE, user.getGyroscope());
        entity.setProperty(User.MAGNETIC_FIELD, user.getMagneticField());
        entity.setProperty(User.AMBIENT_TEMPERATURE, user.getAmbientTemperature());
        entity.setProperty(User.AUTH_TOKEN, user.getAuthToken());
        entity.setProperty(User.DEBUG_DATA, user.getDebugData());
        datastore.put(entity);
    }

    public void deleteUser(Long userId) {
        Key key = KeyFactory.createKey(USER_KIND, userId);
        datastore.delete(key);
    }

    public List<User> entitiesToUsers(Iterator<Entity> results) {
        List<User> resultUsers = new ArrayList<>();
        while (results.hasNext()) {
            resultUsers.add(entityToUser(results.next()));
        }
        return resultUsers;
    }

    public Result<User> listUsers(String startCursor) {
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
        if (startCursor != null && !startCursor.equals("")) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
        }
        Query query = new Query(USER_KIND)
                .addSort(User.EMAIL, Query.SortDirection.ASCENDING);
        PreparedQuery preparedQuery = datastore.prepare(query);
        QueryResultIterator<Entity> results = preparedQuery.asQueryResultIterator(fetchOptions);

        List<User> resultUsers = entitiesToUsers(results);
        Cursor cursor = results.getCursor();
        if (cursor != null && resultUsers.size() == 10) {
            String cursorString = cursor.toWebSafeString();
            return new Result<>(resultUsers, cursorString);
        } else {
            return new Result<>(resultUsers);
        }
    }

    public User getUserByEmail(String email) {
        Filter filter = new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email);
        Query query = new Query(USER_KIND).setFilter(filter);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        if (results != null && !results.isEmpty()) {
            return entityToUser(results.get(0));
        }
        return null;
    }

    public User getUserByAuthToken(String authToken) {
        Filter filter = new Query.FilterPredicate("authToken", Query.FilterOperator.EQUAL, authToken);
        Query query = new Query(USER_KIND).setFilter(filter);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        if (results != null && !results.isEmpty()) {
            return entityToUser(results.get(0));
        }
        return null;
    }
}

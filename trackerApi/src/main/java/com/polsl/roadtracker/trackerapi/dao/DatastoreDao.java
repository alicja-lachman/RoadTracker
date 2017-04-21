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
public class DatastoreDao implements UserDao {

    private DatastoreService datastore;
    private static final String USER_KIND = "User";

    public DatastoreDao() {
        datastore = DatastoreServiceFactory.getDatastoreService(); // Authorized Datastore service
    }

    public User entityToUser(Entity entity) {
        return new User(entity.getKey().getId(), (String) entity.getProperty(User.EMAIL),
                (String) entity.getProperty(User.PASSWORD),
                (Long) entity.getProperty(User.SENSOR_DELAY));

    }

    @Override
    public Long createUser(User user) {
        Entity entity = new Entity(USER_KIND);  // Key will be assigned once written
        entity.setProperty(User.EMAIL, user.getEmail());
        entity.setProperty(User.PASSWORD, user.getPassword());
        entity.setProperty(User.SENSOR_DELAY, user.getSensorDelay());
        Key userKey = datastore.put(entity); // Save the Entity
        return userKey.getId();                     // The ID of the Key
    }

    @Override
    public User getUser(Long userId) {
        try {
            Entity entity = datastore.get(KeyFactory.createKey(USER_KIND, userId));
            return entityToUser(entity);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public void updateUser(User user) {
        Key key = KeyFactory.createKey(USER_KIND, user.getId());  // From a book, create a Key
        Entity entity = new Entity(key);         // Convert Book to an Entity
        entity.setProperty(User.EMAIL, user.getEmail());
        entity.setProperty(User.PASSWORD, user.getPassword());
        entity.setProperty(User.SENSOR_DELAY, user.getSensorDelay());
        datastore.put(entity);                   // Update the Entity
    }

    @Override
    public void deleteUser(Long userId) {
        Key key = KeyFactory.createKey(USER_KIND, userId);        // Create the Key
        datastore.delete(key);                      // Delete the Entity
    }

    public List<User> entitiesToUsers(Iterator<Entity> results) {
        List<User> resultUsers = new ArrayList<>();
        while (results.hasNext()) {  // We still have data
            resultUsers.add(entityToUser(results.next()));      // Add the Book to the List
        }
        return resultUsers;
    }

    @Override
    public Result<User> listUsers(String startCursor) {
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10); // Only show 10 at a time
        if (startCursor != null && !startCursor.equals("")) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor)); // Where we left off
        }
        Query query = new Query(USER_KIND) // We only care about Books
                .addSort(User.EMAIL, Query.SortDirection.ASCENDING); // Use default Index "title"
        PreparedQuery preparedQuery = datastore.prepare(query);
        QueryResultIterator<Entity> results = preparedQuery.asQueryResultIterator(fetchOptions);

        List<User> resultUsers = entitiesToUsers(results);     // Retrieve and convert Entities
        Cursor cursor = results.getCursor();              // Where to start next time
        if (cursor != null && resultUsers.size() == 10) {         // Are we paging? Save Cursor
            String cursorString = cursor.toWebSafeString();               // Cursors are WebSafe
            return new Result<>(resultUsers, cursorString);
        } else {
            return new Result<>(resultUsers);
        }
    }
    // [END listbooks]
}
// [END example]

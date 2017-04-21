package com.polsl.roadtracker.trackerapi;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import servlet.UserResource;

public class ApiApplication extends Application {

    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(UserResource.class);
        return s;
    }
}

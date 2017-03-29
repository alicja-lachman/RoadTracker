package com.polsl.roadtracker.dagger.di.component;

import com.polsl.roadtracker.activity.ExampleActivity;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;

import dagger.Component;

/**
 * Created by alachman on 29.03.2017.
 */
@Component(modules = {DatabaseModule.class})
public interface DatabaseComponent {
    ExampleActivity inject(ExampleActivity exampleActivity);
}


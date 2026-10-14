package com.example.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class SpinAddon extends MeteorAddon {
    public static final Category CATEGORY = new Category("Spin");

    @Override
    public void onInitialize() {
        Modules.get().add(new SpinModule());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}

package com.simplytest.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Aufgabe_3_JSON_Tests {

    public class DeviceDTO {
        private String name;
        private String[] devices;

        // Getter und Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String[] getDevices() {
            return devices;
        }

        public void setDevices(String[] devices) {
            this.devices = devices;
        }
    }


    @Test
    public void jsonTest() {

        // Erstellen und Befüllen von JsonObject
        org.json.JSONObject jsonObj = new org.json.JSONObject();
        jsonObj.put("name", "Apple");

        // Befüllen von Array
        org.json.JSONArray array = new org.json.JSONArray();
        array.put("iPhone");
        array.put("iPad");
        array.put("iMac");
        jsonObj.put("devices", array);

        // Zugriff auf einzelne Elemente
        String name = jsonObj.getString("name");
        var devices = jsonObj.getJSONArray("devices");
        String firstDevice = devices.getString(0);

        // Ausgabe
        System.out.println("Name: " + name);
        System.out.println("Devices: " + devices);
        System.out.println("First Device: " + firstDevice);


        // JSON-String
        String json = """
        {
            "name": "Apple",
            "devices": ["iPhone", "iPad", "iMac"]
        }
        """;

        // JSON ins DTO parsen
        // JSON ins DTO parsen
        DeviceDTO dto = new DeviceDTO();
        org.json.JSONObject jsonObject = new org.json.JSONObject(json);
        dto.setName(jsonObject.getString("name"));

        org.json.JSONArray jsonArray = jsonObject.getJSONArray("devices");
        String[] devices2 = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            devices2[i] = jsonArray.getString(i);
        }
        dto.setDevices(devices2);

        // Zugriff auf die Daten im DTO und Ausgabe
        System.out.println("Name: " + dto.getName());
        System.out.println("Devices: " + String.join(", ", dto.getDevices()[0]));
        System.out.println("First Device: " + firstDevice);

        Assertions.assertEquals("Apple", name);
        Assertions.assertEquals("iPhone", firstDevice);
    }


    @Test
    public void gsonTest() {

        // Erstellen und Befüllen von JsonObject
        com.google.gson.JsonObject jsonObj = new com.google.gson.JsonObject();
        jsonObj.addProperty("name", "Apple");

        // Befüllen von JsonArray
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        array.add("iPhone");
        array.add("iPad");
        array.add("iMac");
        jsonObj.add("devices", array);

        // Zugriff auf einzelne Elemente
        String name = jsonObj.get("name").getAsString();
        var devices = jsonObj.getAsJsonArray("devices");
        String firstDevice = devices.get(0).getAsString();

        // Ausgabe
        System.out.println("Name: " + name);
        System.out.println("Devices: " + devices);
        System.out.println("First Device: " + firstDevice);


        // JSON-String
        String json = """
        {
            "name": "Apple",
            "devices": ["iPhone", "iPad", "iMac"]
        }
        """;

        // JSON ins DTO parsen
        DeviceDTO dto = new com.google.gson.Gson().fromJson(json, DeviceDTO.class);


        // Zugriff auf die Daten im DTO und Ausgabe
        System.out.println("Name: " + dto.getName());
        System.out.println("Devices: " + String.join(", ", dto.getDevices()[0]));
        System.out.println("First Device: " + firstDevice);

        Assertions.assertEquals("Apple", name);
        Assertions.assertEquals("iPhone", firstDevice);
    }
}





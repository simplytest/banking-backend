package com.simplytest.core;

public class Address
{
    private String country = "Deutschland";
    private String zipCode;
    private String street;
    private String house;
    private String city;

    private String email;

    public String getCountry()
    {
        return country;
    }

    public String getZipCode()
    {
        return zipCode;
    }

    public String getStreet()
    {
        return street;
    }

    public String getHouse()
    {
        return house;
    }

    public String getCity()
    {
        return city;
    }

    public String getEmail()
    {
        return email;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public void setZipCode(String zipCode)
    {
        this.zipCode = zipCode;
    }

    public void setStreet(String street)
    {
        this.street = street;
    }

    public void setHouse(String house)
    {
        this.house = house;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}

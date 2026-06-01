package com.teknisio.mobile.model.request;

public class RegisterTechnicianRequest {
    public String name;
    public String email;
    public String phoneNumber;
    public String password;
    public String address;
    public String description;

    public RegisterTechnicianRequest(
            String name,
            String email,
            String phoneNumber,
            String password,
            String address,
            String description
    ) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.address = address;
        this.description = description;
    }
}

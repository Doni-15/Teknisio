package com.teknisio.mobile.model.request;

public class RegisterCustomerRequest {
  public String name;
  public String email;
  public String phoneNumber;
  public String password;
  public String address;
  
  public RegisterCustomerRequest(String name, String email, String phoneNumber, String password, String address) {
    this.name = name;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.password = password;
    this.address = address;
  }
}

package com.scc.model;

import java.time.LocalDate;

public class SeniorCitizen {
    private int seniorId;
    private String firstName;
    private String lastName;
    private LocalDate birthdate;
    private int age;
    private String gender;
    private String purokAddress;
    private String contactNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String healthConditions;
    private String status;

    public SeniorCitizen() {}

    public SeniorCitizen(int seniorId, String firstName, String lastName, LocalDate birthdate,
                         int age, String gender, String purokAddress, String contactNumber,
                         String emergencyContactName, String emergencyContactPhone,
                         String healthConditions, String status) {
        this.seniorId = seniorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.age = age;
        this.gender = gender;
        this.purokAddress = purokAddress;
        this.contactNumber = contactNumber;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.healthConditions = healthConditions;
        this.status = status;
    }

    public int getSeniorId() { return seniorId; }
    public void setSeniorId(int seniorId) { this.seniorId = seniorId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPurokAddress() { return purokAddress; }
    public void setPurokAddress(String purokAddress) { this.purokAddress = purokAddress; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getHealthConditions() { return healthConditions; }
    public void setHealthConditions(String healthConditions) { this.healthConditions = healthConditions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
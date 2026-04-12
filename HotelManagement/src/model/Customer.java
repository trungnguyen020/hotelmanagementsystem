package model;

public class Customer {
    private int id;
    private String fullName;
    private String phone;
    private String idNumber;

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getIdNumber() { return idNumber; }

    public void setId(int id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
}
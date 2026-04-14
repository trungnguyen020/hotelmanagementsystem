package model;

public class Employee {
    private int id;
    private String username;
    private String fullName;
    private Role role;
    private String status;

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(Role role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
}
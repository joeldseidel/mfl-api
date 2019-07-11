package main.types;

public class User{
    private String username, password, firstname, lastname, org;
    private int access;

    public User(String username, String password, String firstname, String lastname, String org, int access){
        setUsername(username);
        setPassword(password);
        setFirstName(firstname);
        setLastName(lastname);
        setOrganization(org);
        setAccess(access);
    }

    public String getUsername(){ return this.username; }
    public String getPassword() { return this.password; }
    public String getFirstname() { return this.firstname; }
    public String getLastname() { return this.lastname; }
    public String getOrganization() { return this.org; }
    public int getAccess() { return this.access; }
    private void setUsername(String username){ this.username = username; }
    private void setPassword(String password) { this.password = password; }
    private void setFirstName(String firstname) { this.firstname = firstname; }
    private void setLastName(String lastname) { this.lastname = lastname; }
    private void setOrganization(String org) { this.org = org; }
    private void setAccess(int access) { this.access = access; }
}

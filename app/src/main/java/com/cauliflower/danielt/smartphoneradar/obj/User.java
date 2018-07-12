package com.cauliflower.danielt.smartphoneradar.obj;

public class User {
    private String email, password, usedFor, in_use;

    public User() {
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsedFor(String usedFor) {
        this.usedFor = usedFor;
    }

    public void setIn_use(String in_use) {
        this.in_use = in_use;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsedFor() {
        return usedFor;
    }

    public String getIn_use() {
        return in_use;
    }
}

package com.cauliflower.danielt.smartphoneradar.obj;

public class User {
    private String account, password, usedFor, in_use;

    public User() {
    }

    public void setAccount(String account) {
        this.account = account;
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

    public String getAccount() {
        return account;
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

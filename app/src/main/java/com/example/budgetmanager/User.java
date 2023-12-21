package com.example.budgetmanager;

public class User {
    public String  username, age, email, date;

    public User(){

    }
    public User(String regusername, String regage, String email, String date){
        this.username = regusername;
        this.age = regage;
        this.email = email;
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
package model.Entity;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="User",schema = "sys",
       indexes = { @Index(name = "IDX_User1", columnList = "userID") },
       uniqueConstraints = {@UniqueConstraint(columnNames = {"userName","userPhone"})})
@NamedQueries({
        @NamedQuery(name = "User.findByName",
                query = "SELECT u FROM User u WHERE u.userName = :name"),
        @NamedQuery(name = "User.findByNameAndPassword",
                query = "SELECT u FROM User u WHERE u.userName = :name and u.userPass = :password"),
        @NamedQuery(name = "User.findByID",
                query =  "SELECT u FROM User u WHERE u.id = :ID"),
        @NamedQuery(name = "User.findAll",
                query = "SELECT u FROM User u")
})
public class User {
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Integer userID;
    @Column(length = 50, nullable = false)
    private String userName;
    @Column(length = 50, nullable = false)
    private String userPass;
    private boolean isAdmin;
    private boolean isDeleted;
    @Column(length = 30)
    private String userPhone;
    private String userMail;

    public User(int userID, String userName, String userPass, boolean isAdmin, boolean isDeleted, String userPhone, String userMail) {
        this.userID = userID;
        this.userName = userName;
        this.userPass = userPass;
        this.isAdmin = isAdmin;
        this.isDeleted = isDeleted;
        this.userPhone = userPhone;
        this.userMail = userMail;
    }

    public User(String userName, String userPass, boolean isAdmin, boolean isDeleted, String userPhone, String userMail) {
        this.userName = userName;
        this.userPass = userPass;
        this.isAdmin = isAdmin;
        this.isDeleted = isDeleted;
        this.userPhone = userPhone;
        this.userMail = userMail;
    }

    public User() {

    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public User get(){
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isAdmin == user.isAdmin &&
                isDeleted == user.isDeleted &&
                Objects.equals(userID, user.userID) &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(userPass, user.userPass) &&
                Objects.equals(userPhone, user.userPhone) &&
                Objects.equals(userMail, user.userMail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, userName, userPass, isAdmin, isDeleted, userPhone, userMail);
    }
}



package hausfix.entities;

public class User {
    private Long id;
    private String username;
    private String password;

    // Standard-Konstruktor
    public User() {
    }

    // Parameter-Konstruktor
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // toString-Methode (optional, nützlich für Debugging)
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
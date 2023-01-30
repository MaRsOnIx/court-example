package eu.great.code.courtexample;


import java.util.Objects;

public class Fullname {
    private String firstname;
    private String lastname;

    public Fullname(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public static Fullname of(String firstname, String lastname) {
        return new Fullname(firstname, lastname);
    }
    protected Fullname() {
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fullname fullname = (Fullname) o;
        return firstname.equals(fullname.firstname) && lastname.equals(fullname.lastname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstname, lastname);
    }

    @Override
    public String toString() {
        return "Fullname{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }
}

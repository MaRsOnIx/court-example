package eu.great.code.courtexample;


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



}

package de.mbe.tutorials.aws.lambda.pojos;

public class Request {

    private String firstName;
    private String lastName;
    private int age;
    private boolean throwError;

    public Request() { }

    public Request(final String firstName, final String lastName, final int age) {
        this(firstName, lastName, age, false);
    }

    public Request(final String firstName, final String lastName, final int age, final boolean throwError) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.throwError = throwError;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public boolean isThrowError() {
        return throwError;
    }

    public void setThrowError(final boolean throwError) {
        this.throwError = throwError;
    }
}

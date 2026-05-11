package fr.istic.taa.jaxrs.domain;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)

public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long UserId;
	private String name;
	private String firstname;
	private String password;
	private String email;

	public User() {
	}

	public User(Long userId, String name, String firstname, String password, String email) {
		super();
		UserId = userId;
		this.name = name;
		this.firstname = firstname;
		this.password = password;
		this.email = email;
	}
	public Long getUserId() {
		return UserId;
	}
	public void setUserId(Long userId) {
		UserId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	


}

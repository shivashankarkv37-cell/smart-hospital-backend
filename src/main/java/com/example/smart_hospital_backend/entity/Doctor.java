package com.example.smart_hospital_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String specialization;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "department_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_doctors_department"))
    private Department department;

    // Optional account link used to scope doctor queue access to the assigned doctor.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, foreignKey = @ForeignKey(name = "fk_doctors_user"))
    private User user;

    protected Doctor() {}

    public Doctor(String name, String specialization, Department department, User user) {
        this.name = name;
        this.specialization = specialization;
        this.department = department;
        this.user = user;
    }

    public void updateDetails(String name, String specialization, Department department, User linkedUser) {
        this.name = name;
        this.specialization = specialization;
        this.department = department;
        if (linkedUser != null) {
            this.user = linkedUser;
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public Department getDepartment() {
        return department;
    }

    public User getUser() {
        return user;
    }
}

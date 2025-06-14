package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Set;

@Entity(name = "organization")
@Table(name = "organization")
@SQLDelete(sql = "UPDATE organization SET deleted = 'Y' WHERE id=?")
@Where(clause = "deleted='N'")
@Getter
@Setter
public class Organization extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_gen")
    @SequenceGenerator(name = "organization_gen", sequenceName = "organization_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "external_id")
    private String externalId;
    private String name;
    @Convert(converter= BooleanConverter.class)
    @Column(name = "status", length = 1)
    private Boolean status;

    @Column(name = "api_key")
    private String apiKey;

    @JsonManagedReference
    @OneToMany(mappedBy = "organization")
    @JsonIgnore
    private Set<OperationSchema> operationSchemas;

    @JsonManagedReference
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Operation> operations;

    @JsonManagedReference
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<UserOrganization> userOrganizations;

    @JsonManagedReference
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Endpoint> endpoints;

    @OneToMany(mappedBy = "organization", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<OrganizationSubscription> subscriptions;


}

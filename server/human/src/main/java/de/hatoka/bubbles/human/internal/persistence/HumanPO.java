package de.hatoka.bubbles.human.internal.persistence;

import java.io.Serializable;
import java.util.Objects;

import de.hatoka.bubbles.human.capi.business.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "humans", uniqueConstraints = { @UniqueConstraint(columnNames = { "human_ext" }) })
public class HumanPO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Internal identifier for persistence only
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "human_id")
    private Long id;

    /**
     * Initial owner of the human
     */
    @NotNull
    @Column(name = "user_ref", nullable = false)
    private String userref;

    /**
     * External key of human
     */
    @NotNull
    @Column(name = "human_ext", nullable = false)
    private String humanext;

    /**
     * Readable name of human
     */
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "date_of_birth", nullable = true)
    private String dateOfBirth;
    @Column(name = "date_of_death", nullable = true)
    private String dateOfDeath;
    @Column(name = "gender", nullable = true)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    public HumanPO()
    {
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(humanext);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HumanPO other = (HumanPO)obj;
        return Objects.equals(humanext, other.humanext);
    }

    public String getUserRef()
    {
        return userref;
    }

    public void setUserRef(String userRef)
    {
        this.userref = userRef;
    }

    public String getExternalID()
    {
        return humanext;
    }

    public void setExternalID(String humanExternalID)
    {
        this.humanext = humanExternalID;
    }

    public String getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfDeath()
    {
        return dateOfDeath;
    }

    public void setDateOfDeath(String dateOfDeath)
    {
        this.dateOfDeath = dateOfDeath;
    }

    public Gender getGender()
    {
        return gender;
    }

    public void setGender(Gender gender)
    {
        this.gender = gender;
    }
}

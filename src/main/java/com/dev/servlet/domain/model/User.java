package com.dev.servlet.domain.model;
import com.dev.servlet.domain.model.enums.RoleType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ColumnTransformer;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@javax.persistence.Entity
@Table(name = "tb_user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@ToString(exclude = "credentials")
public class User implements Entity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Embedded
    private Credentials credentials;
    @Column(name = "status", nullable = false)
    @ColumnTransformer(write = "UPPER(?)")
    private String status;
    @Column(name = "image_url")
    private String imgUrl;
    @Column(name = "config")
    private String config;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_perfis", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "perfil_id")
    private List<Long> perfis;
    @Transient
    @JsonIgnore
    private String token;
    public User(Long id) {
        this.id = id;
    }

    public User(String login) {
        setLogin(login);
    }

    public User(String login, String password) {
        this(login);
        this.setPassword(password);
    }

    public void addPerfil(Long perfil) {
        synchronized (this) {
            if (this.perfis == null) {
                this.perfis = new ArrayList<>();
            }
        }
        this.perfis.add(perfil);
    }

    public boolean hasRole(RoleType role) {
        if (this.perfis == null) {
            return false;
        }
        for (Long perfil : this.perfis) {
            if (RoleType.toEnum(perfil).equals(role)) {
                return true;
            }
        }
        return false;
    }

    public void setLogin(String login) {
        if (credentials == null) credentials = new Credentials();
        this.credentials.setLogin(login);
    }

    public void setPassword(String password) {
        if (credentials == null) credentials = new Credentials();
        this.credentials.setPassword(password);
    }
    @JsonIgnore
    public String getLogin() {
        return credentials != null ? credentials.getLogin() : null;
    }
    @JsonIgnore
    public String getPassword() {
        return credentials != null ? credentials.getPassword() : null;
    }
}

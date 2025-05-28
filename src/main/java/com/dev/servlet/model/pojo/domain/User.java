package com.dev.servlet.model.pojo.domain;

import com.dev.servlet.model.Identifier;
import com.dev.servlet.model.pojo.enums.RoleType;
import lombok.AllArgsConstructor;
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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@javax.persistence.Entity
@Table(name = "tb_user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@ToString(exclude = "password")
public class User implements Identifier<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ColumnTransformer(write = "LOWER(?)")
    @Column(name = "login", unique = true)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "status", nullable = false)
    @ColumnTransformer(write = "UPPER(?)")
    private String status;

    @Column(name = "image_url")
    private String imgUrl;

    @Column(name = "config")
    private String config; //json

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_perfis", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "perfil_id")
    private List<Long> perfis;

    public User(Long id) {
        this.id = id;
    }

    public User(String login) {
        this.login = login;
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
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
}

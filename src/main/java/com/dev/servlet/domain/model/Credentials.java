package com.dev.servlet.domain.model;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreType
public class Credentials {
    @ColumnTransformer(write = "LOWER(?)")
    @Column(name = "login", unique = true)
    private String login;
    @Column(name = "password", nullable = false)
    private String password;

    public Credentials(String login) {
        this.login = login;
    }
}

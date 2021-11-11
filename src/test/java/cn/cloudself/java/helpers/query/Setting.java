package cn.cloudself.java.helpers.query;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * 
 */
@Entity
@Table(name = "setting")
public class Setting implements Serializable {
    private static final long serialVersionUID = 1L;

    /**  */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**  */
    @Column(name = "user_id")
    private Long userId;

    /**  */
    @Column(name = "kee")
    private String kee;

    /**  */
    @Column(name = "value")
    private String value;

    /**  */
    @Column(name = "deleted")
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public Setting setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public Setting setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getKee() {
        return kee;
    }

    public Setting setKee(String kee) {
        this.kee = kee;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Setting setValue(String value) {
        this.value = value;
        return this;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Setting setDeleted(Boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Setting setting = (Setting) o;
        return Objects.equals(id, setting.id) && Objects.equals(userId, setting.userId) && Objects.equals(kee, setting.kee) && Objects.equals(value, setting.value) && Objects.equals(deleted, setting.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, kee, value, deleted);
    }

    @Override
    public String toString() {
        return "Setting{" +
                ", id=" + id +
                ", userId=" + userId +
                ", kee='" + kee + '\'' +
                ", value='" + value + '\'' +
                ", deleted=" + deleted +
                '}';
    }

    public <T> T copyTo(Class<T> clazz) { return null; }
}

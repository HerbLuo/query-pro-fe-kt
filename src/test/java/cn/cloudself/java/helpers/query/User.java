package cn.cloudself.java.helpers.query;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * 
 */
@Entity
@Table(name = "user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**  */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**  */
    @Column(name = "name")
    private String name;

    /**  */
    @Column(name = "age")
    private Integer age;

    /**  */
    @Column(name = "deleted")
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public User setAge(Integer age) {
        this.age = age;
        return this;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public User setDeleted(Boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(age, user.age) && Objects.equals(deleted, user.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, deleted);
    }

    @Override
    public String toString() {
        return "User{" +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", deleted=" + deleted +
                '}';
    }

    public <T> T copyTo(Class<T> clazz) { return null; }
}

package entity;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.io.Serializable;

@Entity
@NamedQueries({@NamedQuery(name = "MyTable.findAll", query = "select e from MyTable e order by e.mycol")})
public class MyTable implements Serializable {
    @Id
    private String mycol;

    public String getMycol() {
        return mycol;
    }

    public void setMycol(final String mycol) {
        this.mycol = mycol;
    }
}

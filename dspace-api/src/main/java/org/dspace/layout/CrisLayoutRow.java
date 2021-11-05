package org.dspace.layout;

import org.dspace.core.ReloadableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "cris_layout_row")
public class CrisLayoutRow implements ReloadableEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cris_layout_row_id_seq")
    @SequenceGenerator(name = "cris_layout_row_id_seq", sequenceName = "cris_layout_row_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false, insertable = true, updatable = false)
    private Integer id;
    @Column(name = "style")
    private String style;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tab")
    private CrisLayoutTab crisLayoutTab;
    @Column(name = "position")
    private int position;
    @OneToMany(fetch = FetchType.LAZY)
    private List<CrisLayoutCell> cells;
    @Override
    public Integer getID() {
        return id;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<CrisLayoutCell> getCells() {
        return cells;
    }

    public void setCells(List<CrisLayoutCell> cells) {
        this.cells = cells;
    }
}

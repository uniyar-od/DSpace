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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cris_layout_cell")
public class CrisLayoutCell implements ReloadableEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cris_layout_cell_id_seq")
    @SequenceGenerator(name = "cris_layout_cell_id_seq", sequenceName = "cris_layout_cell_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false, insertable = true, updatable = false)
    private Integer id;
    @Column(name = "style")
    private String style;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "row")
    private CrisLayoutRow crisLayoutRow;
    @Column(name = "position")
    private int position;
    @OneToMany(fetch = FetchType.LAZY)
    private java.util.List<CrisLayoutBox> boxes = new ArrayList<>();
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<CrisLayoutBox> getBoxes() {
        return boxes;
    }

    public void setBoxex(List<CrisLayoutBox> boxes) {
        this.boxes = boxes;
    }
}

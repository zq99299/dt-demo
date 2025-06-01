package cn.mrcode.dtdemo.b.repo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhuqiang
 * @since 2025-06-01
 */
@TableName("t_storage")
public class TStorage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String productId;

    private Integer total;

    private Integer used;

    /**
     * 可用库存
     */
    private Integer residue;

    /**
     * 冻结库存
     */
    private Integer frozen;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public Integer getResidue() {
        return residue;
    }

    public void setResidue(Integer residue) {
        this.residue = residue;
    }

    public Integer getFrozen() {
        return frozen;
    }

    public void setFrozen(Integer frozen) {
        this.frozen = frozen;
    }

    @Override
    public String toString() {
        return "TStorage{" +
        "id = " + id +
        ", productId = " + productId +
        ", total = " + total +
        ", used = " + used +
        ", residue = " + residue +
        ", frozen = " + frozen +
        "}";
    }
}

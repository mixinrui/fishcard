package view;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by liuzhihao on 16/3/14.
 */
@Data
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EqualsAndHashCode(exclude = "updateTime")
public class BaseObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "create_time", updatable = false, columnDefinition = "DEFAULT CURRENT_TIMESTAMP()")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Column(name = "update_time", insertable = false, columnDefinition = "DEFAULT CURRENT_TIMESTAMP()")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public static boolean isNull(BaseObject object) {
        return object == null;
    }
}

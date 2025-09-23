package edu.cust.secad.model.hanlder;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class GlobalMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        //在插入时：创建时间和修改时间
        this.setFieldValByName("createTime",LocalDateTime.now(),metaObject);
        this.setFieldValByName("modifyTime",LocalDateTime.now(),metaObject);
        this.setFieldValByName("isDeleted",1,metaObject);
//        this.setFieldValByName("cpcId", IdWorker.getId(), metaObject);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //在修改时：修改时间
        this.setFieldValByName("modifyTime",LocalDateTime.now(),metaObject);
    }

}
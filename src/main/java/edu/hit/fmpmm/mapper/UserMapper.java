package edu.hit.fmpmm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.hit.fmpmm.domain.web.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

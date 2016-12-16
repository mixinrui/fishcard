package workorder.service;

import com.boxfishedu.workorder.servicex.assignTeacher.AssignTeacherServiceX;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by olly on 2016/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class AssginTeacherTest {
    @Autowired
    AssignTeacherServiceX assignTeacherServiceX;

    @Test
    public void doAssign(){
        Long studentId = 1300041L;
        Long teacherId = 100000000612L;
        assignTeacherServiceX.doAssignTeacher(teacherId,studentId);
    }
}

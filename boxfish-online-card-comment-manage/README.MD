## 外教点评后台
### 1 外教点评分页查询
> 1 接口：/comment/manage/pageitem  
> 2 参数表：  
>
|参数名称|参数作用|参数值|是否必需
||||
|page|页码|默认1|否
|size|每页显示条数|默认10|否
|code|点评id|id|否
|notAnswerTime|未点评时长|int(0:<=24,1:24<hours<=36,2:>36,3:24<hours<48)|否
|teacherId|老师Id|老师Id|否
|teacherName|老师Name|老师姓名|否
|status|点评状态|0:未点评,1:已回答,2:超时未点评|否
|studentId|学生Id|学生Id|否
|studentName|学生姓名|学生姓名|否
|studentAskTimeRangeFrom|点评查询范围开始时间|开始时间|否
|studentAskTimeRangeTo|点评查询范围结束时间|结束时间|否
|orderChannel|订单类型|类型|否
|orderCode|订单code|code|否
|courseType|课程Type|Type|否
|courseDifficulty|课程难度|难度|否

> 3 返回值：返回分页数据  

### 2 根据id查询外教点评
> 1 接口：/comment/manage/{id}  
> 2 返回值：对应的点评DTO

### 3 导出查询的外教点评列表
> 1 接口：/comment/manage/export  
> 2 参数表：与接口#1参数一致  
> 3 工作流程：
>
* 根据条件查询外教点评列表，每页查询条数由前端传递过来，一般是一个很大的值（但excel最多60K行），查询出列表然后使用DTOBinder进行转换，将其转换为CommentCardExcelDto，并且进行下载。

### 4 外教点评换老师
> 1 接口：/comment/manage/{id}/change/teacher/{teacherId}  
> 2 工作流程：  
>
* 先根据点评id获取到点评，然后对点评进行验证（点评是否存在，该点评是否是指定老师的点评，验证数据权限）。
* 复制出一个新的点评，然后将状态标记为未点评，将老的点评标记为已过期，将其进行保存。
* 最后调用教师管理changeTeacher对应老师扣点评次数。

### 5 批量外教点评换老师
> 1 接口：/comment/manage/batch/change/teacher/{teacherId}  
> 2 参数表：long[] ids  
> 3 工作流程：与单个外教点评换老师类似。

### 6 获取未点评总数数组
> 1 接口：/comment/manage/notanswer/count  
> 2 返回值：{"count": [10,20,30]}，count[0]: 24小时以内，count[1]: 24到36小时以内，count[2]: 36小时以上。

### 7 获取点评状态列表
> 1 接口：/comment/manage/statuses  
> 2 返回值：{resultCode:200, resultMsg:"success", data: [{"未点评":0},{"已回答":1},{"超时未完成":2}]}

### 8 获取到该点评的日志列表
> 1 接口：/comment/manage/logs/{id}  
> 2 工作流程：
>
* 根据外教点评Id获取到对应的点评， 然后根据这个点评id作为preId，查询出一连串的点评列表（用preId作为追溯点评的线索）。
* 将这一连串的点评按照时间顺序串联起来，形成了点评状态的轮转切换日志。

### 9 冻结老师接口
> 1 接口：/comment/manage/freeze_teacher_id/{teacherId}  
> 2 工作流程：
> 
* 先验证老师的正确性，老师是否存在，内部账号不能执行冻结或者解冻。
* 获取到该老师所有的还未点评的点评列表， 然后将其分配给内部账号， 同时将老的点评标记为过期。
* 调用教师管理，冻结老师。

### 10 解冻老师接口
> 1 接口：/comment/manage/unfreeze_teacher_id/{teacherId}  
> 2 工作流程：
> 
* 先验证老师的正确性，老师是否存在，内部账号不能执行冻结或者解冻。
* 调用教师管理，解冻老师。

### 11 获取老师操作日志列表
> 1 接口：/comment/manage/get/teacher_operations/{teacherId}  
> 2 工作流程：
>
* 直接调用教师管理，获取到外教点评老师冻结解冻操作的日志列表。

### 12 获取到已收到点评老师列表
> 1 接口：/comment/manage/teacher/page/commentitem  
> 2 参数表：
>
|参数名称|参数作用|参数值|是否必需
|||
|page|页码|默认1|否
|size|每页显示条数|默认10|否
|teacherId|教师ID|教师id|否
|teacherName|教师姓名|教师名字|否
|teacherStatus|教师状态|冻结或者正常|否
|teacherType|教师类型|内部老师或者外部老师|否

### 13 获取到未收到点评老师列表
> 1 接口：/comment/manage/teacher/page/uncomment
> 2 参数表：参数表与#12接口参数表一致。

### 14 获取到外教点评老师次数变更日志列表
> 1 接口：/comment/manage/teacher/{teacherId}/teacher_times/logs  
> 2 参数表：page=1&size=20&teacherId=xxxx  
> 3 工作流程：直接调用教师管理变更日志列表。

### 15 获取到单个外教点评老师信息
> 1 接口：/comment/manage/teacher/{teacherId}  
> 2 工作流程：直接调用教师管理获取到单个教师信息。

### 16 获取到所有外教点评老师列表
> 1 接口：/comment/manage/teacher/page  
> 2 参数表：与#12接口参数表一致  

### 17 获取到外教点评老师冻结日志
> 1 接口：/comment/manage/teacher/freezelog/page  
> 2 参数表：page=1&size=10&teacherId=xxxx  
> 3 工作流程： 直接调用教师管理获取外教点评老师冻结日志。









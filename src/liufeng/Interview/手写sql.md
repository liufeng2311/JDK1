##记录面试中遇到的手写SQL问题

###找出所有功课都大于80分的学生
    
    id(主键),class_name(科目名),user_id(用户ID),score(分数)

###找出所有功课都大于某一门学科平均分的学生

###统计访问次数的前五的科目
    
    id(主键),class_name(科目名),user_id(用户ID),create_time(浏览时间)
    
    select count(id) num, class_name from t group by class_name order by num limit 5

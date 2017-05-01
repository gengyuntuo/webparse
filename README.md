# Web Parse

> 沈阳理工大学教学网数据解析，是[CollegeStudentsEvaluation](https://github.com/gengyuntuo/CollegeStudentsEvaluation)项目的子项目之一

## 简介
解析教学网，获取学生的课表、学生成绩

## 未完成功能
1. 部门用户的数据解析
2. 教师用户的数据解析

## 说明
1. 项目中使用WPClient来操作教学网数据，该类只使用了一个HttpClient，所以不需要设置Cookie
2. Referer头很重要，如果缺少Referer头会导致访问失败
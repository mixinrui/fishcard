/*
* Copyright (c) 2015 boxfish.cn. All Rights Reserved.
*/
package com.boxfishedu.workorder.web.view.course;

import com.boxfishedu.workorder.web.view.base.BaseView;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
@Data
public class CourseView extends BaseView {
    private String name;
    private String courseId;
    //课程类型
    private List<String> courseType;
    //此处为course的id
    private String bookSectionId;
    //难易程度
    private List<String> difficulty;
    //课程名称
    private String courseName;
    private String thumbnail;

    public static List<CourseView> simulateData(int size){
        List<CourseView> courseViews=new ArrayList<>();
        for(int i=0;i<size;i++){
            CourseView courseView=new CourseView();
           switch (i%10){
               case 1: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_kuK3nuqflkKzor7sxLzAwMS7oi7Hpm4TmmK_mgI7moLfngrzmiJDnmoQyLnhsc3g");
                   courseView.setCourseName("英雄是怎样炼成的2");
                   List list = new ArrayList<>();
                   list.add("全面提高");
                   courseView.setCourseType(list);
                   break;
               }
               case 2: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_lpJbnoJTlhavkuItNT0RVTEUxMOaLk-WxlS8wMDUu6ZW_5a-56K-dLeiBjOS4mueUn-a2r--8muWcqOaXtuWwmuWls-mtlOWktOmdouWJjemipOaKluWQpy54bHN4");
                   courseView.setCourseName("长对话-职业生涯：在时尚女魔头面前颤抖吧");
                   List list = new ArrayList<>();
                   list.add("全面提高");
                   courseView.setCourseType(list);
                   break;
               }
               case 3: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_niZvmtKXnsqTmlZnniYjlhavkuIogVW5pdDfmi5PlsZUvMDA1LumVv-WvueivnS3orrDlv4bvvJrmgKbnhLblv4PliqjnmoTlm57lv4YueGxzeA");
                   courseView.setCourseName("长对话-记忆：怦然心动的回忆");
                   List list = new ArrayList<>();
                   list.add("对话");
                   courseView.setCourseType(list);
                   break;
               }
               case 4: {
                   courseView.setCourseId("L3NoYXJlL3N2bi9NT1ZJRSBUSU1FIDMvMDAzLumprOi-vuWKoOaWr-WKoOWKqOeJqeWbreeahOS4gOWkqS54bHN4");
                   courseView.setCourseName("马达加斯加动物园的一天");
                   List list = new ArrayList<>();
                   list.add("全面提高");
                   courseView.setCourseType(list);
                   break;
               }
               case 5: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_kuK3nuqflkKzor7szLzA1MC7ni6znq4vmiJjkuonml7bmnJ_nmoTmlrDpl7vpgq7pgJIueGxzeA");
                   courseView.setCourseName("独立战争时期的新闻邮递");
                   List list = new ArrayList<>();
                   list.add("全面提高");
                   courseView.setCourseType(list);
                   break;
               }
               case 6: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_kuK3nuqflkKzor7sxLzA2MC7lpaXlt7TpqazmgLvnu5_nmoTnuqbkvJoueGxzeA");
                   courseView.setCourseName("奥巴马总统的约会");
                   List list = new ArrayList<>();
                   list.add("全面提高");
                   courseView.setCourseType(list);
                   break;
               }
               case 7: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_kuK3nuqflkKzor7sxLzAxMC7mi6XmnInljYPkuIfnsonkuJ3nmoROeWFuIENhdC54bHN4");
                   courseView.setCourseName("拥有千万粉丝的Nyan Cat");
                   List list = new ArrayList<>();
                   list.add("全面提高");
                   courseView.setCourseType(list);
                   break;
               }
               case 8: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_kuK3nuqflkKzor7syLzAzMC7lj5HlsZXnp5HmioDlj6_ku6Xkv53miqTnjq_looPlkJfvvJ8ueGxzeA");
                   courseView.setCourseName("拥有千万粉丝的Nyan Cat");
                   List list = new ArrayList<>();
                   list.add("全面提高");
                   courseView.setCourseType(list);
                   break;
               }
               case 9: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_lpJbnoJTlhavkuIogTW9kdWxlN-aLk-WxlS8wMDMu6ZW_5a-56K-dLeaVheS6i-S8mu-8muerpeivneaVheS6i-eahOWPpuS4gOS4que7k-WxgC54bHN4");
                   courseView.setCourseName("长对话-故事会：童话故事的另一个结局");
                   List list = new ArrayList<>();
                   list.add("对话");
                   courseView.setCourseType(list);
                   break;
               }
               case 0: {
                   courseView.setCourseId("L3NoYXJlL3N2bi_mva7mtYHmlrDnlaoyMDE1IDPmnIjliIovMDAxLuWlpeW3tOmprOW8gOWtpua8lOiusl_lm73lrrblhbTkuqHvvIzljLnlpKvmnInotKMueGxzeA");
                   courseView.setCourseName("奥巴马开学演讲_国家兴亡，匹夫有责");
                   List list = new ArrayList<>();
                   list.add("全面提高");
                   courseView.setCourseType(list);
                   break;
               }
           }
            courseViews.add(courseView);
        }
        return courseViews;
    }

    public static void main(String[] args) {
        for(int i=1;i<19;i++){
            System.out.println(i+":::::"+i%8);
        }
    }
}

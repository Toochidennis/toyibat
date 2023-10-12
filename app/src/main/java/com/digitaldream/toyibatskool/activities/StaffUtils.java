package com.digitaldream.toyibatskool.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.digitaldream.toyibatskool.R;
import com.digitaldream.toyibatskool.fragments.AdminClassAttendanceFragment;
import com.digitaldream.toyibatskool.fragments.CBTCoursesFragment;
import com.digitaldream.toyibatskool.fragments.CBTExamTypeFragment;
import com.digitaldream.toyibatskool.fragments.CBTYearFragment;
import com.digitaldream.toyibatskool.fragments.StaffFormClassFragment;
import com.digitaldream.toyibatskool.fragments.LibraryGamesFragment;
import com.digitaldream.toyibatskool.fragments.LibraryVideosFragment;
import com.digitaldream.toyibatskool.fragments.ResultStaff;
import com.digitaldream.toyibatskool.fragments.StaffFormClassStudentsFragment;
import com.digitaldream.toyibatskool.fragments.StaffResultCommentFragment;
import com.digitaldream.toyibatskool.fragments.StaffSkillsBehaviourFragment;

public class StaffUtils extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_utils);

        Intent i = getIntent();
        String classId = i.getStringExtra("classId");
        String levelId = i.getStringExtra("levelId");
        String className = i.getStringExtra("class_name");
        String courseName = i.getStringExtra("course_name");

        switch (i.getStringExtra("from")) {

            case "result":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container, new ResultStaff()).commit();
                break;

            case "student":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container, new StaffFormClassFragment()).commit();
                break;

            case "staff":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        AdminClassAttendanceFragment.newInstance(classId,
                                levelId, className, "staff")).commit();
                break;

            case "cbt":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        new CBTExamTypeFragment()).commit();
                break;

            case "exam_type":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        new CBTCoursesFragment()).commit();
                break;

            case "cbt_course":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        CBTYearFragment.newInstance(courseName, "")).commit();
                break;

            case "videos":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        new LibraryVideosFragment()).commit();
                break;

            case "games":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        new LibraryGamesFragment()).commit();
                break;

            case "form_class":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        StaffFormClassStudentsFragment.newInstance(classId)).commit();
                break;

            case "staff_comment":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        StaffResultCommentFragment.newInstance(classId)).commit();
                break;

            case "skills_behaviour":
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container,
                        StaffSkillsBehaviourFragment.newInstance(classId)).commit();
                break;
        }


    }
}

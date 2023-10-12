package com.digitaldream.toyibatskool.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digitaldream.toyibatskool.activities.ElearningCourses;
import com.digitaldream.toyibatskool.adapters.ElearningLevelAdapter;
import com.digitaldream.toyibatskool.config.DatabaseHelper;
import com.digitaldream.toyibatskool.models.CourseTable;
import com.digitaldream.toyibatskool.R;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;

import java.sql.SQLException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StaffELearningFragment extends Fragment implements ElearningLevelAdapter.OnLevelClickListener {
    RecyclerView recyclerView;
    private List<CourseTable> coursesList;
    DatabaseHelper databaseHelper;
    Dao<CourseTable,Long> courseTableDao;
    private Toolbar toolbar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_staff_elearning, container, false);
        toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("E-learning");
        toolbar.setNavigationIcon(R.drawable.arrow_left);
        toolbar.setNavigationOnClickListener(v1 -> getActivity().onBackPressed());
        setHasOptionsMenu(true);

        recyclerView = v.findViewById(R.id.e_learning_level_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        databaseHelper = new DatabaseHelper(getContext());
        try {
            courseTableDao = DaoManager.createDao(databaseHelper.getConnectionSource(),CourseTable.class);
            coursesList = courseTableDao.queryBuilder().groupBy("levelId").query();
            //coursesList = courseTableDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        ElearningLevelAdapter adapter = new ElearningLevelAdapter(getContext(),coursesList,this);
        recyclerView.setAdapter(adapter);
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        return v;
    }

    @Override
    public void onLevelClick(int position) {
        Intent intent = new Intent(getContext(), ElearningCourses.class);
        intent.putExtra("levelId",coursesList.get(position).getLevelId());
        intent.putExtra("levelName",coursesList.get(position).getLevelName());
        Log.i("levelName",coursesList.get(position).getLevelName());
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

    }
}

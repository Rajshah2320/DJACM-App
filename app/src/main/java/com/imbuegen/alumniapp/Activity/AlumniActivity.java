package com.imbuegen.alumniapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imbuegen.alumniapp.Adapters.AlumniListAdapter;
import com.imbuegen.alumniapp.Models.AlumniModel;
import com.imbuegen.alumniapp.Models.QuestionsModel;

import java.util.ArrayList;
import java.util.List;

import com.imbuegen.alumniapp.R;
import com.imbuegen.alumniapp.Service.SFHandler;

public class AlumniActivity extends BaseActivity {

    //Displaying List of Alumnis

    ListView listViewAlumni;
    List<AlumniModel> AlumniModelList;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference deptRef ;//=database.getReference("Departments").child(fbDeptKey);


    //DatabaseReference companyRef = deptRef.child("Companies");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumni_list);


        exitForAlumnis();

        listViewAlumni = findViewById(R.id.listViewAlumni);

        Bundle gt=getIntent().getExtras();
        final String str1=gt.getString("DeptName");
        final String str2=gt.getString("CompName");


        AlumniModelList = new ArrayList<>();


        deptRef = database.getReference("Departments").child(str1);
        deptRef.keepSynced(true);
        DatabaseReference companyRef = deptRef.child("Companies");
        DatabaseReference alumniref=companyRef.child(str2);
        DatabaseReference alumniref2=alumniref.child("Alumnis");

        setTitle(str2);



        alumniref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                AlumniModelList.clear();

                for (DataSnapshot alumniSnapshot : dataSnapshot.getChildren()) {

                    final AlumniModel alumni = alumniSnapshot.getValue(AlumniModel.class);
                    alumni.setCompany(str2);
                    alumni.setAlumniName(alumniSnapshot.child("Name").getValue().toString());
                    ArrayList<QuestionsModel> list = new ArrayList<>();
                    for(DataSnapshot qSnapshot: alumniSnapshot.child("questions").getChildren()) {
                        list.add(qSnapshot.getValue(QuestionsModel.class));
                    }

                    alumni.setDatabaseReferencePath(String.format("Departments/%s/Companies/%s/Alumnis/%s",str1,str2,alumniSnapshot.getKey()));
                    alumni.setQuestionsList(list);
                    AlumniModelList.add(alumni);
                }


                AlumniListAdapter adapter = new AlumniListAdapter(AlumniActivity.this,AlumniModelList);
                listViewAlumni.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void exitForAlumnis() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null ||  SFHandler.getString(getSharedPreferences("Auth",MODE_PRIVATE),SFHandler.USER_KEY).equals("Alumni")) {

            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,LoginActivity.class));
            finish();

        }
    }
}

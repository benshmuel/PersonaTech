package test;

import com.google.firebase.database.*;

/**
 * Created by benshmuel on 14/12/2017.
 */
public class te {
   private FirebaseDatabase myRef;

    public te() {

        myRef= FirebaseDatabase.getInstance();


        Query q =myRef.getReference()
                .child("Employess");

        q.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("onDataChange");
                for(DataSnapshot ds : snapshot.getChildren()){
                    System.out.println(ds);
                    System.out.println("whaet the ???");
                }
            }

            public void onCancelled(DatabaseError error) {

                System.out.println("onCancelled");
            }
        });

    }
}

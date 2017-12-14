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
                .child("Employees");

        q.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("onDataChange");
                for(DataSnapshot ds : dataSnapshot.getChildren()){
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
